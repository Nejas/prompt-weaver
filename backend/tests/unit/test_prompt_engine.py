import pytest
from prompt_engine import Parameter, PromptTemplate, Rule, build_prompt


def test_rules_are_optional_and_sorted():
    template = PromptTemplate('Тема: {{topic}}', [Parameter('topic')])
    assert build_prompt(template, {'topic': 'Python'}).text == 'Тема: Python'
    rules = [Rule('z', 'Последнее', 20), Rule('b', 'Второе', 10), Rule('a', 'Первое', 10)]
    assert build_prompt(template, {'topic': 'Python'}, rules).text == 'Инструкции сценария:\n- Первое\n- Второе\n- Последнее\n\nЗадача:\nТема: Python'


def test_reject_invalid_rules_and_values():
    template = PromptTemplate('{{topic}}', [Parameter('topic')])
    with pytest.raises(ValueError):
        build_prompt(template, {}, [])
    with pytest.raises(ValueError):
        build_prompt(template, {'topic': 'x'}, [Rule('same', 'one'), Rule('same', 'two')])
    with pytest.raises(ValueError):
        build_prompt(template, {'topic': 'x'}, [Rule('x', '  ')])
