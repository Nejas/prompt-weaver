import pytest
from services.identity_service.security import hash_password, verify_password, new_token, token_hash


def test_password_hash_is_salted_and_verified():
    first = hash_password('a sufficiently long password')
    second = hash_password('a sufficiently long password')
    assert first != second
    assert verify_password('a sufficiently long password', first)
    assert not verify_password('a different long password', first)


def test_rejects_short_password_and_invalid_hash():
    with pytest.raises(ValueError):
        hash_password('short')
    assert not verify_password('anything', 'invalid')


def test_tokens_are_random_and_stored_as_hashes():
    first, second = new_token(), new_token()
    assert first != second
    assert len(token_hash(first)) == 32
    assert token_hash(first) != token_hash(second)
