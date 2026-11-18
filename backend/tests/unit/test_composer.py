import pytest
from services.prompt_service.composer import compose


def test_substitutes_without_evaluating_user_input():
    assert compose('Task: {{request}}', {'request': '{{secret}}'}) == 'Task: {{secret}}'


def test_rejects_missing_parameter():
    with pytest.raises(ValueError, match='missing'):
        compose('{{request}} {{level}}', {'request': 'recursion'})


def test_rejects_extra_parameter():
    with pytest.raises(ValueError, match='extra'):
        compose('{{request}}', {'request': 'test', 'other': 'x'})
