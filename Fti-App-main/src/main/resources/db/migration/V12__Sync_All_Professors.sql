-- ============================================================
-- V12__Sync_All_Professors.sql
-- Sinkronizim dinamik dhe gjeneral per TE GJITHE pedagoget ne sistem
-- ============================================================

-- 1. Shton ne tabelen PROFESSORS cdo perdorues qe ka rol PROFESSOR
--    dhe nuk ka ende nje rekord ne tabelen PROFESSORS
INSERT INTO FTIAPP.PROFESSORS (USER_ID, STATUS, DEPARTMENT_ID)
SELECT u.USER_ID, 'A', NVL(
    (SELECT MIN(DEPARTMENT_ID) FROM FTIAPP.DEPARTMENTS),
    1
)
FROM FTIAPP.USERS u
WHERE u.USER_ID IN (
    SELECT ur.USER_ID 
    FROM FTIAPP.USER_ROLES ur 
    JOIN FTIAPP.ROLES r ON ur.ROLE_ID = r.ROLE_ID 
    WHERE UPPER(r.ROLE_NAME) IN ('PROFESSOR', 'TEACHER', 'PETAGOG', 'LEKTOR')
)
AND NOT EXISTS (
    SELECT 1 FROM FTIAPP.PROFESSORS p WHERE p.USER_ID = u.USER_ID
);

-- 2. Aktivizon dhe verifikon te gjithe pedagoget ne USERS dhe PROFESSORS
UPDATE FTIAPP.USERS
SET VERIFIED = 'Y', STATUS = 'A'
WHERE USER_ID IN (
    SELECT ur.USER_ID 
    FROM FTIAPP.USER_ROLES ur 
    JOIN FTIAPP.ROLES r ON ur.ROLE_ID = r.ROLE_ID 
    WHERE UPPER(r.ROLE_NAME) IN ('PROFESSOR', 'TEACHER', 'PETAGOG', 'LEKTOR')
);

UPDATE FTIAPP.PROFESSORS
SET STATUS = 'A'
WHERE STATUS IS NULL OR STATUS != 'A';
