# Платформа шаблонов промптов и адаптивного обучения

Дипломный проект: платформа для подготовки запросов к языковым моделям с помощью шаблонов и персонального контекста, с дальнейшим развитием адаптивного обучения. Проект находится в разработке; сейчас доступен начальный серверный прототип.

## Что реализовано

- Сборка текста промпта из опубликованной версии шаблона с проверкой параметров, с правилами или без них.
- Независимый сервис опубликованных наборов правил и встраиваемая библиотека `prompt_engine`.
- Регистрация и вход, обновление и отзыв сессии, получение текущего пользователя.
- HTTP API на FastAPI и взаимодействие внутренних сервисов через gRPC.
- Схемы PostgreSQL для пользователей, ролей, шаблонов и будущей истории.
- Модульные тесты и отдельный интеграционный тест авторизации.

Чат, персонализация, учебный модуль и сохранение истории пока не реализованы. Gateway умеет передать собранный промпт языковой модели через Bifrost.

## Архитектура

```text
Клиент (планируется) → HTTP Gateway → Prompt Service → PostgreSQL (prompts)
                                  → Identity Service → PostgreSQL (identity)
                                  → Rules Service → JSON-хранилище правил
```

Стек: Python, FastAPI, gRPC, PostgreSQL и Docker Compose. Redis предусмотрен в окружении, но сервисами пока не используется.

Пользователь будет самостоятельно выбирать шаблон в каталоге или чате. API каталога предоставляет опубликованные версии; интерфейс ещё не реализован. Совместный API может использовать явно указанный набор правил или связь версии шаблона с набором, заданную в конфигурации платформы. В дальнейшем платформа добавит разрешённый персональный контекст и передаст запрос модели через отдельный адаптер. Интерфейс просмотра и редактирования, персонализация и адаптер пока запланированы.

Правила сценариев могут напоминать skills, `AGENTS.md` и rules, но платформа сама отвечает за выбор и передачу инструкций, не полагаясь на поддержку конкретного механизма моделью. Пользовательский ввод и персональный контекст рассматриваются как данные.

## Структура базы данных

В PostgreSQL используются две отдельные базы. Диаграммы показывают основные поля и связи; полный состав полей и ограничений находится в `infrastructure/database/init` и `infrastructure/database/migrations`.

**База `identity`** — пользователи, роли и сессии:

```mermaid
erDiagram
    users ||--o{ user_roles : has
    roles ||--o{ user_roles : assigned
    users ||--o{ auth_sessions : owns

    users {
        uuid id PK
        text email
        text password_hash
        text status
    }
    roles {
        text code PK
        text description
    }
    user_roles {
        uuid user_id PK,FK
        text role_code PK,FK
    }
    auth_sessions {
        uuid id PK
        uuid user_id FK
        bytea access_token_hash
        bytea refresh_token_hash
        timestamptz access_expires_at
        timestamptz refresh_expires_at
        timestamptz revoked_at
    }
```

**База `prompts`** — каталог, версии шаблонов и таблица для будущей истории:

```mermaid
erDiagram
    categories ||--o{ templates : contains
    templates ||--o{ template_versions : has
    template_versions ||--o{ template_parameters : defines
    template_versions ||--o{ prompt_runs : used_in

    categories {
        uuid id PK
        text name
        text description
    }
    templates {
        uuid id PK
        uuid category_id FK
        text slug
        text status
    }
    template_versions {
        uuid id PK
        uuid template_id FK
        integer version_number
        text title
        text body
        text status
        timestamptz published_at
    }
    template_parameters {
        uuid id PK
        uuid template_version_id FK
        text name
        text label
        text type
        boolean required
        integer max_length
        jsonb options
        integer display_order
    }
    prompt_runs {
        uuid id PK
        uuid user_id
        uuid template_version_id FK
        jsonb parameters
        text result
        timestamptz created_at
    }
```

`prompt_runs.user_id` содержит идентификатор пользователя из отдельной базы `identity`, поэтому внешнего ключа между базами нет. Таблица предусмотрена схемой, но сохранение истории пока не реализовано. Наборы правил и связи сценариев сейчас хранятся в JSON-файлах, а не в PostgreSQL.

## Локальный запуск

Нужны Docker и Docker Compose. Конфигурация предназначена для локальной разработки.

