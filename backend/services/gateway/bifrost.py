"""Small, typed boundary for Bifrost's OpenAI-compatible chat API."""

from dataclasses import dataclass

import httpx


class BifrostError(Exception):
    """A safe error that can be returned by the public gateway."""

    def __init__(self, status_code: int, detail: str):
        super().__init__(detail)
        self.status_code = status_code
        self.detail = detail


@dataclass(frozen=True)
class BifrostCompletion:
    content: str
    model: str
    usage: dict[str, int | float]


class BifrostClient:
    def __init__(self, base_url: str, timeout_seconds: float):
        self._client = httpx.AsyncClient(
            base_url=base_url.rstrip('/'),
            timeout=httpx.Timeout(timeout_seconds),
        )

    async def close(self) -> None:
        await self._client.aclose()

    async def create_chat_completion(
        self,
        *,
        model: str,
        system_prompt: str,
        user_message: str,
    ) -> BifrostCompletion:
        try:
            response = await self._client.post(
                '/v1/chat/completions',
                json={
                    'model': model,
                    'messages': [
                        {'role': 'system', 'content': system_prompt},
                        {'role': 'user', 'content': user_message},
                    ],
                },
            )
            response.raise_for_status()
        except httpx.TimeoutException as exc:
            raise BifrostError(504, 'Model gateway timed out') from exc
        except httpx.HTTPStatusError as exc:
            upstream_status = exc.response.status_code
            if upstream_status == 408:
                raise BifrostError(504, 'Model gateway timed out') from exc
            if upstream_status == 429 or upstream_status >= 500:
                raise BifrostError(503, 'Model gateway is temporarily unavailable') from exc
            raise BifrostError(502, 'Model gateway rejected the request') from exc
        except httpx.RequestError as exc:
            raise BifrostError(503, 'Model gateway is unavailable') from exc

        try:
            payload = response.json()
            content = payload['choices'][0]['message']['content']
            response_model = payload['model']
            usage = payload.get('usage') or {}
        except (IndexError, KeyError, TypeError, ValueError) as exc:
            raise BifrostError(502, 'Model gateway returned an invalid response') from exc

        if not isinstance(content, str) or not content.strip() or not isinstance(response_model, str):
            raise BifrostError(502, 'Model gateway returned an invalid response')
        if not isinstance(usage, dict):
            usage = {}

        safe_usage = {
            key: value
            for key, value in usage.items()
            if isinstance(key, str) and isinstance(value, (int, float))
        }
        return BifrostCompletion(content=content, model=response_model, usage=safe_usage)
