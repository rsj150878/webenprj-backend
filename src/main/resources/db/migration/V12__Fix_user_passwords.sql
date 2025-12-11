-- V12__Fix_user_passwords.sql
-- Fix test users: replace plain text passwords with BCrypt hashed passwords
-- BCrypt hashes generated with cost factor 10 (Spring Security default)

-- Delete old users with plain text passwords
DELETE FROM users WHERE email IN (
    'anna.schmidt@example.com',
    'max.meier@example.com',
    'admin@motivise.app'
);

-- Re-insert users with properly BCrypt-hashed passwords
-- password column expects BCrypt hash (60 chars starting with $2a$ or $2b$)
INSERT INTO users (id, email, username, password, country_code, profile_image_url, role, created_at, active)
VALUES
    (UUID_TO_BIN('a8f23b8e-2a40-4f1a-9e7d-21a4c7c89d12'),
     'anna.schmidt@example.com',
     'study_anna',
     '$2b$10$qKpAZRFtRvcSpsuDCnGZw.Fz9Nu57cptSQZSZ4QZ5QWysZc7BqnAy',
     'AT',
     'https://example.com/images/profile1.png',
     'USER',
     NOW(),
     1),

    (UUID_TO_BIN('b6d5e90c-3e55-4c5f-bc1d-6720a2c841a9'),
     'max.meier@example.com',
     'maxlearns',
     '$2b$10$fEhuBoERKsfznX/ODvNJSOPM1wZev2x5TlrkBx2rUNtxQKWgvbn8S',
     'DE',
     'https://example.com/images/profile2.png',
     'USER',
     NOW(),
     1),

    (UUID_TO_BIN('c27f9b6b-6c8d-4d62-9e3a-1e8d87c7adf0'),
     'admin@motivise.app',
     'motadmin',
     '$2b$10$t0WgY8Sr6oj.3jwibGgSs.fjgZCmvE6XX9.HHOEcf6vIrDV7Nc0/S',
     'CH',
     'https://example.com/images/admin.png',
     'ADMIN',
     NOW(),
     1);
