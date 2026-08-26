-- ============================================================
-- V3__Add_Missing_Verification_Columns.sql
-- Adds missing columns that V2 did not apply to the live DB:
--   CODE_CREATED_AT (and VERIFIED, VERIFICATION_CODE if also missing)
-- to the USERS table.
-- ============================================================

ALTER TABLE FTIAPP.USERS ADD CODE_CREATED_AT TIMESTAMP;
