-- Shpërndarja e barabartë dhe e larmishme e lëndëve/alokimeve midis të gjithë pedagogëve aktivë të departamentit
MERGE INTO FTIAPP.TEACHING_COURSES tc
USING (
    WITH ProfRanked AS (
        SELECT 
            p.PROFESSOR_ID,
            p.DEPARTMENT_ID,
            ROW_NUMBER() OVER (PARTITION BY p.DEPARTMENT_ID ORDER BY p.PROFESSOR_ID) - 1 as prof_idx,
            COUNT(*) OVER (PARTITION BY p.DEPARTMENT_ID) as prof_count
        FROM FTIAPP.PROFESSORS p
        WHERE p.STATUS = 'A' OR p.STATUS IS NULL
    ),
    TCRanked AS (
        SELECT 
            tc_inner.TEACHING_COURSE_ID,
            c.COURSE_ID,
            COALESCE(prog.DEPARTMENT_ID, 1) as DEPT_ID,
            ROW_NUMBER() OVER (PARTITION BY COALESCE(prog.DEPARTMENT_ID, 1) ORDER BY c.COURSE_ID, tc_inner.ROLE_TYPE, tc_inner.TEACHING_COURSE_ID) - 1 as tc_idx
        FROM FTIAPP.TEACHING_COURSES tc_inner
        JOIN FTIAPP.COURSES c ON tc_inner.COURSE_ID = c.COURSE_ID
        LEFT JOIN FTIAPP.PROGRAMS prog ON c.PROGRAM_ID = prog.PROGRAM_ID
    )
    SELECT 
        tcr.TEACHING_COURSE_ID,
        pr.PROFESSOR_ID as NEW_PROFESSOR_ID
    FROM TCRanked tcr
    JOIN ProfRanked pr ON tcr.DEPT_ID = pr.DEPARTMENT_ID AND MOD(tcr.tc_idx, pr.prof_count) = pr.prof_idx
) src
ON (tc.TEACHING_COURSE_ID = src.TEACHING_COURSE_ID)
WHEN MATCHED THEN
UPDATE SET tc.PROFESSOR_ID = src.NEW_PROFESSOR_ID;
