import asyncio
import grpc
from generated import prompt_pb2, prompt_pb2_grpc
from prompt_service.composer import compose
from prompt_service.repository import create_pool, get_published_template
from prompt_service.validation import validate_parameters


class PromptService(prompt_pb2_grpc.PromptServiceServicer):
    def __init__(self, pool):
        self.pool = pool

    async def Compose(self, request, context):
        template = await get_published_template(self.pool, request.template_version_id)
        if not template:
            await context.abort(grpc.StatusCode.NOT_FOUND, 'Template version not found')
        try:
            version, definitions = template
            values = validate_parameters(definitions, dict(request.parameters))
            result = compose(version['body'], values, allow_empty=True)
        except ValueError as exc:
            await context.abort(grpc.StatusCode.INVALID_ARGUMENT, str(exc))
        return prompt_pb2.ComposeResponse(template_version_id=str(version['id']), prompt=result)


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
