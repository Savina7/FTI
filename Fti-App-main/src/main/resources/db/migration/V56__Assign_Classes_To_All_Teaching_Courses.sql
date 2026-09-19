-- Pastrimi i lidhjeve ekzistuese per t'i ristrukturuar ne menyre te sakte dhe te larmishme
DELETE FROM FTIAPP.TEACHING_COURSES_CLASSES;

-- 1. LEKSION: Pedagogu mbulon te gjitha grupet e atij viti (Grupi A, B, C, D)
INSERT INTO FTIAPP.TEACHING_COURSES_CLASSES (TEACHING_COURSE_ID, CLASS_ID)
SELECT DISTINCT tc.TEACHING_COURSE_ID, cl.CLASS_ID
FROM FTIAPP.TEACHING_COURSES tc
JOIN FTIAPP.COURSES c ON tc.COURSE_ID = c.COURSE_ID
JOIN FTIAPP.CLASSES cl ON cl.PROGRAM_ID = c.PROGRAM_ID AND (cl.VIT_STUDIMIT = c.STUDY_YEAR OR c.STUDY_YEAR IS NULL)
WHERE UPPER(tc.ROLE_TYPE) = 'LEKSION';

-- 2. SEMINAR / LABORATOR / DETYRE / PRAKTIKE: Ndarja realiste sipas grupeve (Grupi A & B per pedagogun 1, Grupi C & D per pedagogun 2)
INSERT INTO FTIAPP.TEACHING_COURSES_CLASSES (TEACHING_COURSE_ID, CLASS_ID)
WITH RankedTC AS (
    SELECT 
        tc.TEACHING_COURSE_ID,
        tc.COURSE_ID,
        tc.ROLE_TYPE,
        ROW_NUMBER() OVER (PARTITION BY tc.COURSE_ID, UPPER(tc.ROLE_TYPE) ORDER BY tc.TEACHING_COURSE_ID) as tc_num,
        COUNT(*) OVER (PARTITION BY tc.COURSE_ID, UPPER(tc.ROLE_TYPE)) as total_tc
    FROM FTIAPP.TEACHING_COURSES tc
    WHERE UPPER(tc.ROLE_TYPE) != 'LEKSION'
),
RankedClasses AS (
    SELECT 
        c.COURSE_ID,
        cl.CLASS_ID,
        cl.EMRI_CLASS,
        ROW_NUMBER() OVER (PARTITION BY c.COURSE_ID ORDER BY cl.CLASS_ID) as class_num,
        COUNT(*) OVER (PARTITION BY c.COURSE_ID) as total_classes
    FROM FTIAPP.COURSES c
    JOIN FTIAPP.CLASSES cl ON cl.PROGRAM_ID = c.PROGRAM_ID AND (cl.VIT_STUDIMIT = c.STUDY_YEAR OR c.STUDY_YEAR IS NULL)
)
SELECT DISTINCT rtc.TEACHING_COURSE_ID, rc.CLASS_ID
FROM RankedTC rtc
JOIN RankedClasses rc ON rtc.COURSE_ID = rc.COURSE_ID
WHERE 
    -- Nese ka vetem 1 pedagog per ate rol ne ate lende, ai mbulon te gjitha grupet
    (rtc.total_tc = 1)
    -- Nese ka 2 pedagoge per seminar/lab: pedagogu 1 mbulon Grupet A & B, pedagogu 2 mbulon Grupet C & D
    OR (rtc.total_tc = 2 AND rtc.tc_num = 1 AND rc.class_num <= CEIL(rc.total_classes / 2.0))
    OR (rtc.total_tc = 2 AND rtc.tc_num = 2 AND rc.class_num > CEIL(rc.total_classes / 2.0))
    -- Nese ka me shume se 2 pedagoge, shperndahen sipas modulit
    OR (rtc.total_tc > 2 AND MOD(rc.class_num - 1, rtc.total_tc) = (rtc.tc_num - 1));

-- 3. Fallback per cdo Teaching Course qe mund te mos kete gjetur klase me siper
INSERT INTO FTIAPP.TEACHING_COURSES_CLASSES (TEACHING_COURSE_ID, CLASS_ID)
SELECT DISTINCT tc.TEACHING_COURSE_ID, cl.CLASS_ID
FROM FTIAPP.TEACHING_COURSES tc
JOIN FTIAPP.COURSES c ON tc.COURSE_ID = c.COURSE_ID
JOIN FTIAPP.CLASSES cl ON cl.PROGRAM_ID = c.PROGRAM_ID
WHERE NOT EXISTS (
    SELECT 1 FROM FTIAPP.TEACHING_COURSES_CLASSES tcc 
    WHERE tcc.TEACHING_COURSE_ID = tc.TEACHING_COURSE_ID
);
