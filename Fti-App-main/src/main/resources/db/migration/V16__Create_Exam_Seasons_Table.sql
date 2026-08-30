-- =============================================================================
-- V16: Krijimi i tabeles EXAM_SEASONS per konfigurimin e datave te sezoneve
-- =============================================================================

CREATE TABLE FTIAPP.EXAM_SEASONS (
    SEASON_ID       NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ACADEMIC_YEAR   VARCHAR2(20) NOT NULL,
    SEASON_TYPE     VARCHAR2(20) NOT NULL,
    PROGRAM_ID      NUMBER,
    START_DATE      DATE NOT NULL,
    END_DATE        DATE NOT NULL,
    CONSTRAINT FK_EXAM_SEASONS_PROG FOREIGN KEY (PROGRAM_ID) REFERENCES FTIAPP.PROGRAMS(PROGRAM_ID),
    CONSTRAINT UQ_EXAM_SEASON UNIQUE (ACADEMIC_YEAR, SEASON_TYPE, PROGRAM_ID)
);
