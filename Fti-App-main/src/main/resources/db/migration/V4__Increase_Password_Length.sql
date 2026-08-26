-- Increase PASSWORD column size in USERS table to support 60-character BCrypt hashes
ALTER TABLE USERS MODIFY PASSWORD VARCHAR2(255);
