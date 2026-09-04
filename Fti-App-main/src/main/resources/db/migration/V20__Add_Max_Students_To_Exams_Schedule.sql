-- =============================================================================
-- V20: Shto kolonen MAX_STUDENTS te tabela EXAMS_SCHEDULE
-- =============================================================================

DECLARE
    v_count NUMBER := 0;
BEGIN
    SELECT COUNT(*) INTO v_count
    FROM USER_TAB_COLS
    WHERE TABLE_NAME = 'EXAMS_SCHEDULE' AND COLUMN_NAME = 'MAX_STUDENTS';

    IF v_count = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE FTIAPP.EXAMS_SCHEDULE ADD (MAX_STUDENTS NUMBER)';
    END IF;
END;
/
