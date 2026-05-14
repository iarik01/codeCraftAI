CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT roles_code_check CHECK (code IN ('TEACHER', 'STUDENT'))
);

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id UUID NOT NULL REFERENCES roles(id),
    name VARCHAR(150) NOT NULL,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT users_email_unique UNIQUE (email),
    CONSTRAINT users_email_not_blank CHECK (length(trim(email)) > 0),
    CONSTRAINT users_name_not_blank CHECK (length(trim(name)) > 0)
);

CREATE TABLE groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    teacher_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    invite_code VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT groups_invite_code_unique UNIQUE (invite_code),
    CONSTRAINT groups_name_not_blank CHECK (length(trim(name)) > 0)
);

CREATE TABLE group_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id UUID NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    joined_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT group_members_group_student_unique UNIQUE (group_id, student_id)
);

CREATE TABLE assignment_generation_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    teacher_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    direction VARCHAR(32) NOT NULL,
    topic VARCHAR(255) NOT NULL,
    age_group VARCHAR(16) NOT NULL,
    difficulty VARCHAR(32) NOT NULL,
    assignment_type VARCHAR(64) NOT NULL,
    questions_count INTEGER,
    additional_requirements TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT generation_direction_check CHECK (direction IN ('SCRATCH', 'PYTHON', 'ALGORITHMS', 'HTML_CSS')),
    CONSTRAINT generation_age_group_check CHECK (age_group IN ('AGE_7_9', 'AGE_10_12', 'AGE_13_15', 'AGE_16_17')),
    CONSTRAINT generation_difficulty_check CHECK (difficulty IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    CONSTRAINT generation_assignment_type_check CHECK (
        assignment_type IN ('PRACTICE', 'TEST', 'BUG_FIX', 'MINI_PROJECT', 'HOMEWORK_WITH_CRITERIA')
    ),
    CONSTRAINT generation_questions_count_check CHECK (questions_count IS NULL OR questions_count > 0),
    CONSTRAINT generation_topic_not_blank CHECK (length(trim(topic)) > 0)
);

CREATE TABLE generated_assignments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    teacher_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    generation_request_id UUID REFERENCES assignment_generation_requests(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    goal TEXT,
    instructions TEXT NOT NULL,
    expected_result TEXT,
    evaluation_criteria TEXT,
    hints TEXT,
    teacher_solution TEXT,
    direction VARCHAR(32) NOT NULL,
    age_group VARCHAR(16) NOT NULL,
    difficulty VARCHAR(32) NOT NULL,
    assignment_type VARCHAR(64) NOT NULL,
    source VARCHAR(32) NOT NULL DEFAULT 'AI',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT generated_assignments_direction_check CHECK (direction IN ('SCRATCH', 'PYTHON', 'ALGORITHMS', 'HTML_CSS')),
    CONSTRAINT generated_assignments_age_group_check CHECK (age_group IN ('AGE_7_9', 'AGE_10_12', 'AGE_13_15', 'AGE_16_17')),
    CONSTRAINT generated_assignments_difficulty_check CHECK (difficulty IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    CONSTRAINT generated_assignments_type_check CHECK (
        assignment_type IN ('PRACTICE', 'TEST', 'BUG_FIX', 'MINI_PROJECT', 'HOMEWORK_WITH_CRITERIA')
    ),
    CONSTRAINT generated_assignments_source_check CHECK (source IN ('AI', 'MOCK', 'MANUAL')),
    CONSTRAINT generated_assignments_title_not_blank CHECK (length(trim(title)) > 0)
);

CREATE TABLE assignment_groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assignment_id UUID NOT NULL REFERENCES generated_assignments(id) ON DELETE CASCADE,
    group_id UUID NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    deadline TIMESTAMPTZ,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT assignment_groups_assignment_group_unique UNIQUE (assignment_id, group_id)
);

CREATE TABLE assignment_submissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assignment_group_id UUID NOT NULL REFERENCES assignment_groups(id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    answer_text TEXT NOT NULL,
    answer_url TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'SUBMITTED',
    teacher_comment TEXT,
    submitted_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    reviewed_at TIMESTAMPTZ,
    CONSTRAINT assignment_submissions_latest_unique UNIQUE (assignment_group_id, student_id),
    CONSTRAINT assignment_submissions_status_check CHECK (status IN ('SUBMITTED', 'ACCEPTED', 'NEEDS_REVISION')),
    CONSTRAINT assignment_submissions_answer_not_blank CHECK (length(trim(answer_text)) > 0)
);

CREATE INDEX idx_users_role_id ON users(role_id);
CREATE INDEX idx_groups_teacher_id ON groups(teacher_id);
CREATE INDEX idx_group_members_student_id ON group_members(student_id);
CREATE INDEX idx_generation_requests_teacher_id ON assignment_generation_requests(teacher_id);
CREATE INDEX idx_generated_assignments_teacher_id ON generated_assignments(teacher_id);
CREATE INDEX idx_generated_assignments_generation_request_id ON generated_assignments(generation_request_id);
CREATE INDEX idx_assignment_groups_group_id ON assignment_groups(group_id);
CREATE INDEX idx_assignment_submissions_student_id ON assignment_submissions(student_id);
