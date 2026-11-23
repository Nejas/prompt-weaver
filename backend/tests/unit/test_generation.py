import httpx

from generated import identity_pb2, prompt_pb2
from services.gateway.bifrost import BifrostCompletion, BifrostError
from services.gateway.main import GenerationRateLimiter, app


def run_immediate(coro):
    try:
        coro.send(None)
    except StopIteration as result:
        return result.value
    raise AssertionError('Unexpected asynchronous I/O in test doubles')


def test_generation_requires_authentication():
    async def exercise():
        async with httpx.AsyncClient(transport=httpx.ASGITransport(app=app), base_url='http://test') as client:
            response = await client.post('/api/v1/prompts/generate', json={
                'template_version_id': 'template-1',
                'parameters': {'topic': 'Python'},
                'message': 'Explain decorators',
            })
        assert response.status_code == 401

    run_immediate(exercise())


def test_generation_composes_prompt_and_uses_bifrost():
    class IdentityStub:
        async def Me(self, request, timeout):
            assert request.token == 'access-token'
            return identity_pb2.User(id='user-1', email='user@example.test')

    class PromptStub:
        async def GetTemplate(self, request, timeout):
            assert request.template_version_id == 'template-1'
            return prompt_pb2.TemplateResponse(
                id='template-1',
                body='Explain {{topic}} clearly.',
                parameters=[prompt_pb2.ParameterDefinition(name='topic', type='text', required=True)],
            )

    class BifrostStub:
        async def create_chat_completion(self, *, model, system_prompt, user_message):
            assert model == 'gemini/gemini-3.6-flash'
            assert system_prompt == 'Explain Python clearly.'
            assert user_message == 'Explain decorators'
            return BifrostCompletion(
                content='Decorators wrap functions.',
                model='gemini/gemini-3.6-flash',
                usage={'prompt_tokens': 12, 'completion_tokens': 4},
            )

    async def exercise():
        original = {
            name: getattr(app.state, name, None)
            for name in ('identity', 'prompt', 'bifrost', 'generation_model', 'generation_rate_limiter')
        }
        app.state.identity = IdentityStub()
        app.state.prompt = PromptStub()
        app.state.bifrost = BifrostStub()
        app.state.generation_model = 'gemini/gemini-3.6-flash'
        app.state.generation_rate_limiter = GenerationRateLimiter(10, 60)
        try:
            async with httpx.AsyncClient(transport=httpx.ASGITransport(app=app), base_url='http://test') as client:
                response = await client.post(
                    '/api/v1/prompts/generate',
                    headers={'Authorization': 'Bearer access-token'},
                    json={
                        'template_version_id': 'template-1',
                        'parameters': {'topic': 'Python'},
                        'message': 'Explain decorators',
                    },
                )
            assert response.status_code == 200
            assert response.json() == {
                'template_version_id': 'template-1',
                'rule_set_id': None,
                'model': 'gemini/gemini-3.6-flash',
                'response': 'Decorators wrap functions.',
                'usage': {'prompt_tokens': 12, 'completion_tokens': 4},
            }
        finally:
            for name, value in original.items():
                setattr(app.state, name, value)

    run_immediate(exercise())


def test_generation_hides_upstream_error_details():
    class IdentityStub:
        async def Me(self, request, timeout):
            return identity_pb2.User(id='user-1')

    class PromptStub:
        async def GetTemplate(self, request, timeout):
            return prompt_pb2.TemplateResponse(id='template-1', body='Prompt', parameters=[])

    class BifrostStub:
        async def create_chat_completion(self, **kwargs):
            raise BifrostError(503, 'Model gateway is temporarily unavailable')

    async def exercise():
        original = {
            name: getattr(app.state, name, None)
            for name in ('identity', 'prompt', 'bifrost', 'generation_rate_limiter')
        }
        app.state.identity = IdentityStub()
        app.state.prompt = PromptStub()
        app.state.bifrost = BifrostStub()
        app.state.generation_rate_limiter = GenerationRateLimiter(10, 60)
        try:
            async with httpx.AsyncClient(transport=httpx.ASGITransport(app=app), base_url='http://test') as client:
                response = await client.post(
                    '/api/v1/prompts/generate',
                    headers={'Authorization': 'Bearer access-token'},
                    json={
                        'template_version_id': 'template-1',
                        'parameters': {},
                        'message': 'Hello',
                    },
                )
            assert response.status_code == 503
            assert response.json()['detail'] == 'Model gateway is temporarily unavailable'
        finally:
            for name, value in original.items():
                setattr(app.state, name, value)

    run_immediate(exercise())
