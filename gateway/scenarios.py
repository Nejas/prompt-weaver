import json
import os
from pathlib import Path
from uuid import UUID


def rule_set_for_version(version_id):
    try:
        key = str(UUID(version_id))
        data = json.loads(Path(os.getenv('SCENARIOS_FILE', 'gateway/scenarios.json')).read_text(encoding='utf-8'))
        if not isinstance(data, dict) or any(not isinstance(k, str) or not isinstance(v, str) or not v.strip() for k, v in data.items()):
            raise ValueError('Invalid scenario configuration')
    except (OSError, ValueError, TypeError) as exc:
        raise RuntimeError('Scenario configuration unavailable') from exc
    return data.get(key)
