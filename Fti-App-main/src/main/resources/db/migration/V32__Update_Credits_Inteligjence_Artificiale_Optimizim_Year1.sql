-- Përditësimi i krediteve të detajuara për lëndët e Vitit 1 (Inteligjencë Artificiale dhe Optimizim - Master Shkencor)

-- 1. Statistikë [Totali: 6]
UPDATE FTIAPP.COURSES c
SET c.KREDITE = 6,
    c.KREDITE_LEKSION = 3.0,
    c.KREDITE_SEMINAR = 2.0,
    c.KREDITE_LABORATOR = 0.0,
    c.KREDITE_DETYRE_KURSI = 1.0,
    c.KREDITE_PRAKTIKE = 0.0
WHERE (c.COURSE_ID = 153 OR (LOWER(c.EMRI_COURSE) LIKE '%statistik%' AND c.STUDY_YEAR = 1))
  AND (c.PROGRAM_ID IN (
      SELECT p.PROGRAM_ID FROM FTIAPP.PROGRAMS p 
      WHERE LOWER(p.SPECIALIZIMI) LIKE '%inteligjenc%optimizim%' OR p.PROGRAM_ID = 7
  ));

-- 2. Inteligjenca Artificiale [Totali: 6]
UPDATE FTIAPP.COURSES c
SET c.KREDITE = 6,
    c.KREDITE_LEKSION = 3.0,
    c.KREDITE_SEMINAR = 1.0,
    c.KREDITE_LABORATOR = 1.0,
    c.KREDITE_DETYRE_KURSI = 1.0,
    c.KREDITE_PRAKTIKE = 0.0
WHERE (c.COURSE_ID = 154 OR (LOWER(c.EMRI_COURSE) LIKE '%inteligjenc%artificiale%' AND c.STUDY_YEAR = 1))
  AND (c.PROGRAM_ID IN (
      SELECT p.PROGRAM_ID FROM FTIAPP.PROGRAMS p 
      WHERE LOWER(p.SPECIALIZIMI) LIKE '%inteligjenc%optimizim%' OR p.PROGRAM_ID = 7
  ));

-- 3. Cloud Computing [Totali: 6]
UPDATE FTIAPP.COURSES c
SET c.KREDITE = 6,
    c.KREDITE_LEKSION = 3.0,
    c.KREDITE_SEMINAR = 1.0,
    c.KREDITE_LABORATOR = 1.0,
    c.KREDITE_DETYRE_KURSI = 1.0,
    c.KREDITE_PRAKTIKE = 0.0
WHERE (c.COURSE_ID = 155 OR (LOWER(c.EMRI_COURSE) LIKE '%cloud%computing%' AND c.STUDY_YEAR = 1))
  AND (c.PROGRAM_ID IN (
      SELECT p.PROGRAM_ID FROM FTIAPP.PROGRAMS p 
      WHERE LOWER(p.SPECIALIZIMI) LIKE '%inteligjenc%optimizim%' OR p.PROGRAM_ID = 7
  ));

-- 4. Baza të Dhënash të Avancuara [Totali: 4]
UPDATE FTIAPP.COURSES c
SET c.KREDITE = 4,
    c.KREDITE_LEKSION = 2.0,
    c.KREDITE_SEMINAR = 1.0,
    c.KREDITE_LABORATOR = 0.5,
    c.KREDITE_DETYRE_KURSI = 0.5,
    c.KREDITE_PRAKTIKE = 0.0
WHERE (c.COURSE_ID = 156 OR (LOWER(c.EMRI_COURSE) LIKE '%baza%dh%navancuar%' AND c.STUDY_YEAR = 1))
  AND (c.PROGRAM_ID IN (
      SELECT p.PROGRAM_ID FROM FTIAPP.PROGRAMS p 
      WHERE LOWER(p.SPECIALIZIMI) LIKE '%inteligjenc%optimizim%' OR p.PROGRAM_ID = 7
  ));

-- 5. Programim i Avancuar (ish-Menaxhimi i Projekteve) [Totali: 5]
UPDATE FTIAPP.COURSES c
SET c.EMRI_COURSE = 'Programim i Avancuar',
    c.KREDITE = 5,
    c.KREDITE_LEKSION = 3.0,
    c.KREDITE_SEMINAR = 1.0,
    c.KREDITE_LABORATOR = 0.5,
    c.KREDITE_DETYRE_KURSI = 0.5,
    c.KREDITE_PRAKTIKE = 0.0
WHERE (c.COURSE_ID = 157 OR ((LOWER(c.EMRI_COURSE) LIKE '%menaxhim%projekt%' OR LOWER(c.EMRI_COURSE) LIKE '%programim%avancuar%') AND c.STUDY_YEAR = 1))
  AND (c.PROGRAM_ID IN (
      SELECT p.PROGRAM_ID FROM FTIAPP.PROGRAMS p 
      WHERE LOWER(p.SPECIALIZIMI) LIKE '%inteligjenc%optimizim%' OR p.PROGRAM_ID = 7
  ));

-- 6. Gjuhë Frënge 1 (Lëndë me zgjedhje - Tabela 1) [Totali: 3]
UPDATE FTIAPP.COURSES c
SET c.KREDITE = 3,
    c.KREDITE_LEKSION = 2.0,
    c.KREDITE_SEMINAR = 1.0,
    c.KREDITE_LABORATOR = 0.0,
    c.KREDITE_DETYRE_KURSI = 0.0,
    c.KREDITE_PRAKTIKE = 0.0
