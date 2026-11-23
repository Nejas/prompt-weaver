import asyncio
import os
import uuid
from datetime import datetime, timedelta, timezone
import asyncpg
import grpc
from generated import identity_pb2 as pb, identity_pb2_grpc as rpc
from services.identity_service.security import hash_password, verify_password, new_token, token_hash

ACCESS_SECONDS = 900
REFRESH_DAYS = 30


class IdentityService(rpc.IdentityServiceServicer):
    def __init__(self, pool):
        self.pool = pool

    async def Register(self, request, context):
        email = request.email.strip().lower()
        if len(email) > 254 or len(email.split('@')) != 2 or not all(email.split('@')):
            await context.abort(grpc.StatusCode.INVALID_ARGUMENT, 'Invalid email')
        first_name = request.first_name.strip()
        last_name = request.last_name.strip()
        if not 1 <= len(first_name) <= 100 or not 1 <= len(last_name) <= 100:
            await context.abort(grpc.StatusCode.INVALID_ARGUMENT, 'First name and last name are required')
        try:
            password = await asyncio.to_thread(hash_password, request.password)
        except ValueError as exc:
            await context.abort(grpc.StatusCode.INVALID_ARGUMENT, str(exc))
        uid = uuid.uuid4()
        try:
            async with self.pool.acquire() as conn:
                async with conn.transaction():
                    await conn.execute(
                        'INSERT INTO users (id,email,first_name,last_name,password_hash) VALUES ($1,$2,$3,$4,$5)',
                        uid, email, first_name, last_name, password,
                    )
                    await conn.execute("INSERT INTO user_roles (user_id,role_code) VALUES ($1,'user')", uid)
        except asyncpg.UniqueViolationError:
            await context.abort(grpc.StatusCode.ALREADY_EXISTS, 'Email already registered')
        return pb.User(id=str(uid), email=email, first_name=first_name, last_name=last_name, roles=['user'])

    async def Login(self, request, context):
        async with self.pool.acquire() as conn:
            user = await conn.fetchrow('SELECT id,password_hash,status FROM users WHERE lower(btrim(email))=$1', request.email.strip().lower())
            if not user or user['status'] != 'active' or not await asyncio.to_thread(verify_password, request.password, user['password_hash']):
                await context.abort(grpc.StatusCode.UNAUTHENTICATED, 'Invalid credentials')
            access, refresh = new_token(), new_token()
            now = datetime.now(timezone.utc)
            await conn.execute('INSERT INTO auth_sessions (id,user_id,access_token_hash,refresh_token_hash,access_expires_at,refresh_expires_at) VALUES ($1,$2,$3,$4,$5,$6)', uuid.uuid4(), user['id'], token_hash(access), token_hash(refresh), now+timedelta(seconds=ACCESS_SECONDS), now+timedelta(days=REFRESH_DAYS))
        return pb.Tokens(access_token=access, refresh_token=refresh, expires_in=ACCESS_SECONDS)

    async def Refresh(self, request, context):
        access, refresh = new_token(), new_token()
        now = datetime.now(timezone.utc)
        async with self.pool.acquire() as conn:
            result = await conn.fetchrow('UPDATE auth_sessions s SET access_token_hash=$2, refresh_token_hash=$3, access_expires_at=LEAST($4,s.refresh_expires_at) FROM users u WHERE s.user_id=u.id AND u.status=\'active\' AND s.refresh_token_hash=$1 AND s.revoked_at IS NULL AND s.refresh_expires_at>$5 RETURNING s.access_expires_at', token_hash(request.token), token_hash(access), token_hash(refresh), now+timedelta(seconds=ACCESS_SECONDS), now)
        if not result:
            await context.abort(grpc.StatusCode.UNAUTHENTICATED, 'Invalid refresh token')
        return pb.Tokens(access_token=access, refresh_token=refresh, expires_in=max(0,int((result['access_expires_at']-now).total_seconds())))

    async def Logout(self, request, context):
        async with self.pool.acquire() as conn:
            await conn.execute('UPDATE auth_sessions SET revoked_at=now() WHERE refresh_token_hash=$1 AND revoked_at IS NULL', token_hash(request.token))
        return pb.Empty()

    async def Me(self, request, context):
        async with self.pool.acquire() as conn:
            user = await conn.fetchrow("SELECT u.id,u.email,u.first_name,u.last_name FROM auth_sessions s JOIN users u ON u.id=s.user_id WHERE s.access_token_hash=$1 AND s.revoked_at IS NULL AND s.access_expires_at>now() AND u.status='active'", token_hash(request.token))
            if not user:
                await context.abort(grpc.StatusCode.UNAUTHENTICATED, 'Invalid access token')
            roles = await conn.fetch('SELECT role_code FROM user_roles WHERE user_id=$1 ORDER BY role_code', user['id'])
        return pb.User(
            id=str(user['id']), email=user['email'], first_name=user['first_name'] or '',
            last_name=user['last_name'] or '', roles=[r['role_code'] for r in roles],
        )


async def main():
    pool = await asyncpg.create_pool(host=os.getenv('DB_HOST','localhost'), port=int(os.getenv('DB_PORT','5432')), database=os.getenv('DB_NAME','identity'), user=os.environ['DB_USER'], password=os.environ['DB_PASSWORD'], min_size=1,max_size=5)
    server = grpc.aio.server()
    rpc.add_IdentityServiceServicer_to_server(IdentityService(pool), server)
    server.add_insecure_port('[::]:50052')
    try:
        await server.start()
        await server.wait_for_termination()
    finally:
        await server.stop(5)
        await pool.close()


if __name__ == '__main__':
    asyncio.run(main())
