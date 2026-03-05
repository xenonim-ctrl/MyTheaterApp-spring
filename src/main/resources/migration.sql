-- Migration: Make full_name nullable in users table
-- This fixes the SQLState 23502 error

ALTER TABLE users ALTER COLUMN full_name DROP NOT NULL;

