-- ============================================================
-- V11__Add_Status_To_Student_Pre_Enrollment.sql
-- Shton kolonen STATUS ne STUDENT_PRE_ENROLLMENT
-- ============================================================

ALTER TABLE FTIAPP.STUDENT_PRE_ENROLLMENT ADD (
    STATUS VARCHAR2(30) DEFAULT 'PARAREGJISTRUAR' NOT NULL
);

-- Nese studenti ekziston tashme ne USERS dhe eshte i verifikuar, vendoset VERIFIKUAR
UPDATE FTIAPP.STUDENT_PRE_ENROLLMENT pe
SET pe.STATUS = 'VERIFIKUAR'
WHERE LOWER(pe.EMAIL) IN (SELECT LOWER(u.EMAIL) FROM FTIAPP.USERS u WHERE u.VERIFIED = 'Y');