1. Подготовьте пользователей PostgreSQL и права: Prompt Service нужны права SELECT на таблицы каталога в базе `prompts`; Identity Service — необходимые права на `users`, `roles`, `user_roles` и `auth_sessions` в базе `identity`. Отдельные учётные записи сервисов автоматически не создаются.
2. Задайте переменные окружения `PROMPT_DB_USER`, `PROMPT_DB_PASSWORD`, `IDENTITY_DB_USER`, `IDENTITY_DB_PASSWORD`. В Compose они передаются сервисам как `DB_USER` и `DB_PASSWORD`. Для подключения физического телефона к локальному Gateway дополнительно задайте `GATEWAY_BIND_ADDRESS` равным IPv4-адресу компьютера в той же Wi-Fi-сети; по умолчанию Gateway слушает только `127.0.0.1`.
3. Из этой папки выполните `docker compose up --build`. HTTP API и его документация доступны по адресу `http://localhost:8000/docs`.
4. При необходимости загрузите пример из `infrastructure/database/demo.sql` в базу `prompts` один раз: автоматически он не применяется.

При первом запуске с новым томом скрипты `infrastructure/database/init` создают базы и таблицы. Для существующего тома они повторно не выполняются; для существующей базы Identity предусмотрены однократные миграции в `infrastructure/database/migrations/identity`. Не удаляйте существующий том ради повторной инициализации: он может содержать данные. Контейнерный и локальный PostgreSQL — разные экземпляры.

Перед запуском версии с обязательными именем и фамилией в существующей базе Identity один раз примените `infrastructure/database/migrations/identity/002_user_names.sql`:

```powershell
docker compose exec -T postgres psql -U app -d identity -f /app-migrations/identity/002_user_names.sql
```

Миграция сохраняет старые учётные записи, но требует оба поля для всех новых регистраций.

Инструкция требует предварительной настройки пользователей и прав; автоматизированная процедура подготовки пока не предоставлена. Пароль PostgreSQL в Compose предназначен только для локальной разработки. Для реального развёртывания потребуются защита внутренних соединений и авторизация, ограничение частоты входа и безопасная доставка токенов.

## API

Основная операция сборки — `POST /api/v1/prompts/compose`. Она принимает идентификатор опубликованной версии шаблона и словарь строковых параметров, возвращая готовый текст. Также доступны операции регистрации, входа, обновления и отзыва сессии и получения текущего пользователя; точные форматы описаны в `/docs` после запуска.

`GET /api/v1/rule-sets/{rule_set_id}` возвращает опубликованный набор правил независимо от шаблонов. `POST /api/v1/prompts/compose-with-rules` принимает `template_version_id`, `parameters` и `rule_set_id`; если набор недоступен или отсутствует, запрос завершается ошибкой. Пример набора `teaching-basics-v1` хранится в `services/rules_service/rule_sets.json`. Это начальное файловое хранилище без API редактирования и автоматических версий; для сохранения изменений между пересозданиями контейнера необходимо настроить внешний файл или том.

`POST /api/v1/prompts/generate` требует заголовок `Authorization: Bearer <access_token>`. Он принимает `template_version_id`, `parameters`, необязательный `rule_set_id` и `message`. Gateway составляет системную инструкцию из шаблона и правил, затем вызывает Bifrost. Выбор модели остаётся серверной настройкой `BIFROST_MODEL` (по умолчанию `gemini/gemini-3.6-flash`), поэтому клиент не может направить запрос к произвольному провайдеру. Ответ содержит текст модели, фактически использованную модель и числовую статистику токенов, если её вернул провайдер.

`GET /api/v1/templates` возвращает опубликованные версии активных шаблонов (параметры `limit` от 1 до 100 и `offset`), а `GET /api/v1/templates/{version_id}` — описание, текст и параметры. `POST /api/v1/prompts/compose-scenario` принимает `template_version_id` и `parameters` и использует обязательный набор правил из файла `services/gateway/scenarios.json`. Для демонстрационной версии настроен набор `teaching-basics-v1`; отсутствие связи или набора приводит к ошибке. Другой файл можно указать через `SCENARIOS_FILE`. Это конфигурация платформы, а не автоматический подбор: для новой версии необходимо явно добавить связь. Обычная сборка без правил остаётся доступной через `/api/v1/prompts/compose`.

