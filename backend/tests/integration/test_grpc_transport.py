import asyncio
import json

import grpc
import pytest

from generated import prompt_pb2, prompt_pb2_grpc, rules_pb2, rules_pb2_grpc
from services.prompt_service import server as prompt_server
from services.rules_service.server import RulesService


def test_prompt_grpc_transport(monkeypatch):
    async def get_template(pool, version):
        if version != 'published':
            return None
        return ({'id': version, 'body': 'Hello {{name}}', 'title': 'Greeting',
                 'description': None, 'category': 'Demo', 'slug': 'greeting'},
                [{'name': 'name', 'label': 'Name', 'type': 'text', 'required': True,
                  'max_length': 10, 'options': None}])

    monkeypatch.setattr(prompt_server, 'get_published_template', get_template)

    async def exercise():
        server = grpc.aio.server()
        prompt_pb2_grpc.add_PromptServiceServicer_to_server(prompt_server.PromptService(None), server)
        port = server.add_insecure_port('127.0.0.1:0')
        assert port
        await server.start()
        channel = grpc.aio.insecure_channel(f'127.0.0.1:{port}')
        try:
            client = prompt_pb2_grpc.PromptServiceStub(channel)
            response = await client.Compose(prompt_pb2.ComposeRequest(template_version_id='published', parameters={'name': 'Ada'}), timeout=5)
            assert response.prompt == 'Hello Ada'
            detail = await client.GetTemplate(prompt_pb2.TemplateRequest(template_version_id='published'), timeout=5)
            assert detail.parameters[0].label == 'Name'
            with pytest.raises(grpc.aio.AioRpcError) as error:
                await client.Compose(prompt_pb2.ComposeRequest(template_version_id='published'), timeout=5)
            assert error.value.code() == grpc.StatusCode.INVALID_ARGUMENT
            with pytest.raises(grpc.aio.AioRpcError) as error:
                await client.GetTemplate(prompt_pb2.TemplateRequest(template_version_id='missing'), timeout=5)
            assert error.value.code() == grpc.StatusCode.NOT_FOUND
        finally:
            await channel.close()
            await server.stop(0)
    asyncio.run(exercise())


def test_rules_grpc_transport(tmp_path):
    path = tmp_path / 'rules.json'
    path.write_text(json.dumps([
        {'id': 'active', 'name': 'Rules', 'status': 'published', 'rules': [{'id': 'a', 'text': 'Be concise', 'order': 1}]},
        {'id': 'draft', 'name': 'Draft', 'status': 'draft', 'rules': []},
    ]), encoding='utf-8')

    async def exercise():
        server = grpc.aio.server()
        rules_pb2_grpc.add_RulesServiceServicer_to_server(RulesService(path), server)
        port = server.add_insecure_port('127.0.0.1:0')
        assert port
        await server.start()
        channel = grpc.aio.insecure_channel(f'127.0.0.1:{port}')
        try:
            client = rules_pb2_grpc.RulesServiceStub(channel)
            result = await client.GetRuleSet(rules_pb2.RuleSetRequest(id='active'), timeout=5)
            assert result.rules[0].text == 'Be concise'
            with pytest.raises(grpc.aio.AioRpcError) as error:
                await client.GetRuleSet(rules_pb2.RuleSetRequest(id='draft'), timeout=5)
            assert error.value.code() == grpc.StatusCode.NOT_FOUND
            path.write_text('{', encoding='utf-8')
            with pytest.raises(grpc.aio.AioRpcError) as error:
                await client.GetRuleSet(rules_pb2.RuleSetRequest(id='active'), timeout=5)
            assert error.value.code() == grpc.StatusCode.UNAVAILABLE
        finally:
            await channel.close()
            await server.stop(0)
    asyncio.run(exercise())
