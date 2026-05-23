# Выжимка по текущему состоянию CodeCrafters AI

## Что реализовано

Проект остаётся monorepo с сервисами:

- `auth-service` - регистрация, вход, JWT, роли `TEACHER/STUDENT`.
- `ai-service` - mock-генерация и реальная GigaChat-интеграция с OAuth/fallback.
- `task-service` - генерация, группы, назначения, ученический кабинет и проверка решений.
- `frontend` - HTML/CSS/JS интерфейс без React.

## Основной рабочий сценарий

1. Преподаватель регистрируется или входит.
2. Ученик регистрируется или входит.
3. Преподаватель создаёт группу.
4. Преподаватель добавляет ученика в группу по email.
5. Преподаватель генерирует задание.
6. Преподаватель назначает `generated_task` группе.
7. Ученик видит назначенное задание.
8. Ученик отправляет решение.
9. Преподаватель видит решение.
10. Преподаватель ставит оценку и комментарий.

## База данных

Миграции:

- `V1__create_mvp_schema.sql`
- `V2__seed_roles.sql`
- `V3__add_generated_tasks_ai_generation_requests.sql`
- `V4__add_task_assignments_and_submissions.sql`

Актуальная образовательная модель:

- `groups`
- `group_members`
- `generated_tasks`
- `task_group_assignments`
- `task_submissions`

Legacy-модель `/api/assignments` не расширяется.

## Backend API

Добавлены endpoints:

- `POST /api/groups`
- `GET /api/groups`
- `GET /api/groups/{groupId}`
- `POST /api/groups/{groupId}/students`
- `GET /api/groups/{groupId}/students`
- `PATCH /api/tasks/{id}/content`
- `POST /api/tasks/{taskId}/assign-groups`
- `POST /api/student/groups/join`
- `GET /api/student/groups`
- `GET /api/student/tasks`
- `GET /api/student/tasks/{taskId}`
- `POST /api/student/tasks/{taskId}/submit`
- `GET /api/tasks/{taskId}/submissions`
- `PATCH /api/submissions/{submissionId}/review`

Все новые endpoints работают через JWT и проверяют роль.

## Frontend

Frontend разделён на:

- `frontend/index.html`
- `frontend/styles.css`
- `frontend/app.js`

После входа dashboard зависит от роли:

- `TEACHER`: `Генерация`, `Мои задания`, `Группы`, `Проверка решений`.
- `STUDENT`: список назначенных заданий, детальный просмотр и отправка решения.

## Проверки

После backend-этапов выполнялась сборка `task-service` через Docker:

- после групп;
- после назначения группам;
- после student endpoints;
- после submissions/review.

Также проверена сборка frontend-контейнера после разделения файлов и расширения UI.

## AI Service

`ai-service` поддерживает:

- `AI_MODE=mock` - локальный mock.
- `AI_MODE=gigachat` - OAuth token request и chat completions GigaChat API.

Для GigaChat реализовано:

- `Authorization: Basic <GIGACHAT_AUTH_KEY>` для OAuth;
- `RqUID` как UUID v4 на каждый OAuth-запрос;
- кэширование `access_token` в памяти;
- обновление токена за 60 секунд до `expires_at`;
- dev-настройка `GIGACHAT_VERIFY_SSL=false` для локального Docker-окружения, если контейнер не доверяет TLS-цепочке GigaChat;
- чтение ответа из `choices[0].message.content`;
- извлечение JSON из markdown-блока при необходимости;
- валидация обязательных полей;
- mock fallback при ошибках, если `AI_MOCK_ENABLED=true`.
- type-specific prompts через `AssignmentPromptFactory` для `PRACTICE`, `TEST`, `BUG_FIX`, `MINI_PROJECT`, `HOMEWORK_WITH_CRITERIA`;
- type-specific mock responses для всех типов заданий;
- сохранение дополнительных JSON-полей в `generated_tasks.generated_content`;
- student-safe sanitization скрывает `teacherSolution`, `teacherNotes`, `correctAnswer`, `explanation`.

## Технические долги

- Frontend остаётся SPA на чистом JS без роутера и сборщика.
- Нет автоматических backend/frontend тестов.
- Legacy endpoints `/api/assignments/*` пока остаются для совместимости, но не развиваются.
