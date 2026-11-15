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
                "SELECT v.id, v.body, v.title, v.description, t.slug, c.name AS category FROM template_versions v JOIN templates t ON t.id = v.template_id JOIN categories c ON c.id = t.category_id WHERE v.id = $1 AND v.status = 'published' AND t.status = 'active'",
                version_id,
            )
            if not version:
                return None
            params = await connection.fetch(
                'SELECT name, label, type, required, max_length, options FROM template_parameters WHERE template_version_id = $1 ORDER BY display_order, name', version_id
            )
            return version, params


async def list_published_templates(pool, limit, offset):
    async with pool.acquire() as connection:
        return await connection.fetch("SELECT v.id, v.title, v.description, t.slug, c.name AS category FROM template_versions v JOIN templates t ON t.id=v.template_id JOIN categories c ON c.id=t.category_id WHERE v.status='published' AND t.status='active' ORDER BY v.title, v.id LIMIT $1 OFFSET $2", limit, offset)
