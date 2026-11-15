import json
import grpc
import httpx
from generated import prompt_pb2, rules_pb2
from gateway.main import app


def test_catalog_and_scenario(tmp_path, monkeypatch):
    version = 'ab1a4b42-1957-495c-af89-903e51874902'
    path = tmp_path / 'scenarios.json'
    path.write_text(json.dumps({version: 'teaching'}), encoding='utf-8')
    monkeypatch.setenv('SCENARIOS_FILE', str(path))

    class PromptStub:
        async def ListTemplates(self, req, timeout):
            assert req.limit == 10 and req.offset == 0
            return prompt_pb2.ListTemplatesResponse(templates=[prompt_pb2.TemplateSummary(id=version, title='Explain')])

        async def GetTemplate(self, req, timeout):
            if req.template_version_id != version:
                raise grpc.aio.AioRpcError(grpc.StatusCode.NOT_FOUND, None, None, 'Not found')
            return prompt_pb2.TemplateResponse(id=version, title='Explain', body='Explain {{topic}}', parameters=[prompt_pb2.ParameterDefinition(name='topic', label='Topic', type='text', required=True)])

    class RulesStub:
        async def GetRuleSet(self, req, timeout):
            assert req.id == 'teaching'
            return rules_pb2.RuleSetResponse(id='teaching', rules=[rules_pb2.Rule(id='example', text='Give example', order=1)])

    class UnavailableRules:
        async def GetRuleSet(self, req, timeout):
            raise grpc.aio.AioRpcError(grpc.StatusCode.UNAVAILABLE, None, None, 'Unavailable')

    async def run():
        app.state.prompt = PromptStub()
        app.state.rules = RulesStub()
        async with httpx.AsyncClient(transport=httpx.ASGITransport(app=app), base_url='http://test') as client:
            assert (await client.get('/api/v1/templates?limit=10')).json()['templates'][0]['id'] == version
            assert (await client.get('/api/v1/templates?limit=101')).status_code == 400
            detail = await client.get('/api/v1/templates/' + version)
            assert detail.json()['parameters'][0]['label'] == 'Topic'
            assert (await client.get('/api/v1/templates/unknown')).status_code == 404
            body = {'template_version_id': version, 'parameters': {'topic': 'Python'}}
            result = await client.post('/api/v1/prompts/compose-scenario', json=body)
            assert result.status_code == 200 and 'Give example' in result.json()['prompt']
            app.state.rules = UnavailableRules()
            assert (await client.post('/api/v1/prompts/compose-scenario', json=body)).status_code == 503
            app.state.rules = RulesStub()
            path.write_text('{}', encoding='utf-8')
            assert (await client.post('/api/v1/prompts/compose-scenario', json=body)).status_code == 404
            path.write_text('not json', encoding='utf-8')
            assert (await client.post('/api/v1/prompts/compose-scenario', json=body)).status_code == 503
    coroutine = run()
    try:
        coroutine.send(None)
    except StopIteration:
        return
    raise AssertionError('Unexpected asynchronous I/O in test doubles')
