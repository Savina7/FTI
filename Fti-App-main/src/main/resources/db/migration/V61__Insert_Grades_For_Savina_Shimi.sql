-- V61: Shtimi i notave për studenten Savina Shimi për të treja vitet e studimit

MERGE INTO FTIAPP.GRADES g
USING (
    WITH SavinaStudent AS (
        SELECT s.STUDENT_ID, s.PROGRAM_ID
        FROM FTIAPP.STUDENTS s
        JOIN FTIAPP.USERS u ON s.USER_ID = u.USER_ID
        WHERE LOWER(u.EMRI) LIKE '%savina%' OR LOWER(u.MBIEMRI) LIKE '%shimi%' OR LOWER(u.EMAIL) LIKE '%savina%'
        FETCH FIRST 1 ROWS ONLY
    ),
    TargetCourses AS (
        SELECT 
            c.COURSE_ID,
            c.STUDY_YEAR,
            (
                SELECT MIN(tc.TEACHING_COURSE_ID)
                FROM FTIAPP.TEACHING_COURSES tc
                WHERE tc.COURSE_ID = c.COURSE_ID
            ) as TEACHING_COURSE_ID,
            ROW_NUMBER() OVER (ORDER BY c.STUDY_YEAR, c.COURSE_ID) as rn
        FROM FTIAPP.COURSES c
        CROSS JOIN SavinaStudent ss
        WHERE (c.PROGRAM_ID = ss.PROGRAM_ID OR ss.PROGRAM_ID IS NULL OR c.PROGRAM_ID = 1)
          AND c.STUDY_YEAR IN (1, 2, 3)
    )
    SELECT 
        ss.STUDENT_ID,
        tc.TEACHING_COURSE_ID,
        CASE MOD(tc.rn, 5)
            WHEN 0 THEN 10.0
            WHEN 1 THEN 9.0
            WHEN 2 THEN 8.0
            WHEN 3 THEN 10.0
            ELSE 9.0
        END as GRADE,
        'PASSED' as STATUS,
        SYSDATE as DATE_GIVEN
    FROM TargetCourses tc
    CROSS JOIN SavinaStudent ss
    WHERE tc.TEACHING_COURSE_ID IS NOT NULL
) src
ON (g.STUDENT_ID = src.STUDENT_ID AND g.TEACHING_COURSE_ID = src.TEACHING_COURSE_ID)
WHEN MATCHED THEN
    UPDATE SET g.GRADE = src.GRADE, g.STATUS = src.STATUS, g.DATE_GIVEN = src.DATE_GIVEN
WHEN NOT MATCHED THEN
    INSERT (STUDENT_ID, TEACHING_COURSE_ID, GRADE, STATUS, DATE_GIVEN)
    VALUES (src.STUDENT_ID, src.TEACHING_COURSE_ID, src.GRADE, src.STATUS, src.DATE_GIVEN);

-- Përditësojmë edhe totalin e krediteve për Savina Shimi
UPDATE FTIAPP.STUDENTS s
SET s.TOTAL_KREDITE = NVL((
    SELECT SUM(c.KREDITE)
    FROM FTIAPP.GRADES g
    JOIN FTIAPP.TEACHING_COURSES tc ON g.TEACHING_COURSE_ID = tc.TEACHING_COURSE_ID
    JOIN FTIAPP.COURSES c ON tc.COURSE_ID = c.COURSE_ID
    WHERE g.STUDENT_ID = s.STUDENT_ID AND (g.GRADE >= 5.0 OR UPPER(g.STATUS) = 'PASSED')
), 180)
WHERE s.USER_ID IN (
    SELECT u.USER_ID FROM FTIAPP.USERS u
    WHERE LOWER(u.EMRI) LIKE '%savina%' OR LOWER(u.MBIEMRI) LIKE '%shimi%' OR LOWER(u.EMAIL) LIKE '%savina%'
);
