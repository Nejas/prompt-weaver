FROM python:3.12-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY . .
RUN mkdir -p generated && touch generated/__init__.py && python -m grpc_tools.protoc -Icontracts --python_out=generated --grpc_python_out=generated contracts/prompt.proto contracts/identity.proto
ENV PYTHONPATH=/app:/app/generated
