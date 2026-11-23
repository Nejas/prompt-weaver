import grpc
import pytest
from generated import identity_pb2
from services.identity_service.server import IdentityService


def run_immediate(coro):
    try:
        coro.send(None)
    except StopIteration as result:
        return result.value
    raise AssertionError('Unexpected asynchronous I/O')



class Aborted(Exception):
    def __init__(self, code):
        self.code = code


class Context:
    async def abort(self, code, detail):
        raise Aborted(code)


class Connection:
    async def __aenter__(self):
        return self
    async def __aexit__(self, *args):
        pass
    async def fetchrow(self, *args):
        return None


class Pool:
    def acquire(self):
        return Connection()


def test_register_rejects_bad_email_without_database():
    with pytest.raises(Aborted) as error:
        run_immediate(IdentityService(Pool()).Register(identity_pb2.Registration(email='bad', password='long-password', first_name='Ada', last_name='Lovelace'), Context()))
    assert error.value.code == grpc.StatusCode.INVALID_ARGUMENT


def test_register_requires_first_and_last_name_without_database():
    with pytest.raises(Aborted) as error:
        run_immediate(IdentityService(Pool()).Register(identity_pb2.Registration(email='ada@example.test', password='long-password', first_name=' ', last_name='Lovelace'), Context()))
    assert error.value.code == grpc.StatusCode.INVALID_ARGUMENT


@pytest.mark.parametrize('method,message', [
    ('Login', identity_pb2.Credentials(email='missing@example.test', password='wrong')),
    ('Refresh', identity_pb2.Token(token='unknown')),
    ('Me', identity_pb2.Token(token='unknown')),
])
def test_unknown_credentials_are_unauthenticated(method, message):
    with pytest.raises(Aborted) as error:
        run_immediate(getattr(IdentityService(Pool()), method)(message, Context()))
    assert error.value.code == grpc.StatusCode.UNAUTHENTICATED
