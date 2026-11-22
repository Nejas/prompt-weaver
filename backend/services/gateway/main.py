import os
import time
from collections import defaultdict, deque
from contextlib import asynccontextmanager
import grpc
from fastapi import FastAPI, HTTPException, Request
from pydantic import BaseModel, Field
from generated import prompt_pb2, prompt_pb2_grpc
from generated import identity_pb2, identity_pb2_grpc
from generated import rules_pb2, rules_pb2_grpc
from prompt_engine import Parameter, PromptTemplate, Rule, build_prompt
from services.gateway.bifrost import BifrostClient, BifrostError
from services.gateway.scenarios import rule_set_for_version
from fastapi import Header


class GenerationRateLimiter:
    """Per-process protection against accidental or abusive model usage."""

    def __init__(self, limit: int, window_seconds: int):
        if limit < 1 or window_seconds < 1:
            raise ValueError('Generation rate limit settings must be positive')
        self._limit = limit
        self._window_seconds = window_seconds
        self._requests: dict[str, deque[float]] = defaultdict(deque)

    def allow(self, user_id: str) -> bool:
        now = time.monotonic()
        requests = self._requests[user_id]
        cutoff = now - self._window_seconds
        while requests and requests[0] <= cutoff:
            requests.popleft()
        if len(requests) >= self._limit:
            return False
        requests.append(now)
        return True


@asynccontextmanager
async def lifespan(app: FastAPI):
    channel = grpc.aio.insecure_channel(os.getenv('PROMPT_SERVICE_ADDRESS', 'localhost:50051'))
    app.state.prompt = prompt_pb2_grpc.PromptServiceStub(channel)
    identity_channel = grpc.aio.insecure_channel(os.getenv('IDENTITY_SERVICE_ADDRESS', 'localhost:50052'))
    app.state.identity = identity_pb2_grpc.IdentityServiceStub(identity_channel)
    rules_channel = grpc.aio.insecure_channel(os.getenv('RULES_SERVICE_ADDRESS', 'localhost:50053'))
    app.state.rules = rules_pb2_grpc.RulesServiceStub(rules_channel)
    app.state.bifrost = BifrostClient(
        os.getenv('BIFROST_URL', 'http://localhost:8080'),
        float(os.getenv('BIFROST_TIMEOUT_SECONDS', '30')),
    )
    app.state.generation_model = os.getenv('BIFROST_MODEL', 'deepseek-flash')
    app.state.generation_rate_limiter = GenerationRateLimiter(
        int(os.getenv('GENERATION_RATE_LIMIT', '10')),
        int(os.getenv('GENERATION_RATE_WINDOW_SECONDS', '60')),
    )
    yield
    await app.state.bifrost.close()
    await rules_channel.close()
    await identity_channel.close()
    await channel.close()


app = FastAPI(lifespan=lifespan)


class ComposeBody(BaseModel):
    template_version_id: str = Field(min_length=1, max_length=128)
    parameters: dict[str, str]


class Credentials(BaseModel):
    email: str = Field(min_length=3, max_length=254)
    password: str = Field(min_length=1, max_length=256)


class ComposeWithRulesBody(ComposeBody):
    rule_set_id: str = Field(min_length=1, max_length=128)


class GenerateBody(ComposeBody):
    message: str = Field(min_length=1, max_length=12000)
    rule_set_id: str | None = Field(default=None, min_length=1, max_length=128)


class TokenBody(BaseModel):
    refresh_token: str = Field(min_length=1, max_length=256)


async def identity_call(call, message):
    try:
        return await call(message, timeout=5)
    except grpc.aio.AioRpcError as exc:
        status = {grpc.StatusCode.INVALID_ARGUMENT: 400, grpc.StatusCode.ALREADY_EXISTS: 409,
                  grpc.StatusCode.UNAUTHENTICATED: 401, grpc.StatusCode.UNAVAILABLE: 503,
                  grpc.StatusCode.DEADLINE_EXCEEDED: 504}.get(exc.code(), 500)
        raise HTTPException(status_code=status, detail=exc.details() if status != 500 else 'Internal error') from exc


