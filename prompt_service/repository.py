import os
import uuid
import asyncpg


async def create_pool():
    return await asyncpg.create_pool(
        host=os.getenv('DB_HOST', 'localhost'),
        port=int(os.getenv('DB_PORT', '5432')),
        database=os.getenv('DB_NAME', 'prompts'),
        user=os.environ['DB_USER'],
        password=os.environ['DB_PASSWORD'],
        min_size=1, max_size=5,
    )


async def get_published_template(pool, version_id):
    try:
        version_id = uuid.UUID(version_id)
    except ValueError:
        return None
    async with pool.acquire() as connection:
        async with connection.transaction(isolation='repeatable_read', readonly=True):
            version = await connection.fetchrow(
                "SELECT v.id, v.body FROM template_versions v JOIN templates t ON t.id = v.template_id WHERE v.id = $1 AND v.status = 'published' AND t.status = 'active'",
                version_id,
            )
            if not version:
                return None
            params = await connection.fetch(
                'SELECT name, type, required, max_length, options FROM template_parameters WHERE template_version_id = $1', version_id
            )
            return version, params
