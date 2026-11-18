import json
import grpc
import httpx
from generated import prompt_pb2, rules_pb2
from services.gateway.main import app
from services.rules_service.server import RulesService


def test_rules_and_gateway_composition(tmp_path):
    async def run():
        path = tmp_path / 'rules.json'
        path.write_text(json.dumps([
            {'id': 'active', 'name': 'Learning', 'status': 'published', 'rules': [{'id': 'a', 'text': 'Give an example', 'order': 1}]},
            {'id': 'draft', 'name': 'Draft', 'status': 'draft', 'rules': []},
        ]), encoding='utf-8')
        service = RulesService(path)

        class Context:
            async def abort(self, code, details):
                raise grpc.aio.AioRpcError(code, None, None, details)

        class RulesStub:
            async def GetRuleSet(self, request, timeout):
                return await service.GetRuleSet(request, Context())

        class PromptStub:
            async def GetTemplate(self, request, timeout):
                return prompt_pb2.TemplateResponse(id=request.template_version_id, body='Explain {{topic}}', parameters=[prompt_pb2.ParameterDefinition(name='topic', type='text', required=True)])

        app.state.rules = RulesStub()
        app.state.prompt = PromptStub()
        async with httpx.AsyncClient(transport=httpx.ASGITransport(app=app), base_url='http://test') as client:
            assert (await client.get('/api/v1/rule-sets/draft')).status_code == 404
            assert (await client.get('/api/v1/rule-sets/active')).json()['rules'][0]['id'] == 'a'
            body = {'template_version_id': 'v1', 'parameters': {'topic': 'recursion'}, 'rule_set_id': 'active'}
            result = await client.post('/api/v1/prompts/compose-with-rules', json=body)
            assert result.status_code == 200
            assert result.json()['prompt'] == 'Инструкции сценария:\n- Give an example\n\nЗадача:\nExplain recursion'
            body['rule_set_id'] = 'draft'
            assert (await client.post('/api/v1/prompts/compose-with-rules', json=body)).status_code == 404
            path.unlink()
            body['rule_set_id'] = 'active'
            assert (await client.post('/api/v1/prompts/compose-with-rules', json=body)).status_code == 503
    coroutine = run()
    try:
        coroutine.send(None)
    except StopIteration:
        return
    raise AssertionError('Unexpected asynchronous I/O in test doubles')
