import os
from contextlib import asynccontextmanager
import grpc
from fastapi import FastAPI, HTTPException, Request
from pydantic import BaseModel, Field
from generated import prompt_pb2, prompt_pb2_grpc
from generated import identity_pb2, identity_pb2_grpc
from generated import rules_pb2, rules_pb2_grpc
from prompt_engine import Parameter, PromptTemplate, Rule, build_prompt
from services.gateway.scenarios import rule_set_for_version
from fastapi import Header


@asynccontextmanager
async def lifespan(app: FastAPI):
    channel = grpc.aio.insecure_channel(os.getenv('PROMPT_SERVICE_ADDRESS', 'localhost:50051'))
    app.state.prompt = prompt_pb2_grpc.PromptServiceStub(channel)
    identity_channel = grpc.aio.insecure_channel(os.getenv('IDENTITY_SERVICE_ADDRESS', 'localhost:50052'))
    app.state.identity = identity_pb2_grpc.IdentityServiceStub(identity_channel)
    rules_channel = grpc.aio.insecure_channel(os.getenv('RULES_SERVICE_ADDRESS', 'localhost:50053'))
    app.state.rules = rules_pb2_grpc.RulesServiceStub(rules_channel)
    yield
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
    scheme, _, token = authorization.partition(' ')
    if scheme.lower() != 'bearer' or not token:
        raise HTTPException(status_code=401, detail='Bearer token required')
    result = await identity_call(request.app.state.identity.Me, identity_pb2.Token(token=token))
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
    rule_set = await rules_call(request, rule_set_id)
    params = [Parameter(p.name, p.type, p.required, p.max_length or None, list(p.options)) for p in template.parameters]
    rules = [Rule(r.id, r.text, r.order) for r in rule_set.rules]
    try:
        result = build_prompt(PromptTemplate(template.body, params), values, rules)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    return {'template_version_id': template.id, 'rule_set_id': rule_set.id, 'prompt': result.text}
