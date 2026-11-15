from prompt_engine import Parameter, PromptTemplate, Rule, build_prompt
import pytest


def test_duplicate_parameter_definitions_are_rejected():
    with pytest.raises(ValueError, match='Duplicate parameter name'):
        build_prompt(PromptTemplate('{{x}}', [Parameter('x'), Parameter('x')]), {'x': 'a'})


def test_optional_empty_and_input_are_not_reinterpreted():
    template = PromptTemplate('{{x}} {{note}}', [Parameter('x'), Parameter('note', required=False)])
    assert build_prompt(template, {'x': '{{note}}'}).text == '{{note}} '


@pytest.mark.parametrize('rule', [Rule('', 'text'), Rule('id', ' ')])
def test_blank_rule_is_rejected(rule):
    with pytest.raises(ValueError, match='Rule id and text'):
        build_prompt(PromptTemplate('hello', []), {}, [rule])


def test_parameter_maximum_boundary():
    template = PromptTemplate('{{x}}', [Parameter('x', max_length=3)])
    assert build_prompt(template, {'x': 'abc'}).text == 'abc'
    with pytest.raises(ValueError, match='Parameter too long'):
        build_prompt(template, {'x': 'abcd'})