WHERE (c.COURSE_ID = 158 OR (LOWER(c.EMRI_COURSE) LIKE '%gjuh%fr%nge%1%' AND c.STUDY_YEAR = 1))
  AND (c.PROGRAM_ID IN (
      SELECT p.PROGRAM_ID FROM FTIAPP.PROGRAMS p 
      WHERE LOWER(p.SPECIALIZIMI) LIKE '%inteligjenc%optimizim%' OR p.PROGRAM_ID = 7
  ));

-- 7. Kërkime Operacionale [Totali: 6]
UPDATE FTIAPP.COURSES c
SET c.KREDITE = 6,
    c.KREDITE_LEKSION = 3.0,
    c.KREDITE_SEMINAR = 1.0,
    c.KREDITE_LABORATOR = 1.0,
    c.KREDITE_DETYRE_KURSI = 1.0,
    c.KREDITE_PRAKTIKE = 0.0
WHERE (c.COURSE_ID = 159 OR (LOWER(c.EMRI_COURSE) LIKE '%k%rkime%operacionale%' AND c.STUDY_YEAR = 1))
  AND (c.PROGRAM_ID IN (
      SELECT p.PROGRAM_ID FROM FTIAPP.PROGRAMS p 
      WHERE LOWER(p.SPECIALIZIMI) LIKE '%inteligjenc%optimizim%' OR p.PROGRAM_ID = 7
  ));

-- 8. Inxhinieri Softuere e Avancuar [Totali: 6]
UPDATE FTIAPP.COURSES c
SET c.KREDITE = 6,
    c.KREDITE_LEKSION = 3.0,
    c.KREDITE_SEMINAR = 1.0,
    c.KREDITE_LABORATOR = 1.0,
    c.KREDITE_DETYRE_KURSI = 1.0,
    c.KREDITE_PRAKTIKE = 0.0
WHERE (c.COURSE_ID = 160 OR (LOWER(c.EMRI_COURSE) LIKE '%inxhinieri%soft%' AND c.STUDY_YEAR = 1))
  AND (c.PROGRAM_ID IN (
      SELECT p.PROGRAM_ID FROM FTIAPP.PROGRAMS p 
      WHERE LOWER(p.SPECIALIZIMI) LIKE '%inteligjenc%optimizim%' OR p.PROGRAM_ID = 7
  ));

-- 9. Inxhinieria e Sistemeve Komplekse [Totali: 6]
UPDATE FTIAPP.COURSES c
SET c.KREDITE = 6,
    c.KREDITE_LEKSION = 3.0,
    c.KREDITE_SEMINAR = 1.0,
    c.KREDITE_LABORATOR = 1.0,
    c.KREDITE_DETYRE_KURSI = 1.0,
    c.KREDITE_PRAKTIKE = 0.0
WHERE (c.COURSE_ID = 161 OR (LOWER(c.EMRI_COURSE) LIKE '%sistemeve%komplekse%' AND c.STUDY_YEAR = 1))
  AND (c.PROGRAM_ID IN (
      SELECT p.PROGRAM_ID FROM FTIAPP.PROGRAMS p 
      WHERE LOWER(p.SPECIALIZIMI) LIKE '%inteligjenc%optimizim%' OR p.PROGRAM_ID = 7
  ));

-- 10. Machine Learning [Totali: 6]
UPDATE FTIAPP.COURSES c
SET c.KREDITE = 6,
    c.KREDITE_LEKSION = 3.0,
    c.KREDITE_SEMINAR = 1.0,
    c.KREDITE_LABORATOR = 1.0,
    c.KREDITE_DETYRE_KURSI = 1.0,
    c.KREDITE_PRAKTIKE = 0.0
WHERE (c.COURSE_ID = 162 OR (LOWER(c.EMRI_COURSE) LIKE '%machine%learning%' AND c.STUDY_YEAR = 1))
  AND (c.PROGRAM_ID IN (
      SELECT p.PROGRAM_ID FROM FTIAPP.PROGRAMS p 
      WHERE LOWER(p.SPECIALIZIMI) LIKE '%inteligjenc%optimizim%' OR p.PROGRAM_ID = 7
  ));

-- 11. Computer Vision [Totali: 6]
UPDATE FTIAPP.COURSES c
SET c.KREDITE = 6,
    c.KREDITE_LEKSION = 3.0,
    c.KREDITE_SEMINAR = 1.0,
    c.KREDITE_LABORATOR = 1.0,
    c.KREDITE_DETYRE_KURSI = 1.0,
    c.KREDITE_PRAKTIKE = 0.0
WHERE (c.COURSE_ID = 163 OR (LOWER(c.EMRI_COURSE) LIKE '%computer%vision%' AND c.STUDY_YEAR = 1))
  AND (c.PROGRAM_ID IN (
      SELECT p.PROGRAM_ID FROM FTIAPP.PROGRAMS p 
      WHERE LOWER(p.SPECIALIZIMI) LIKE '%inteligjenc%optimizim%' OR p.PROGRAM_ID = 7
  ));
