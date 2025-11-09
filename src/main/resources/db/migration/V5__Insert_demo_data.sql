-- V5__Insert_demo_data.sql
-- Demo-Posts für bestehende Motivise-User
-- nutzt username -> id Mapping, daher unabhängig von den UUID-Werten

INSERT INTO posts (id, subject, content, image_url, user_id, created_at)
VALUES (
           UUID_TO_BIN('11111111-1111-1111-1111-111111111111'),
           'webengineering',
           'Today I finally understood async/await and promise chains! #webengineering',
           NULL,
           (SELECT id FROM users WHERE username = 'study_anna'),
           NOW()
       );

INSERT INTO posts (id, subject, content, image_url, user_id, created_at)
VALUES (
           UUID_TO_BIN('22222222-2222-2222-2222-222222222222'),
           'database',
           'Playing around with Flyway migrations and MySQL. Database versioning feels powerful.',
           NULL,
           (SELECT id FROM users WHERE username = 'maxlearns'),
           NOW()
       );

INSERT INTO posts (id, subject, content, image_url, user_id, created_at)
VALUES (
           UUID_TO_BIN('33333333-3333-3333-3333-333333333333'),
           'motivation',
           'Welcome to Motivise! Share your study progress and keep your 30-day streak alive 💪',
           NULL,
           (SELECT id FROM users WHERE username = 'motadmin'),
           NOW()
       );
