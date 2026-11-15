import re

VARIABLE = re.compile(r"\{\{\s*([a-zA-Z][a-zA-Z0-9_]*)\s*\}\}")


def compose(template: str, parameters: dict[str, str], *, allow_empty: bool = False) -> str:
    """Substitute validated text once; user input is never evaluated as a template."""
    required = set(VARIABLE.findall(template))
    missing = sorted(name for name in required if name not in parameters or (not allow_empty and not parameters[name].strip()))
    extra = sorted(parameters.keys() - required)
    if missing or extra:
        raise ValueError(f"Invalid parameters: missing={missing}, extra={extra}")
    if any(len(value) > 10000 for value in parameters.values()):
        raise ValueError('Parameter exceeds 10000 characters')
    return VARIABLE.sub(lambda match: parameters[match.group(1)], template)
