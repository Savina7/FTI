-- =============================================================================
-- V19: Modifikimi i tabeles FAILED_COURSES per ndjekjen e frekuentimit
--      Hiqet kolona GRADE_ID dhe shtohet kolona STATUS (default 'FREKUENTIM')
-- =============================================================================

ALTER TABLE FTIAPP.FAILED_COURSES DROP COLUMN GRADE_ID CASCADE CONSTRAINTS;

ALTER TABLE FTIAPP.FAILED_COURSES ADD (
    STATUS VARCHAR2(50) DEFAULT 'FREKUENTIM'
);
