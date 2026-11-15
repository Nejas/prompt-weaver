import pytest

from prompt_service.composer import compose
from prompt_service.validation import validate_parameters


@pytest.fixture
def parameters():
    return [
        {'name': 'request', 'type': 'text', 'required': True, 'max_length': 20, 'options': None},
        {'name': 'level', 'type': 'choice', 'required': True, 'max_length': 20, 'options': '["basic", "advanced"]'},
        {'name': 'notes', 'type': 'text', 'required': False, 'max_length': 20, 'options': None},
    ]


def test_valid_parameters_produce_editable_prompt(parameters):
    values = validate_parameters(parameters, {'request': 'recursion', 'level': 'basic'})
    assert compose('Explain {{request}} at {{level}} level. {{notes}}', values, allow_empty=True) == (
        'Explain recursion at basic level. '
    )


@pytest.mark.parametrize('value', ['', '  '])
def test_required_values_cannot_be_empty(parameters, value):
    with pytest.raises(ValueError, match='Required parameter: request'):
        validate_parameters(parameters, {'request': value, 'level': 'basic'})


def test_rejects_unknown_parameter(parameters):
    with pytest.raises(ValueError, match='Unknown parameters: other'):
        validate_parameters(parameters, {'request': 'recursion', 'level': 'basic', 'other': 'x'})


def test_rejects_invalid_choice(parameters):
    with pytest.raises(ValueError, match='Invalid choice: level'):
        validate_parameters(parameters, {'request': 'recursion', 'level': 'expert'})


def test_rejects_value_over_limit(parameters):
    with pytest.raises(ValueError, match='Parameter too long: request'):
        validate_parameters(parameters, {'request': 'x' * 21, 'level': 'basic'})


def test_optional_parameter_can_be_empty(parameters):
    values = validate_parameters(parameters, {'request': 'recursion', 'level': 'basic', 'notes': ''})
    assert values['notes'] == ''
