# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Язык общения

Всегда отвечай на русском языке. Документацию и комментарии в коде пиши на русском.

## Запуск проекта

```bash
docker compose up --build
```

Адреса сервисов после запуска:

| Сервис        | URL                      |
|---------------|--------------------------|
| frontend      | http://localhost:5173    |
| auth-service  | http://localhost:8081    |
| task-service  | http://localhost:8082    |
| ai-service    | http://localhost:8083    |
| PostgreSQL    | localhost:5432           |

Сборка одного сервиса локально (для быстрой проверки компиляции):

```bash
cd services/task-service && mvn -q -DskipTests package
```

## Настройка AI-сервиса

Скопируй `.env.example` в `.env` и задай переменные окружения до `docker compose up`:

- `AI_MODE=mock` (по умолчанию) — возвращает локальный mock, внешние вызовы не делаются.
- `AI_MODE=gigachat` + `GIGACHAT_AUTH_KEY=<ключ>` — запрашивает OAuth-токен GigaChat и вызывает chat completions.
- `AI_MOCK_ENABLED=true` (по умолчанию) — включает fallback на mock при ошибках GigaChat.
- `GIGACHAT_VERIFY_SSL=false` (по умолчанию) — нужен в локальном Docker, если контейнер не доверяет TLS-цепочке GigaChat.

## Архитектура

### Микросервисы

Три Spring Boot сервиса (Java 21, Maven) с общей базой данных PostgreSQL:

- **auth-service** — регистрация, вход, выдача JWT. Владеет таблицами `users` и `roles`.
- **task-service** — группы, генерация заданий (проксирует запросы в ai-service), назначение заданий группам, решения учеников, проверка преподавателем. Читает таблицы `users`/`roles`, которыми владеет auth-service.
- **ai-service** — stateless; формирует промпты и вызывает GigaChat (или возвращает mock). Базы данных нет.

Все три сервиса валидируют JWT независимо, используя один и тот же `JWT_SECRET`. Payload токена содержит `sub` (userId), `email` и `role`.

### Миграции базы данных

Flyway запускается контейнером `db-migrations` при старте. Все SQL-файлы миграций находятся в `infrastructure/postgres/migrations/` — не внутри директорий отдельных сервисов. Новые миграции добавлять туда же.

Актуальные таблицы (V3/V4): `generated_tasks`, `task_group_assignments`, `task_submissions`, `groups`, `group_members`.

Legacy-таблицы (`generated_assignments`, `assignment_generation_requests`, `assignment_groups`, `assignment_submissions`) остались с V1 и **не расширяются новой функциональностью**.

### Взаимодействие между сервисами

`task-service` синхронно вызывает `ai-service` по `POST /api/ai/generate-task` через `AiGenerationClient`. URL настраивается через `app.ai-service-url` (env: `AI_SERVICE_URL`).

### Frontend

Чистый HTML/CSS/JS — без React и сборщика. Три файла: `frontend/index.html`, `frontend/styles.css`, `frontend/app.js`. В Docker раздаётся через nginx. Интерфейс ветвится по роли из JWT после входа:

- `TEACHER`: вкладки «Генерация», «Мои задания», «Группы», «Проверка решений».
- `STUDENT`: список назначенных заданий, детальный просмотр, форма отправки решения.

### Система AI-промптов

`ai-service` использует `AssignmentPromptFactory` для формирования промптов под каждый из пяти типов заданий: `PRACTICE`, `TEST`, `BUG_FIX`, `MINI_PROJECT`, `HOMEWORK_WITH_CRITERIA`. Каждый тип расширяет базовый JSON `generatedContent` дополнительными полями. В ответах для ученика удаляются поля `teacherSolution`, `teacherNotes`, `correctAnswer`, `explanation`.

## Поддержание документации в актуальном состоянии

После любых изменений в коде обязательно обнови соответствующие документы:

- **Новый или изменённый endpoint** → обнови `docs/api.md` и список в `README.md`.
- **Изменение схемы БД** (новая миграция, новая таблица/колонка) → обнови `docs/database.md`.
- **Изменение архитектуры или ключевых решений** → обнови раздел «Архитектура» в этом файле (`CLAUDE.md`).
- **Изменение сценария работы** (новый шаг MVP-флоу, новая роль, новый статус) → обнови `README.md` и `docs/progress-summary.md`.
- **Изменение переменных окружения** → обнови раздел «Настройка AI-сервиса» в `CLAUDE.md` и `.env.example`.

Правило: документация обновляется **в том же ответе**, что и код — не откладывать на потом.

## Ключевые архитектурные решения

- **Единая БД**: все сервисы подключаются к одному PostgreSQL; изоляция логическая, не физическая.
- **Нет refresh-токенов**: только JWT access token; срок действия 120 минут.
- **task-service читает таблицы auth-service**: `UserAccountRepository` в task-service обращается к таблице `users` для проверки email при добавлении ученика в группу.
- **Решения — upsert**: ограничение `UNIQUE (task_id, student_id)` в `task_submissions` означает, что повторная отправка обновляет существующую запись, а не создаёт новую.
- **Группы принадлежат преподавателю**: все endpoints групп и заданий проверяют, что `teacher_id` в БД совпадает с subject из JWT.

