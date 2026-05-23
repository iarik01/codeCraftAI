-- answerUrl: необязательная ссылка на внешний ресурс при отправке решения
ALTER TABLE task_submissions ADD COLUMN answer_url TEXT;

-- deadline: опциональный дедлайн при назначении задания группе
ALTER TABLE task_group_assignments ADD COLUMN deadline TIMESTAMPTZ;

-- Статус «Нужно доработать»: добавляем NEEDS_REVISION к допустимым статусам решений
ALTER TABLE task_submissions DROP CONSTRAINT task_submissions_status_check;
ALTER TABLE task_submissions ADD CONSTRAINT task_submissions_status_check
    CHECK (status IN ('SUBMITTED', 'REVIEWED', 'NEEDS_REVISION'));
