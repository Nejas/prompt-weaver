import asyncio
import json
import os
from pathlib import Path
import grpc
from generated import rules_pb2, rules_pb2_grpc


class RulesService(rules_pb2_grpc.RulesServiceServicer):
    def __init__(self, path):
        self.path = Path(path)

    async def GetRuleSet(self, request, context):
        try:
            data = json.loads(self.path.read_text(encoding='utf-8'))
        except (OSError, ValueError):
            await context.abort(grpc.StatusCode.UNAVAILABLE, 'Rules storage unavailable')
        item = next((item for item in data if item.get('id') == request.id and item.get('status') == 'published'), None)
        if item is None:
            await context.abort(grpc.StatusCode.NOT_FOUND, 'Published rule set not found')
        return rules_pb2.RuleSetResponse(id=item['id'], name=item['name'], rules=[rules_pb2.Rule(**rule) for rule in item['rules']])


async def main():
    server = grpc.aio.server()
    rules_pb2_grpc.add_RulesServiceServicer_to_server(RulesService(os.getenv('RULES_FILE', 'rules_service/rule_sets.json')), server)
    server.add_insecure_port('[::]:50053')
    await server.start()
    try:
        await server.wait_for_termination()
    finally:
        await server.stop(5)


if __name__ == '__main__':
    asyncio.run(main())
