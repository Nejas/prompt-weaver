import asyncio
import json
import grpc
from generated import prompt_pb2, prompt_pb2_grpc
from prompt_engine import Parameter, PromptTemplate, build_prompt
from prompt_service.repository import create_pool, get_published_template


class PromptService(prompt_pb2_grpc.PromptServiceServicer):
    def __init__(self, pool):
        self.pool = pool

    async def Compose(self, request, context):
        template = await get_published_template(self.pool, request.template_version_id)
        if not template:
            await context.abort(grpc.StatusCode.NOT_FOUND, 'Template version not found')
        try:
            version, definitions = template
            params = [Parameter(name=p['name'], type=p['type'], required=p['required'], max_length=p['max_length'], options=json.loads(p['options']) if isinstance(p['options'], str) else p['options']) for p in definitions]
            result = build_prompt(PromptTemplate(version['body'], params), dict(request.parameters)).text
        except ValueError as exc:
            await context.abort(grpc.StatusCode.INVALID_ARGUMENT, str(exc))
        return prompt_pb2.ComposeResponse(template_version_id=str(version['id']), prompt=result)

    async def GetTemplate(self, request, context):
        template = await get_published_template(self.pool, request.template_version_id)
        if not template:
            await context.abort(grpc.StatusCode.NOT_FOUND, 'Template version not found')
        version, definitions = template
        params = []
        for p in definitions:
            options = json.loads(p['options']) if isinstance(p['options'], str) else p['options']
            params.append(prompt_pb2.ParameterDefinition(name=p['name'], type=p['type'], required=p['required'], max_length=p['max_length'] or 0, options=options or []))
        return prompt_pb2.TemplateResponse(id=str(version['id']), body=version['body'], parameters=params)


async def main():
    pool = await create_pool()
    server = grpc.aio.server()
    prompt_pb2_grpc.add_PromptServiceServicer_to_server(PromptService(pool), server)
    server.add_insecure_port('[::]:50051')
    try:
        await server.start()
        await server.wait_for_termination()
    finally:
        await server.stop(5)
        await pool.close()


if __name__ == '__main__':
    asyncio.run(main())
