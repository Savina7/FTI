-- =============================================================================
-- V15: Ndryshimi i kolonave START_TIME dhe END_TIME nga TIMESTAMP ne VARCHAR2(10)
--      per te ruajtur vetem oren (psh. '08:00', '10:00') pa komponente date.
-- =============================================================================

-- 1. Shtojme dy kolona te perkohshme tek COURSE_SCHEDULE
ALTER TABLE FTIAPP.COURSE_SCHEDULE ADD (
    START_TIME_STR VARCHAR2(10),
    END_TIME_STR   VARCHAR2(10)
);

-- 2. Konvertojme vlerat ekzistuese TIMESTAMP ne formatin 'HH24:MI'
UPDATE FTIAPP.COURSE_SCHEDULE 
SET START_TIME_STR = TO_CHAR(START_TIME, 'HH24:MI'),
    END_TIME_STR   = TO_CHAR(END_TIME, 'HH24:MI');

-- 3. Fshijme kolonat e vjetra TIMESTAMP
ALTER TABLE FTIAPP.COURSE_SCHEDULE DROP (START_TIME, END_TIME);

-- 4. Riemertojme kolonat e reja ne START_TIME dhe END_TIME
ALTER TABLE FTIAPP.COURSE_SCHEDULE RENAME COLUMN START_TIME_STR TO START_TIME;
ALTER TABLE FTIAPP.COURSE_SCHEDULE RENAME COLUMN END_TIME_STR TO END_TIME;

-- 5. Vendosim kushtin NOT NULL
ALTER TABLE FTIAPP.COURSE_SCHEDULE MODIFY (
    START_TIME VARCHAR2(10) NOT NULL,
    END_TIME   VARCHAR2(10) NOT NULL
);
