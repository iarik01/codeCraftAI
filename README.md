# CodeCrafters AI

CodeCrafters AI - образовательный SaaS-сервис для преподавателей детских онлайн-школ программирования. MVP покрывает регистрацию, JWT-авторизацию, генерацию заданий, группы, назначение заданий ученикам, отправку решений и ручную проверку.

## Структура проекта

```text
.
|-- services/
|   |-- auth-service/
|   |-- task-service/
|   `-- ai-service/
|-- frontend/
|   |-- index.html
|   |-- styles.css
|   `-- app.js
|-- infrastructure/postgres/migrations/
|-- docs/
`-- shared/
```

## Локальный запуск

```bash
docker compose up --build
```

Доступные сервисы:

- frontend: `http://localhost:5173`
- auth-service: `http://localhost:8081`
- task-service: `http://localhost:8082`
- ai-service: `http://localhost:8083`
- PostgreSQL: `localhost:5432`

## AI-режимы

`ai-service` поддерживает два режима:

- `AI_MODE=mock` - всегда возвращает локальный mock-ответ.
- `AI_MODE=gigachat` - сначала получает OAuth access token GigaChat, затем вызывает chat completions.

Для локальной разработки fallback включён по умолчанию:

```env
AI_MOCK_ENABLED=true
```

Если `AI_MODE=gigachat`, но `GIGACHAT_AUTH_KEY` не задан, OAuth завершился ошибкой, chat completions недоступен или модель вернула невалидный JSON, сервис использует mock fallback при `AI_MOCK_ENABLED=true`.

Для локального Docker-окружения добавлена настройка:

```env
GIGACHAT_VERIFY_SSL=false
```

Она нужна, если контейнер не доверяет TLS-цепочке GigaChat и OAuth падает с ошибкой сертификата. Для production лучше настроить truststore и включить проверку SSL.

Пример настроек находится в `.env.example`. Реальные ключи не нужно коммитить.

AI-генерация учитывает тип задания:

- `PRACTICE` - практическое задание с кодом, требованиями, примером и решением преподавателя.
- `TEST` - тест с вопросами, вариантами ответов и проходным баллом.
- `BUG_FIX` - задание на поиск и исправление ошибки.
- `MINI_PROJECT` - мини-проект с требованиями, шагами и критериями готовности.
- `HOMEWORK_WITH_CRITERIA` - домашнее задание с критериями оценивания.

## Основной MVP-сценарий

1. Преподаватель и ученик регистрируются через frontend.
2. Преподаватель создаёт группу.
3. Преподаватель добавляет ученика в группу по email.
4. Преподаватель генерирует задание через `generated_tasks`.
5. Преподаватель назначает задание группе.
6. Ученик видит назначенное задание в своём кабинете.
7. Ученик отправляет решение.
8. Преподаватель открывает решения, ставит оценку и комментарий.

## Frontend UX

Frontend остаётся на HTML/CSS/JavaScript без React. Файлы разделены на `index.html`, `styles.css` и `app.js`.

После входа интерфейс ветвится по роли:

- `TEACHER`: вкладки `Генерация`, `Мои задания`, `Группы`, `Проверка решений`.
- `STUDENT`: кабинет ученика со списком назначенных заданий, детальным просмотром и формой отправки решения.

## API

Основные endpoints:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`
- `POST /api/tasks/generate`
- `GET /api/tasks`
- `GET /api/tasks/{id}`
- `POST /api/groups`
- `GET /api/groups`
- `POST /api/groups/{groupId}/students`
- `POST /api/tasks/{taskId}/assign-groups`
- `GET /api/student/tasks`
- `POST /api/student/tasks/{taskId}/submit`
- `GET /api/tasks/{taskId}/submissions`
- `PATCH /api/submissions/{submissionId}/review`

Подробности: `docs/api.md`.

## База данных

Актуальная модель построена вокруг `generated_tasks`.

Ключевые таблицы:

- `groups`, `group_members` - группы и ученики в группах.
- `generated_tasks` - сгенерированные задания.
- `task_group_assignments` - назначение generated task группе.
- `task_submissions` - последнее решение ученика по заданию.
- `ai_generation_requests` - журнал AI-генерации.

Legacy-таблицы `generated_assignments`, `assignment_generation_requests`, `assignment_groups`, `assignment_submissions` не расширяются.

## Документация

- `docs/api.md` - REST API.
- `docs/database.md` - схема БД.
- `docs/progress-summary.md` - текущее состояние и следующие шаги.
