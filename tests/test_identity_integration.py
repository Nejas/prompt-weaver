"""Opt-in integration test: uses a dedicated disposable PostgreSQL database."""
import asyncio
import os
import uuid
import pytest

pytestmark = pytest.mark.skipif(os.getenv('IDENTITY_TEST_DB') != 'identity_integration_test', reason='Requires dedicated identity_integration_test database')


def test_register_login_rotation_and_logout():
    import asyncpg
    import grpc
    import httpx
    from generated import identity_pb2_grpc
    from gateway.main import app
    from identity_service.server import IdentityService

    async def run():
        pool = await asyncpg.create_pool(host='localhost', user='postgres', database='identity_integration_test')
        server = grpc.aio.server()
        identity_pb2_grpc.add_IdentityServiceServicer_to_server(IdentityService(pool), server)
        port = server.add_insecure_port('127.0.0.1:0')
        await server.start()
        channel = grpc.aio.insecure_channel(f'127.0.0.1:{port}')
        app.state.identity = identity_pb2_grpc.IdentityServiceStub(channel)
        credentials = {'email': f'{uuid.uuid4()}@example.test', 'password': 'Long-test-password-123'}
        try:
            async with httpx.AsyncClient(transport=httpx.ASGITransport(app=app), base_url='http://test') as client:
                assert (await client.post('/api/v1/auth/register', json=credentials)).status_code == 201
                assert (await client.post('/api/v1/auth/register', json=credentials)).status_code == 409
                assert (await client.post('/api/v1/auth/login', json={**credentials, 'password': 'wrong'})).status_code == 401
                result = await client.post('/api/v1/auth/login', json=credentials)
                assert result.status_code == 200
                first = result.json()
                assert (await client.get('/api/v1/me', headers={'Authorization': 'Bearer '+first['access_token']})).status_code == 200
                result = await client.post('/api/v1/auth/refresh', json={'refresh_token': first['refresh_token']})
                assert result.status_code == 200
                second = result.json()
                assert (await client.post('/api/v1/auth/refresh', json={'refresh_token': first['refresh_token']})).status_code == 401
                assert (await client.get('/api/v1/me', headers={'Authorization': 'Bearer '+first['access_token']})).status_code == 401
                assert (await client.post('/api/v1/auth/logout', json={'refresh_token': second['refresh_token']})).status_code == 204
                assert (await client.get('/api/v1/me', headers={'Authorization': 'Bearer '+second['access_token']})).status_code == 401
        finally:
            await channel.close()
            await server.stop(0)
            await pool.close()
    asyncio.run(run())
