-- Seed data for development
-- Using INSERT IGNORE equivalent for H2 (continue-on-error is enabled in application.properties)

-- Default admin user (password: admin123)
INSERT INTO users (username, password_hash, email, is_admin) 
SELECT 'admin', '$2a$10$vGq9fu0/uYVocj7pTZWe0e8iCjbFUDbZZkKN5aXVQZyi1hKML6Nka', 'admin@reservo.edu', TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

-- Default test users (password: user123)
INSERT INTO users (username, password_hash, email, is_admin) 
SELECT 'user1', '$2a$10$DVMBkzECfVU/BMgBB0xYZOM3yg9yd7TvUjT9RU6qigxMyvkwhQ/S6', 'user1@reservo.edu', FALSE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'user1');

INSERT INTO users (username, password_hash, email, is_admin) 
SELECT 'user2', '$2a$10$DVMBkzECfVU/BMgBB0xYZOM3yg9yd7TvUjT9RU6qigxMyvkwhQ/S6', 'user2@reservo.edu', FALSE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'user2');

INSERT INTO users (username, password_hash, email, is_admin) 
SELECT 'user3', '$2a$10$DVMBkzECfVU/BMgBB0xYZOM3yg9yd7TvUjT9RU6qigxMyvkwhQ/S6', 'user3@reservo.edu', FALSE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'user3');

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

