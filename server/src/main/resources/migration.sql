-- Migration script to add username_hash column to existing databases
-- This will be executed if the column doesn't exist

-- Add username_hash column if it doesn't exist
ALTER TABLE users ADD COLUMN IF NOT EXISTS username_hash VARCHAR(255);

-- Create unique index on username_hash if it doesn't exist
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_username_hash ON users(username_hash);

-- Migrate existing usernames to username_hash
-- Note: This requires the PasswordService, so we'll handle this in code
-- For now, existing users will need to be recreated or we'll handle null username_hash

