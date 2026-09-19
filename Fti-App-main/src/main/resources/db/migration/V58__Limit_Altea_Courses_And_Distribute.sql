-- Heqim lëndët e tepërta nga Altea Kostallari dhe ua caktojmë pedagogëve të tjerë
MERGE INTO FTIAPP.TEACHING_COURSES tc
USING (
    WITH AlteaProf AS (
        SELECT p.PROFESSOR_ID 
        FROM FTIAPP.PROFESSORS p
        LEFT JOIN FTIAPP.USERS u ON p.USER_ID = u.USER_ID
        WHERE LOWER(u.EMAIL) LIKE '%altea%' OR LOWER(u.EMRI) LIKE '%altea%' OR p.PROFESSOR_ID = 26
        FETCH FIRST 1 ROWS ONLY
    ),
    OtherProfs AS (
        SELECT 
            p.PROFESSOR_ID,
            ROW_NUMBER() OVER (ORDER BY p.PROFESSOR_ID) - 1 as prof_idx,
            COUNT(*) OVER () as total_other_profs
        FROM FTIAPP.PROFESSORS p
        LEFT JOIN FTIAPP.USERS u ON p.USER_ID = u.USER_ID
        WHERE (u.EMAIL IS NULL OR (LOWER(u.EMAIL) NOT LIKE '%altea%' AND LOWER(u.EMRI) NOT LIKE '%altea%'))
          AND p.PROFESSOR_ID != NVL((SELECT PROFESSOR_ID FROM AlteaProf), -1)
    ),
    TcToReassign AS (
        SELECT 
            tc_inner.TEACHING_COURSE_ID,
            c.EMRI_COURSE,
            ROW_NUMBER() OVER (ORDER BY tc_inner.TEACHING_COURSE_ID) - 1 as tc_idx
        FROM FTIAPP.TEACHING_COURSES tc_inner
        JOIN FTIAPP.COURSES c ON tc_inner.COURSE_ID = c.COURSE_ID
        WHERE tc_inner.PROFESSOR_ID = (SELECT PROFESSOR_ID FROM AlteaProf)
          AND LOWER(c.EMRI_COURSE) NOT LIKE '%analiz%'
          AND LOWER(c.EMRI_COURSE) NOT LIKE '%struktur%'
          AND LOWER(c.EMRI_COURSE) NOT LIKE '%algoritm%'
    )
    SELECT 
        tcr.TEACHING_COURSE_ID,
        op.PROFESSOR_ID as NEW_PROFESSOR_ID
    FROM TcToReassign tcr
    JOIN OtherProfs op ON MOD(tcr.tc_idx, op.total_other_profs) = op.prof_idx
) src
ON (tc.TEACHING_COURSE_ID = src.TEACHING_COURSE_ID)
WHEN MATCHED THEN
UPDATE SET tc.PROFESSOR_ID = src.NEW_PROFESSOR_ID;
