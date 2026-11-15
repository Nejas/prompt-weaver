import json
import grpc
import pytest
from generated import prompt_pb2, rules_pb2
from services.prompt_service import server as prompt
from services.rules_service.server import RulesService


def run_immediate(coro):
    try:
        coro.send(None)
    except StopIteration as result:
        return result.value
    raise AssertionError('Unexpected asynchronous I/O')



class Aborted(Exception):
    def __init__(self, code, detail):
        self.code = code
        self.detail = detail


class Context:
    async def abort(self, code, detail):
        raise Aborted(code, detail)


def run(coro):
    return run_immediate(coro)


def test_prompt_service_composes_and_validates(monkeypatch):
    async def get_template(pool, version):
        return ({'id': version, 'body': 'Hello {{name}}'}, [
            {'name': 'name', 'type': 'text', 'required': True, 'max_length': 10, 'options': None}])
    monkeypatch.setattr(prompt, 'get_published_template', get_template)
    service = prompt.PromptService(None)
    response = run(service.Compose(prompt_pb2.ComposeRequest(template_version_id='v1', parameters={'name': 'Ada'}), Context()))
    assert response.prompt == 'Hello Ada'
    with pytest.raises(Aborted) as error:
        run(service.Compose(prompt_pb2.ComposeRequest(template_version_id='v1'), Context()))
    assert error.value.code == grpc.StatusCode.INVALID_ARGUMENT


def test_prompt_service_missing_and_bad_pagination(monkeypatch):
    async def missing(pool, version):
        return None
    monkeypatch.setattr(prompt, 'get_published_template', missing)
    service = prompt.PromptService(None)
    with pytest.raises(Aborted) as error:
        run(service.GetTemplate(prompt_pb2.TemplateRequest(template_version_id='missing'), Context()))
    assert error.value.code == grpc.StatusCode.NOT_FOUND
    with pytest.raises(Aborted) as error:
        run(service.ListTemplates(prompt_pb2.ListTemplatesRequest(limit=101), Context()))
    assert error.value.code == grpc.StatusCode.INVALID_ARGUMENT


def test_rules_service_filters_drafts_and_handles_bad_storage(tmp_path):
    path = tmp_path / 'rules.json'
    path.write_text(json.dumps([{'id': 'draft', 'status': 'draft'}, {'id': 'ok', 'status': 'published', 'name': 'Rules', 'rules': []}]), encoding='utf-8')
    service = RulesService(path)
    assert run(service.GetRuleSet(rules_pb2.RuleSetRequest(id='ok'), Context())).name == 'Rules'
    with pytest.raises(Aborted) as error:
        run(service.GetRuleSet(rules_pb2.RuleSetRequest(id='draft'), Context()))
    assert error.value.code == grpc.StatusCode.NOT_FOUND
    path.write_text('{', encoding='utf-8')
    with pytest.raises(Aborted) as error:
        run(service.GetRuleSet(rules_pb2.RuleSetRequest(id='ok'), Context()))
    assert error.value.code == grpc.StatusCode.UNAVAILABLE
