# CodeCrafters AI API

Base URLs:

- auth-service: `http://localhost:8081`
- task-service: `http://localhost:8082`
- ai-service: `http://localhost:8083`

Все защищённые endpoints используют `Authorization: Bearer <accessToken>`.

## Auth

- `POST /api/auth/register` - регистрация `TEACHER` или `STUDENT`.
- `POST /api/auth/login` - вход и получение JWT.
- `GET /api/auth/me` - текущий пользователь.

JWT содержит `sub`, `email`, `role`.

## Teacher API

### Задания

- `POST /api/tasks/generate` - сгенерировать задание. `teacherId` берётся из JWT.
- `GET /api/tasks` - список заданий текущего преподавателя.
- `GET /api/tasks/{id}` - задание текущего преподавателя.
- `POST /api/tasks/{taskId}/assign-groups` - назначить задание группам.

Пример назначения:

```json
{
  "groupIds": ["00000000-0000-0000-0000-000000000000"]
}
```

### Группы

- `POST /api/groups` - создать группу.
- `GET /api/groups` - группы текущего преподавателя.
- `GET /api/groups/{groupId}` - группа текущего преподавателя.
- `POST /api/groups/{groupId}/students` - добавить ученика по email.
- `GET /api/groups/{groupId}/students` - ученики группы.

Пример создания группы:

```json
{
  "name": "Python 10-12 лет",
  "description": "Группа начинающих учеников по Python"
}
```

Пример добавления ученика:

```json
{
  "email": "student@example.com"
}
```

### Проверка решений

- `GET /api/tasks/{taskId}/submissions` - решения по заданию текущего преподавателя.
- `PATCH /api/submissions/{submissionId}/review` - проверить решение.

Пример проверки:

```json
{
  "status": "REVIEWED",
  "grade": 5,
  "teacherComment": "Хорошее решение, но можно улучшить оформление кода."
}
```

`grade` и `teacherComment` могут быть `null`.

## Student API

- `GET /api/student/tasks` - задания, назначенные группам текущего ученика.
- `GET /api/student/tasks/{taskId}` - назначенное ученику задание.
- `POST /api/student/tasks/{taskId}/submit` - отправить или обновить решение.

Пример отправки:

```json
{
  "answerText": "Моё решение задания..."
}
```

Student responses не содержат служебные поля `prompt`, `teacherId`, `aiProvider` и `teacherSolution`.

## AI Service

- `POST /api/ai/generate-task` - генерация задания в `mock` или `gigachat` режиме.

Публичный контракт endpoint не меняется.

Режимы:

- `AI_MODE=mock` - локальный mock без внешнего API.
- `AI_MODE=gigachat` - OAuth-запрос к GigaChat и вызов chat completions.

GigaChat OAuth:

- `POST https://ngw.devices.sberbank.ru:9443/api/v2/oauth`
- `Content-Type: application/x-www-form-urlencoded`
- `Accept: application/json`
- `RqUID: <uuid4>`
- `Authorization: Basic <GIGACHAT_AUTH_KEY>`
- body: `scope=<GIGACHAT_SCOPE>`

Chat completions:

- `POST https://gigachat.devices.sberbank.ru/api/v1/chat/completions`
- response content читается из `choices[0].message.content`

`ai-service` кэширует `access_token` в памяти и обновляет его заранее, за 60 секунд до `expires_at`.

Для локального Docker-запуска можно использовать `GIGACHAT_VERIFY_SSL=false`, если Java-контейнер не доверяет TLS-цепочке GigaChat. В production рекомендуется включить SSL-проверку и настроить truststore.

Fallback:

- если ключ не задан;
- если OAuth завершился ошибкой;
- если chat completions вернул ошибку;
- если модель вернула невалидный JSON;
- если обязательные поля отсутствуют.

При `AI_MOCK_ENABLED=true` во всех этих случаях возвращается mock-ответ.

### generatedContent by taskType

Все типы содержат базовые поля:

```json
{
  "title": "string",
  "taskType": "PRACTICE",
  "description": "string",
  "instructions": "string",
  "inputData": "string",
  "expectedResult": "string",
  "hints": ["string"],
  "difficulty": "BEGINNER",
  "topic": "string"
}
```

`PRACTICE` добавляет:

```json
{
  "starterCode": "string",
  "requirements": ["string"],
  "example": {
    "input": "string",
    "output": "string"
  },
  "teacherSolution": "string"
}
```

`TEST` добавляет:

```json
{
  "questions": [
    {
      "question": "string",
      "options": ["string", "string", "string", "string"],
      "correctAnswer": "string",
      "explanation": "string"
    }
  ],
  "passingScore": "string"
}
```

`BUG_FIX` добавляет:

```json
{
  "buggyCode": "string",
  "bugDescription": "string",
  "expectedFixedBehavior": "string",
  "teacherSolution": "string",
  "commonMistakes": ["string"]
}
```

`MINI_PROJECT` добавляет:

```json
{
  "projectGoal": "string",
  "functionalRequirements": ["string"],
  "steps": ["string"],
  "acceptanceCriteria": ["string"],
  "extensionIdeas": ["string"]
}
```

`HOMEWORK_WITH_CRITERIA` добавляет:

```json
{
  "homeworkTasks": ["string"],
  "evaluationCriteria": [
    {
      "criterion": "string",
      "points": 1
    }
  ],
  "maxScore": 10,
  "teacherNotes": "string"
}
```

Student API скрывает из `generatedContent` поля `teacherSolution`, `teacherNotes`, `correctAnswer`, `explanation`, а также служебные `prompt`, `teacherId`, `aiProvider`.

## Legacy

Legacy endpoints `/api/assignments/*` и legacy-таблицы не расширяются новой функциональностью.