async def current_user(request: Request, authorization: str):
    scheme, _, token = authorization.partition(' ')
    if scheme.lower() != 'bearer' or not token:
        raise HTTPException(status_code=401, detail='Bearer token required')
    return await identity_call(request.app.state.identity.Me, identity_pb2.Token(token=token))


@app.post('/api/v1/auth/register', status_code=201)
async def register(body: Credentials, request: Request):
    result = await identity_call(request.app.state.identity.Register, identity_pb2.Credentials(**body.model_dump()))
    return {'id': result.id, 'email': result.email, 'roles': list(result.roles)}


@app.post('/api/v1/auth/login')
async def login(body: Credentials, request: Request):
    result = await identity_call(request.app.state.identity.Login, identity_pb2.Credentials(**body.model_dump()))
    return {'access_token': result.access_token, 'refresh_token': result.refresh_token, 'token_type': 'bearer', 'expires_in': result.expires_in}


@app.post('/api/v1/auth/refresh')
async def refresh(body: TokenBody, request: Request):
    result = await identity_call(request.app.state.identity.Refresh, identity_pb2.Token(token=body.refresh_token))
    return {'access_token': result.access_token, 'refresh_token': result.refresh_token, 'token_type': 'bearer', 'expires_in': result.expires_in}


@app.post('/api/v1/auth/logout', status_code=204)
async def logout(body: TokenBody, request: Request):
    await identity_call(request.app.state.identity.Logout, identity_pb2.Token(token=body.refresh_token))


@app.get('/api/v1/me')
async def me(request: Request, authorization: str = Header(default='', max_length=512)):
    result = await current_user(request, authorization)
    return {'id': result.id, 'email': result.email, 'roles': list(result.roles)}


@app.post('/api/v1/prompts/compose')
async def compose(body: ComposeBody, request: Request):
    try:
        result = await request.app.state.prompt.Compose(
            prompt_pb2.ComposeRequest(**body.model_dump()), timeout=5
        )
    except grpc.aio.AioRpcError as exc:
        status = {grpc.StatusCode.INVALID_ARGUMENT: 400, grpc.StatusCode.NOT_FOUND: 404,
                  grpc.StatusCode.UNAVAILABLE: 503, grpc.StatusCode.DEADLINE_EXCEEDED: 504}.get(exc.code(), 500)
        raise HTTPException(status_code=status, detail=exc.details() if status != 500 else 'Internal error') from exc
    return {'template_version_id': result.template_version_id, 'prompt': result.prompt}


async def rules_call(request, rule_set_id):
    try:
        return await request.app.state.rules.GetRuleSet(rules_pb2.RuleSetRequest(id=rule_set_id), timeout=5)
    except grpc.aio.AioRpcError as exc:
        status = {grpc.StatusCode.NOT_FOUND: 404, grpc.StatusCode.UNAVAILABLE: 503, grpc.StatusCode.DEADLINE_EXCEEDED: 504}.get(exc.code(), 500)
        raise HTTPException(status_code=status, detail=exc.details() if status != 500 else 'Internal error') from exc


@app.get('/api/v1/rule-sets/{rule_set_id}')
async def get_rule_set(rule_set_id: str, request: Request):
    result = await rules_call(request, rule_set_id)
    return {'id': result.id, 'name': result.name, 'rules': [{'id': r.id, 'text': r.text, 'order': r.order} for r in result.rules]}


async def prompt_call(call, message):
    try:
        return await call(message, timeout=5)
    except grpc.aio.AioRpcError as exc:
        status = {grpc.StatusCode.INVALID_ARGUMENT: 400, grpc.StatusCode.NOT_FOUND: 404, grpc.StatusCode.UNAVAILABLE: 503, grpc.StatusCode.DEADLINE_EXCEEDED: 504}.get(exc.code(), 500)
        raise HTTPException(status_code=status, detail=exc.details() if status != 500 else 'Internal error') from exc


@app.get('/api/v1/templates')
async def list_templates(request: Request, limit: int = 50, offset: int = 0):
    if not 1 <= limit <= 100 or offset < 0:
        raise HTTPException(status_code=400, detail='Invalid pagination')
    result = await prompt_call(request.app.state.prompt.ListTemplates, prompt_pb2.ListTemplatesRequest(limit=limit, offset=offset))
    return {'templates': [{'id': t.id, 'title': t.title, 'description': t.description, 'category': t.category, 'slug': t.slug} for t in result.templates], 'limit': limit, 'offset': offset}


