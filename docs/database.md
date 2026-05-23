# Схема базы данных

База CodeCrafters AI работает на PostgreSQL, миграции применяются Flyway.

## Миграции

- `V1__create_mvp_schema.sql` - роли, пользователи, группы, участники групп и legacy-модель заданий.
- `V2__seed_roles.sql` - роли `TEACHER` и `STUDENT`.
- `V3__add_generated_tasks_ai_generation_requests.sql` - актуальная AI-модель `generated_tasks` и `ai_generation_requests`.
- `V4__add_task_assignments_and_submissions.sql` - назначение `generated_tasks` группам и решения учеников.
- `V5__add_answer_url_deadline_needs_revision.sql` - поле `answer_url` в решениях, `deadline` в назначениях, статус `NEEDS_REVISION` в решениях.

## Актуальные таблицы

### users / roles

`auth-service` хранит пользователей и роли. `task-service` использует эти таблицы для проверки, что добавляемый в группу пользователь существует и имеет роль `STUDENT`.

### groups

Группы преподавателей.

Ключевые поля:

- `id`
- `teacher_id`
- `name`
- `description`
- `invite_code`
- `created_at`
- `updated_at`

Преподаватель видит и изменяет только группы, где `groups.teacher_id` равен id из JWT.

### group_members

Ученики в группах.

Ключевые поля:

- `group_id`
- `student_id`
- `joined_at`

Ограничение `UNIQUE (group_id, student_id)` не позволяет добавить ученика в одну группу дважды.

### generated_tasks

Актуальная таблица сгенерированных заданий преподавателя.

Ключевые поля:

- `teacher_id`
- `subject_area`
- `topic`
- `difficulty`
- `grade_level`
- `task_type`
- `prompt`
- `generated_content`
- `ai_provider`
- `status`

Новая образовательная логика строится только вокруг `generated_tasks`.

### task_group_assignments

Назначение сгенерированного задания группе.

Поля:

- `id`
- `task_id` -> `generated_tasks.id`
- `group_id` -> `groups.id`
- `assigned_at`
- `deadline` — необязательный срок сдачи (null = бессрочное)

Ограничение `UNIQUE (task_id, group_id)` предотвращает повторное назначение одного задания одной группе.

### task_submissions

Последнее решение ученика по заданию.

Поля:

- `id`
- `task_id` -> `generated_tasks.id`
- `student_id` -> `users.id`
- `answer_text`
- `answer_url` — необязательная ссылка на внешний ресурс (Scratch, Replit, CodePen и т.д.)
- `status`: `SUBMITTED`, `REVIEWED` или `NEEDS_REVISION`
- `teacher_comment`
- `grade`
- `submitted_at`
- `reviewed_at`
- `updated_at`

Ограничение `UNIQUE (task_id, student_id)` означает, что повторная отправка решения обновляет существующую запись.

## Legacy

Legacy-таблицы остаются в БД, но новая логика их не расширяет:

- `assignment_generation_requests`
- `generated_assignments`
- `assignment_groups`
- `assignment_submissions`
