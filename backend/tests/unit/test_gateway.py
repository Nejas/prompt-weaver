import grpc
import httpx
from services.gateway.main import app


def test_gateway_validates_http_and_translates_service_errors():
    class BrokenIdentity:
        async def Login(self, request, timeout):
            raise grpc.aio.AioRpcError(grpc.StatusCode.UNAUTHENTICATED, None, None, 'Invalid credentials')

    async def exercise():
        original = getattr(app.state, 'identity', None)
        app.state.identity = BrokenIdentity()
        try:
            async with httpx.AsyncClient(transport=httpx.ASGITransport(app=app), base_url='http://test') as client:
                assert (await client.post('/api/v1/auth/login', json={'email': 'x', 'password': 'x'})).status_code == 422
                response = await client.post('/api/v1/auth/login', json={'email': 'a@b.test', 'password': 'wrong'})
                assert response.status_code == 401
                assert response.json()['detail'] == 'Invalid credentials'
                assert (await client.get('/api/v1/me')).status_code == 401
                assert (await client.get('/api/v1/templates?limit=0')).status_code == 400
        finally:
            app.state.identity = original
    coroutine = exercise()
    try:
        coroutine.send(None)
    except StopIteration:
        return
    raise AssertionError('Unexpected asynchronous I/O')
