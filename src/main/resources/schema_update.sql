-- Database Schema Update for Users Table
-- Run this SQL script to update the users table schema

-- Make sure full_name, phone, and email are NOT NULL
ALTER TABLE users 
    ALTER COLUMN full_name SET NOT NULL,
    ALTER COLUMN phone SET NOT NULL,
    ALTER COLUMN email SET NOT NULL;

-- If columns don't exist, create them first:
-- ALTER TABLE users ADD COLUMN IF NOT EXISTS full_name TEXT;
-- ALTER TABLE users ADD COLUMN IF NOT EXISTS phone TEXT;
-- ALTER TABLE users ADD COLUMN IF NOT EXISTS email TEXT;
-- Then set NOT NULL constraints as above

