"""Password hashing and opaque session token utilities."""
import base64
import hashlib
import hmac
import secrets

_N, _R, _P = 16384, 8, 1


def hash_password(password: str) -> str:
    if not 8 <= len(password) <= 256:
        raise ValueError('Password must contain between 8 and 256 characters')
    salt = secrets.token_bytes(16)
    digest = hashlib.scrypt(password.encode('utf-8'), salt=salt, n=_N, r=_R, p=_P, dklen=32)
    return 'scrypt$%s$%s' % (base64.b64encode(salt).decode('ascii'), base64.b64encode(digest).decode('ascii'))


def verify_password(password: str, encoded: str) -> bool:
    try:
        if len(password) > 256:
            return False
        scheme, salt, expected = encoded.split('$')
        if scheme != 'scrypt':
            return False
        salt = base64.b64decode(salt, validate=True)
        expected = base64.b64decode(expected, validate=True)
        if len(salt) != 16 or len(expected) != 32:
            return False
        actual = hashlib.scrypt(password.encode('utf-8'), salt=salt, n=_N, r=_R, p=_P, dklen=32)
        return hmac.compare_digest(actual, expected)
    except (ValueError, UnicodeError):
        return False


def new_token() -> str:
    return secrets.token_urlsafe(32)


def token_hash(token: str) -> bytes:
    return hashlib.sha256(token.encode('utf-8')).digest()