Ядро можно установить отдельно из этой папки командой `pip install .` и использовать без сервисов: `from prompt_engine import Parameter, PromptTemplate, Rule, build_prompt`. В `build_prompt` правила необязательны. Правила являются текстовыми инструкциями, а не исполняемыми skills; системные ограничения должны обеспечиваться кодом платформы.

## Дальнейшее развитие

- Управление версиями шаблонов и интерфейс каталога.
- Клиентский интерфейс и подключение языковой модели.
- Управление правилами и их версиями, настройки пользователя и контекстная персонализация.
- Учебные задания, учёт прогресса и адаптация следующих шагов.
- История взаимодействий и инструменты для сравнения результатов экспериментов.

## Тестирование

Команды ниже выполняются из `backend`. Установите зависимости из `requirements.txt`. Для тестов сервисов необходимы сгенерированные Python-модули gRPC: создайте каталог `generated` и выполните:

```powershell
New-Item -ItemType Directory -Force generated
python -m grpc_tools.protoc -Icontracts --python_out=generated --grpc_python_out=generated contracts/prompt.proto contracts/identity.proto contracts/rules.proto
```

```powershell
python -m pytest -m "not integration"  # независимые тесты
python -m pytest -m integration          # взаимодействие компонентов и opt-in тесты БД
python -m pytest                        # все доступные тесты
```

Тесты PostgreSQL по умолчанию пропускаются. Для проверки каталога подготовьте отдельную пустую базу `prompt_integration_test`, задайте `PROMPT_TEST_DSN`, например `postgresql://user:password@localhost/prompt_integration_test`, и запустите интеграционные тесты. Пользователю нужны права создавать и удалять схемы: тест создаёт временную схему, применяет схему каталога и удаляет её после проверки. Проверяется имя базы, чтобы не запускать этот сценарий на рабочей базе.

Существующий тест авторизации включается переменной `IDENTITY_TEST_DB=identity_integration_test`. Он подключается к локальной базе `identity_integration_test` пользователем `postgres` и требует предварительно применённую схему identity и сессий. Используйте отдельную тестовую базу: тест создаёт в ней пользователя и не удаляет его после выполнения. Оба теста БД требуют работающий PostgreSQL; запуск прочих тестов от него не зависит.

Интеграционные проверки включают реальный локальный обмен по gRPC для сервисов шаблонов и правил. Некоторые другие тесты используют подставные сервисы; тесты с PostgreSQL требуют настройки отдельных баз, как описано выше.

### REST API и Postman CLI

Коллекция Postman автоматически проверяет авторизацию, каталог шаблонов, правила и сборку промпта через HTTP Gateway. Установите Postman CLI и запускайте коллекцию только против отдельного тестового сервера с изолированными базами и загруженным демонстрационным шаблоном:

```powershell
postman collection run tests/postman/prompt-weaver.postman_collection.json --environment tests/postman/local.postman_environment.json
```

Каждый запуск создаёт тестовую учётную запись, которая остаётся в базе. Требования к окружению и подготовка описаны в [инструкции Postman](tests/postman/POSTMAN_README.md). Локальная коллекция не требует API-ключа Postman.

## Bifrost и Gemini

В Compose предусмотрен отдельный сервис Bifrost. Он доступен другим сервисам по внутреннему адресу `http://bifrost:8080`; порт на компьютер не опубликован. Gateway обращается к его OpenAI-совместимому маршруту `/v1/chat/completions` через `BIFROST_URL` и не передаёт API-ключ клиентскому приложению.

Конфигурация `infrastructure/bifrost/config.json` задаёт провайдера Gemini и модель `gemini-3.6-flash`. Для работы с моделью задайте `GEMINI_API_KEY` в окружении или в локальном `.env` (этот файл исключён из Git). Файловая конфигурация загружается при старте Bifrost; после её изменения сервис необходимо перезапустить. Не передавайте ключ в чат и не добавляйте его в репозиторий.

Без ключа вызовы Gemini не работают. Дополнительно можно задать `BIFROST_MODEL`, `BIFROST_TIMEOUT_SECONDS`, `GENERATION_RATE_LIMIT` и `GENERATION_RATE_WINDOW_SECONDS`. Ограничение частоты действует на пользователя в рамках одного процесса Gateway и предназначено для локального прототипа; для нескольких экземпляров сервиса его следует перенести в общее хранилище. Доступность образа, запуск сервиса и подключение к модели необходимо проверить в Docker после настройки ключа.