@app.get('/api/v1/templates/{version_id}')
async def get_template(version_id: str, request: Request):
    t = await prompt_call(request.app.state.prompt.GetTemplate, prompt_pb2.TemplateRequest(template_version_id=version_id))
    return {'id': t.id, 'title': t.title, 'description': t.description, 'category': t.category, 'slug': t.slug, 'body': t.body, 'parameters': [{'name': p.name, 'label': p.label, 'type': p.type, 'required': p.required, 'max_length': p.max_length or None, 'options': list(p.options)} for p in t.parameters]}


@app.post('/api/v1/prompts/compose-scenario')
async def compose_scenario(body: ComposeBody, request: Request):
    template = await prompt_call(request.app.state.prompt.GetTemplate, prompt_pb2.TemplateRequest(template_version_id=body.template_version_id))
    try:
        rule_set_id = rule_set_for_version(template.id)
    except RuntimeError as exc:
        raise HTTPException(status_code=503, detail=str(exc)) from exc
    if not rule_set_id:
        raise HTTPException(status_code=404, detail='No rule set configured for template version')
    return await compose_template_with_rules(template, body.parameters, request, rule_set_id)


@app.post('/api/v1/prompts/compose-with-rules')
async def compose_with_rules(body: ComposeWithRulesBody, request: Request):
    template = await prompt_call(request.app.state.prompt.GetTemplate, prompt_pb2.TemplateRequest(template_version_id=body.template_version_id))
    return await compose_template_with_rules(template, body.parameters, request, body.rule_set_id)


async def compose_template_with_rules(template, values, request, rule_set_id):
    prompt, resolved_rule_set_id = await compose_prompt(template, values, request, rule_set_id)
    return {'template_version_id': template.id, 'rule_set_id': resolved_rule_set_id, 'prompt': prompt}


async def compose_prompt(template, values, request, rule_set_id=None):
    rules = []
    resolved_rule_set_id = None
    if rule_set_id:
        rule_set = await rules_call(request, rule_set_id)
        rules = [Rule(r.id, r.text, r.order) for r in rule_set.rules]
        resolved_rule_set_id = rule_set.id
    params = [Parameter(p.name, p.type, p.required, p.max_length or None, list(p.options)) for p in template.parameters]
    try:
        result = build_prompt(PromptTemplate(template.body, params), values, rules)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    return result.text, resolved_rule_set_id


def generation_rate_limiter(request: Request) -> GenerationRateLimiter:
    limiter = getattr(request.app.state, 'generation_rate_limiter', None)
    if limiter is None:
        limiter = GenerationRateLimiter(10, 60)
        request.app.state.generation_rate_limiter = limiter
    return limiter


@app.post('/api/v1/prompts/generate')
async def generate(body: GenerateBody, request: Request, authorization: str = Header(default='', max_length=512)):
    user = await current_user(request, authorization)
    if not generation_rate_limiter(request).allow(user.id):
        raise HTTPException(status_code=429, detail='Generation rate limit exceeded')

    template = await prompt_call(
        request.app.state.prompt.GetTemplate,
        prompt_pb2.TemplateRequest(template_version_id=body.template_version_id),
    )
    prompt, resolved_rule_set_id = await compose_prompt(
        template,
        body.parameters,
        request,
        body.rule_set_id,
    )
    bifrost = getattr(request.app.state, 'bifrost', None)
    if bifrost is None:
        raise HTTPException(status_code=503, detail='Model gateway is unavailable')
    try:
        completion = await bifrost.create_chat_completion(
            model=getattr(request.app.state, 'generation_model', os.getenv('BIFROST_MODEL', 'deepseek-flash')),
            system_prompt=prompt,
            user_message=body.message,
        )
    except BifrostError as exc:
        raise HTTPException(status_code=exc.status_code, detail=exc.detail) from exc
    return {
        'template_version_id': template.id,
        'rule_set_id': resolved_rule_set_id,
        'model': completion.model,
        'response': completion.content,
        'usage': completion.usage,
    }
