import asyncio
import os
import uuid
from pathlib import Path

import asyncpg
import pytest
from services.prompt_service.repository import get_published_template, list_published_templates


@pytest.mark.skipif(not os.getenv('PROMPT_TEST_DSN'), reason='Set PROMPT_TEST_DSN for dedicated prompt_integration_test database')
def test_published_catalog_and_schema_constraints():
    async def exercise():
        connection = await asyncpg.connect(os.environ['PROMPT_TEST_DSN'])
        schema = 'test_' + uuid.uuid4().hex
        try:
            if await connection.fetchval('SELECT current_database()') != 'prompt_integration_test':
                pytest.fail('PROMPT_TEST_DSN must point to prompt_integration_test')
            await connection.execute(f'CREATE SCHEMA {schema}')
            await connection.execute(f'SET search_path TO {schema}')
            sql = Path('infrastructure/database/init/002-schema.sql').read_text(encoding='utf-8').split('\\connect prompts', 1)[1]
            await connection.execute(sql)
            category, active, archived, published, draft, hidden = [uuid.uuid4() for _ in range(6)]
            await connection.execute("INSERT INTO categories(id,name) VALUES($1,'category')", category)
            await connection.execute("INSERT INTO templates(id,category_id,slug,status) VALUES($1,$3,'active','active'),($2,$3,'archived','archived')", active, archived, category)
            await connection.execute("INSERT INTO template_versions(id,template_id,version_number,title,body,status,published_at) VALUES($1,$4,1,'Visible','{{x}}','published',now()),($2,$4,2,'Draft','text','draft',NULL),($3,$5,1,'Hidden','text','published',now())", published, draft, hidden, active, archived)
            await connection.execute("INSERT INTO template_parameters(id,template_version_id,name,label,type) VALUES($1,$2,'x','X','text')", uuid.uuid4(), published)
            class Pool:
                def acquire(self):
                    class Acquired:
                        async def __aenter__(self): return connection
                        async def __aexit__(self, *args): pass
                    return Acquired()
            pool = Pool()
            assert [row['id'] for row in await list_published_templates(pool, 10, 0)] == [published]
            assert await get_published_template(pool, str(draft)) is None
            assert await get_published_template(pool, str(hidden)) is None
            assert await get_published_template(pool, 'not-a-uuid') is None
            version, params = await get_published_template(pool, str(published))
            assert version['id'] == published and params[0]['name'] == 'x'
            with pytest.raises(asyncpg.UniqueViolationError):
                await connection.execute("INSERT INTO template_versions(id,template_id,version_number,title,body,status,published_at) VALUES($1,$2,3,'Another','text','published',now())", uuid.uuid4(), active)
        finally:
            if await connection.fetchval('SELECT current_database()') == 'prompt_integration_test':
                await connection.execute(f'DROP SCHEMA IF EXISTS {schema} CASCADE')
            await connection.close()
    asyncio.run(exercise())
