CREATE TABLE generated_tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    teacher_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subject_area VARCHAR(32) NOT NULL,
    topic VARCHAR(255) NOT NULL,
    difficulty VARCHAR(32) NOT NULL,
    grade_level VARCHAR(32) NOT NULL,
    task_type VARCHAR(64) NOT NULL,
    prompt TEXT NOT NULL,
    generated_content JSONB,
    ai_provider VARCHAR(32) NOT NULL DEFAULT 'MOCK',
    status VARCHAR(32) NOT NULL DEFAULT 'GENERATING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT generated_tasks_subject_area_check CHECK (subject_area IN ('SCRATCH', 'PYTHON', 'ALGORITHMS', 'HTML_CSS')),
    CONSTRAINT generated_tasks_difficulty_check CHECK (difficulty IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    CONSTRAINT generated_tasks_grade_level_check CHECK (grade_level IN ('AGE_7_9', 'AGE_10_12', 'AGE_13_15', 'AGE_16_17')),
    CONSTRAINT generated_tasks_task_type_check CHECK (
        task_type IN ('PRACTICE', 'TEST', 'BUG_FIX', 'MINI_PROJECT', 'HOMEWORK_WITH_CRITERIA')
    ),
    CONSTRAINT generated_tasks_ai_provider_check CHECK (ai_provider IN ('MOCK', 'GIGACHAT')),
    CONSTRAINT generated_tasks_status_check CHECK (status IN ('GENERATING', 'GENERATED', 'FAILED')),
    CONSTRAINT generated_tasks_topic_not_blank CHECK (length(trim(topic)) > 0),
    CONSTRAINT generated_tasks_prompt_not_blank CHECK (length(trim(prompt)) > 0),
    CONSTRAINT generated_tasks_content_required_when_generated CHECK (
        status <> 'GENERATED' OR generated_content IS NOT NULL
    )
);

CREATE TABLE ai_generation_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID NOT NULL REFERENCES generated_tasks(id) ON DELETE CASCADE,
    request_payload JSONB NOT NULL,
    response_payload JSONB,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ai_generation_requests_status_check CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED')),
    CONSTRAINT ai_generation_requests_response_required_on_success CHECK (
        status <> 'SUCCESS' OR response_payload IS NOT NULL
    ),
    CONSTRAINT ai_generation_requests_error_required_on_failure CHECK (
        status <> 'FAILED' OR error_message IS NOT NULL
    )
);

CREATE INDEX idx_generated_tasks_teacher_created_at ON generated_tasks(teacher_id, created_at DESC);
CREATE INDEX idx_generated_tasks_teacher_status ON generated_tasks(teacher_id, status);
CREATE INDEX idx_generated_tasks_subject_area ON generated_tasks(subject_area);
CREATE INDEX idx_ai_generation_requests_task_id ON ai_generation_requests(task_id);
CREATE INDEX idx_ai_generation_requests_status ON ai_generation_requests(status);

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_generated_tasks_updated_at
BEFORE UPDATE ON generated_tasks
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();
