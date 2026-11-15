from dataclasses import dataclass
from typing import Sequence
from .composer import compose
from .validation import validate_parameters


@dataclass(frozen=True)
class Parameter:
    name: str
    type: str = 'text'
    required: bool = True
    max_length: int | None = None
    options: Sequence[str] | None = None


@dataclass(frozen=True)
class PromptTemplate:
    body: str
    parameters: Sequence[Parameter]


@dataclass(frozen=True)
class Rule:
    id: str
    text: str
    order: int = 0


@dataclass(frozen=True)
class PromptResult:
    text: str


def build_prompt(template: PromptTemplate, values: dict[str, str], rules: Sequence[Rule] = ()) -> PromptResult:
    names = [p.name for p in template.parameters]
    if len(names) != len(set(names)):
        raise ValueError('Duplicate parameter name')
    definitions = [vars(p) for p in template.parameters]
    validated = validate_parameters(definitions, values)
    result = compose(template.body, validated, allow_empty=True)
    ids = [rule.id for rule in rules]
    if any(not rule.id.strip() or not rule.text.strip() for rule in rules):
        raise ValueError('Rule id and text must not be empty')
    if len(ids) != len(set(ids)):
        raise ValueError('Duplicate rule id')
    if rules:
        instructions = '\n'.join('- ' + rule.text for rule in sorted(rules, key=lambda r: (r.order, r.id)))
        result = f'Инструкции сценария:\n{instructions}\n\nЗадача:\n{result}'
    return PromptResult(text=result)
