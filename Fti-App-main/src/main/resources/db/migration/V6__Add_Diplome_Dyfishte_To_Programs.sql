-- ============================================================
-- V6__Add_Diplome_Dyfishte_To_Programs.sql
-- Adds DIPLOME_DYFISHTE column to FTIAPP.PROGRAMS table
-- ============================================================

ALTER TABLE FTIAPP.PROGRAMS ADD DIPLOME_DYFISHTE VARCHAR2(10) DEFAULT 'JO';
