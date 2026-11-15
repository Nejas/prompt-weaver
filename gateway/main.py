import os
from contextlib import asynccontextmanager
import grpc
from fastapi import FastAPI, HTTPException, Request
from pydantic import BaseModel, Field
from generated import prompt_pb2, prompt_pb2_grpc
from generated import identity_pb2, identity_pb2_grpc
from fastapi import Header


@asynccontextmanager
async def lifespan(app: FastAPI):
    channel = grpc.aio.insecure_channel(os.getenv('PROMPT_SERVICE_ADDRESS', 'localhost:50051'))
    app.state.prompt = prompt_pb2_grpc.PromptServiceStub(channel)
    identity_channel = grpc.aio.insecure_channel(os.getenv('IDENTITY_SERVICE_ADDRESS', 'localhost:50052'))
    app.state.identity = identity_pb2_grpc.IdentityServiceStub(identity_channel)
    yield
    await identity_channel.close()
    await channel.close()


app = FastAPI(lifespan=lifespan)


class ComposeBody(BaseModel):
    template_version_id: str = Field(min_length=1, max_length=128)
    parameters: dict[str, str]


class Credentials(BaseModel):
    email: str = Field(min_length=3, max_length=254)
    password: str = Field(min_length=1, max_length=256)


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
