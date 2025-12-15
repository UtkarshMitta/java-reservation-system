-- Seed data for development
-- Using INSERT IGNORE equivalent for H2 (continue-on-error is enabled in application.properties)
-- Note: Passwords are hashed with salt+pepper. Usernames are also hashed.

-- Default admin user (password: admin123)
-- Password hash includes pepper: admin123 + PEPPER -> BCrypt
-- Username hash: HMAC-SHA256 with salt+pepper
INSERT INTO users (username, username_hash, password_hash, email, is_admin) 
SELECT 'admin', 'LjkY56iC4sXfavGna0r0rs0aPLvG83xj8rK/JCtBb+s=', '$2a$10$D2HeajffAk5KLal3Uwkpa.DgQRO6wo9yFgys597BskyJeSHTuzvQS', 'admin@nyu.edu', TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username_hash = 'LjkY56iC4sXfavGna0r0rs0aPLvG83xj8rK/JCtBb+s=');

-- Default test users (password: user123)
INSERT INTO users (username, username_hash, password_hash, email, is_admin) 
SELECT 'user1', 'JEMf58huOhFgOlIygbaynswpWOmHpYCy26ecduM3rTo=', '$2a$10$FmCywRfojHliC7f9Wj8B4efdMEabTpldCQ34PNq.gOpnwaUemuNZG', 'user1@nyu.edu', FALSE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username_hash = 'JEMf58huOhFgOlIygbaynswpWOmHpYCy26ecduM3rTo=');

INSERT INTO users (username, username_hash, password_hash, email, is_admin) 
SELECT 'user2', 'wuvDQ0+xSwnBwfCRNi41agpC6mUhBdiw5D6NR086QjY=', '$2a$10$FmCywRfojHliC7f9Wj8B4efdMEabTpldCQ34PNq.gOpnwaUemuNZG', 'user2@nyu.edu', FALSE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username_hash = 'wuvDQ0+xSwnBwfCRNi41agpC6mUhBdiw5D6NR086QjY=');

INSERT INTO users (username, username_hash, password_hash, email, is_admin) 
SELECT 'user3', '6mSlZa3NC9LWgcGtlhJbJl9Agab2WEeVEUFmsxX8zCo=', '$2a$10$FmCywRfojHliC7f9Wj8B4efdMEabTpldCQ34PNq.gOpnwaUemuNZG', 'user3@nyu.edu', FALSE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username_hash = '6mSlZa3NC9LWgcGtlhJbJl9Agab2WEeVEUFmsxX8zCo=');

-- Sample resources
INSERT INTO resources (name, capacity, slot_duration_minutes, booking_horizon_days, max_hours_per_day, rules_json) 
SELECT 'Study Room A', 8, 60, 30, 4, '{"allow_cancellation": true, "min_advance_hours": 1}'
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE name = 'Study Room A');

INSERT INTO resources (name, capacity, slot_duration_minutes, booking_horizon_days, max_hours_per_day, rules_json) 
SELECT 'Study Room B', 6, 60, 30, 4, '{"allow_cancellation": true, "min_advance_hours": 1}'
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE name = 'Study Room B');

INSERT INTO resources (name, capacity, slot_duration_minutes, booking_horizon_days, max_hours_per_day, rules_json) 
SELECT 'Squash Court 1', 1, 30, 14, 2, '{"allow_cancellation": true, "min_advance_hours": 2}'
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE name = 'Squash Court 1');

INSERT INTO resources (name, capacity, slot_duration_minutes, booking_horizon_days, max_hours_per_day, rules_json) 
SELECT 'Squash Court 2', 1, 30, 14, 2, '{"allow_cancellation": true, "min_advance_hours": 2}'
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE name = 'Squash Court 2');

INSERT INTO resources (name, capacity, slot_duration_minutes, booking_horizon_days, max_hours_per_day, rules_json) 
SELECT 'Lab Bench 1', 1, 120, 7, 8, '{"allow_cancellation": true, "min_advance_hours": 4}'
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE name = 'Lab Bench 1');

INSERT INTO resources (name, capacity, slot_duration_minutes, booking_horizon_days, max_hours_per_day, rules_json) 
SELECT 'Clinic Slot', 1, 30, 30, 2, '{"allow_cancellation": true, "min_advance_hours": 24}'
WHERE NOT EXISTS (SELECT 1 FROM resources WHERE name = 'Clinic Slot');

