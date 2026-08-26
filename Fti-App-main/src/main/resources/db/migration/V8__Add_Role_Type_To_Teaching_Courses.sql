-- ============================================================
-- V8__Add_Role_Type_To_Teaching_Courses.sql
-- Shtimi i kolones ROLE_TYPE ne TEACHING_COURSES
-- ============================================================
ALTER TABLE FTIAPP.TEACHING_COURSES ADD ROLE_TYPE VARCHAR2(20) DEFAULT 'LEKSION' NOT NULL;
