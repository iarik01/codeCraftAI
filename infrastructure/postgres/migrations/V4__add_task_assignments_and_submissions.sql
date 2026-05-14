CREATE TABLE task_group_assignments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID NOT NULL REFERENCES generated_tasks(id) ON DELETE CASCADE,
    group_id UUID NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT task_group_assignments_task_group_unique UNIQUE (task_id, group_id)
);

CREATE TABLE task_submissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID NOT NULL REFERENCES generated_tasks(id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    answer_text TEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'SUBMITTED',
    teacher_comment TEXT,
    grade INTEGER,
    submitted_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    reviewed_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT task_submissions_task_student_unique UNIQUE (task_id, student_id),
    CONSTRAINT task_submissions_status_check CHECK (status IN ('SUBMITTED', 'REVIEWED')),
    CONSTRAINT task_submissions_answer_not_blank CHECK (length(trim(answer_text)) > 0),
    CONSTRAINT task_submissions_grade_check CHECK (grade IS NULL OR (grade >= 1 AND grade <= 5))
);

CREATE INDEX idx_task_group_assignments_task_id ON task_group_assignments(task_id);
CREATE INDEX idx_task_group_assignments_group_id ON task_group_assignments(group_id);
CREATE INDEX idx_task_submissions_task_id ON task_submissions(task_id);
CREATE INDEX idx_task_submissions_student_id ON task_submissions(student_id);
CREATE INDEX idx_task_submissions_status ON task_submissions(status);

CREATE TRIGGER trg_task_submissions_updated_at
BEFORE UPDATE ON task_submissions
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();
