import pytest


def pytest_collection_modifyitems(items):
    for item in items:
        if 'integration' in item.path.parts:
            item.add_marker(pytest.mark.integration)
