-- ============================================================
-- V13__Link_Altea_Professor.sql
-- Lidh me siguri User-in Altea me Professor ID = 26
-- ============================================================

UPDATE FTIAPP.PROFESSORS
SET USER_ID = (
    SELECT USER_ID 
    FROM FTIAPP.USERS 
    WHERE LOWER(EMAIL) LIKE '%altea%' OR LOWER(EMRI) LIKE '%altea%'
    FETCH FIRST 1 ROWS ONLY
)
WHERE PROFESSOR_ID = 26
AND (USER_ID IS NULL OR USER_ID != (
    SELECT USER_ID 
    FROM FTIAPP.USERS 
    WHERE LOWER(EMAIL) LIKE '%altea%' OR LOWER(EMRI) LIKE '%altea%'
    FETCH FIRST 1 ROWS ONLY
));
