import json


def validate_parameters(definitions, supplied):
    definitions = {p['name']: p for p in definitions}
    unknown = set(supplied) - definitions.keys()
    if unknown:
        raise ValueError('Unknown parameters: ' + ', '.join(sorted(unknown)))
    values = {}
    for name, definition in definitions.items():
        value = supplied.get(name, '')
        if not value.strip():
            if definition['required']:
                raise ValueError('Required parameter: ' + name)
            values[name] = ''
            continue
        limit = min(definition['max_length'] or 10000, 10000)
        if len(value) > limit:
            raise ValueError('Parameter too long: ' + name)
        if definition['type'] == 'choice':
            options = definition['options']
            if isinstance(options, str):
                options = json.loads(options)
            if value not in options:
                raise ValueError('Invalid choice: ' + name)
        values[name] = value
    return values
