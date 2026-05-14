INSERT INTO roles (code, name)
VALUES
    ('TEACHER', 'Преподаватель'),
    ('STUDENT', 'Ученик')
ON CONFLICT (code) DO NOTHING;
