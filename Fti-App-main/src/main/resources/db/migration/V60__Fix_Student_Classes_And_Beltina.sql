-- V60: Korrigjimi i klasave për studentët (sigurohemi që Beltina dhe studentët e Grupit A janë caktuar saktësisht te Grupi A)

-- 1. Përditësojmë studentin Beltina në tabelën STUDENTS që të ketë CLASS_ID e Grupit A
UPDATE FTIAPP.STUDENTS s
SET s.CLASS_ID = (
    SELECT cl.CLASS_ID 
    FROM FTIAPP.CLASSES cl 
    WHERE cl.PROGRAM_ID = s.PROGRAM_ID 
      AND (cl.VIT_STUDIMIT = s.VIT_STUDIMIT OR s.VIT_STUDIMIT IS NULL)
      AND (UPPER(cl.EMRI_CLASS) LIKE '%A%' OR UPPER(cl.EMRI_CLASS) LIKE '%GRUPI A%')
    FETCH FIRST 1 ROWS ONLY
)
WHERE s.USER_ID IN (
    SELECT u.USER_ID FROM FTIAPP.USERS u
    WHERE LOWER(u.EMRI) LIKE '%beltina%' 
       OR LOWER(u.MBIEMRI) LIKE '%beltina%' 
       OR LOWER(u.EMAIL) LIKE '%beltina%'
)
OR s.STUDENT_ID IN (
    SELECT pe.ENROLLMENT_ID FROM FTIAPP.STUDENT_PRE_ENROLLMENT pe
    WHERE LOWER(pe.EMRI) LIKE '%beltina%' 
       OR LOWER(pe.MBIEMRI) LIKE '%beltina%' 
       OR LOWER(pe.EMAIL) LIKE '%beltina%'
);

-- 2. Përditësojmë edhe në tabelën STUDENT_PRE_ENROLLMENT
UPDATE FTIAPP.STUDENT_PRE_ENROLLMENT pe
SET pe.CLASS_ID = (
    SELECT cl.CLASS_ID 
    FROM FTIAPP.CLASSES cl 
    WHERE cl.PROGRAM_ID = pe.PROGRAM_ID 
      AND (cl.VIT_STUDIMIT = pe.VIT_STUDIMIT OR pe.VIT_STUDIMIT IS NULL)
      AND (UPPER(cl.EMRI_CLASS) LIKE '%A%' OR UPPER(cl.EMRI_CLASS) LIKE '%GRUPI A%')
    FETCH FIRST 1 ROWS ONLY
)
WHERE LOWER(pe.EMRI) LIKE '%beltina%' 
   OR LOWER(pe.MBIEMRI) LIKE '%beltina%' 
   OR LOWER(pe.EMAIL) LIKE '%beltina%';
