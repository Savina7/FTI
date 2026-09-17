-- Përditësimi i krediteve të detajuara për Master Profesional në Inxhinieri Software (Program ID 8)

-- 1. Përpunimi i të Dhënave me Python [Totali: 5]
UPDATE FTIAPP.COURSES c
SET c.KREDITE = 5,
    c.KREDITE_LEKSION = 2.0,
    c.KREDITE_SEMINAR = 1.0,
    c.KREDITE_LABORATOR = 1.0,
    c.KREDITE_DETYRE_KURSI = 1.0,
    c.KREDITE_PRAKTIKE = 0.0
WHERE (c.COURSE_ID = 175 OR (LOWER(c.EMRI_COURSE) LIKE '%python%' AND c.PROGRAM_ID = 8));

-- 2. Bazat e të Dhënave [Totali: 6]
UPDATE FTIAPP.COURSES c
SET c.KREDITE = 6,
    c.KREDITE_LEKSION = 2.0,
    c.KREDITE_SEMINAR = 1.0,
    c.KREDITE_LABORATOR = 2.0,
    c.KREDITE_DETYRE_KURSI = 1.0,
    c.KREDITE_PRAKTIKE = 0.0
WHERE (c.COURSE_ID = 176 OR (LOWER(c.EMRI_COURSE) LIKE '%bazat%dh%nave%' AND c.PROGRAM_ID = 8));

-- 3. Parimet e DevOps-it në Cloud [Totali: 7]
UPDATE FTIAPP.COURSES c
SET c.KREDITE = 7,
    c.KREDITE_LEKSION = 2.0,
    c.KREDITE_SEMINAR = 2.0,
    c.KREDITE_LABORATOR = 2.0,
    c.KREDITE_DETYRE_KURSI = 1.0,
    c.KREDITE_PRAKTIKE = 0.0
WHERE (c.COURSE_ID = 177 OR (LOWER(c.EMRI_COURSE) LIKE '%devops%' AND c.PROGRAM_ID = 8));

-- 4. Programimi i Avancuar [Totali: 6]
UPDATE FTIAPP.COURSES c
SET c.KREDITE = 6,
    c.KREDITE_LEKSION = 2.0,
    c.KREDITE_SEMINAR = 1.0,
    c.KREDITE_LABORATOR = 2.0,
    c.KREDITE_DETYRE_KURSI = 1.0,
    c.KREDITE_PRAKTIKE = 0.0
WHERE (c.COURSE_ID = 178 OR (LOWER(c.EMRI_COURSE) LIKE '%programimi%avancuar%' AND c.PROGRAM_ID = 8));

-- 5. Teknologjia e Testimit të Aplikacioneve [Totali: 6] (Përditësim emri dhe kreditesh)
UPDATE FTIAPP.COURSES c
SET c.EMRI_COURSE = 'Teknologjia e Testimit të Aplikacioneve',
    c.KREDITE = 6,
    c.KREDITE_LEKSION = 2.0,
    c.KREDITE_SEMINAR = 1.0,
    c.KREDITE_LABORATOR = 2.0,
    c.KREDITE_DETYRE_KURSI = 1.0,
    c.KREDITE_PRAKTIKE = 0.0
WHERE (c.COURSE_ID = 179 OR (LOWER(c.EMRI_COURSE) LIKE '%testimit%' AND c.PROGRAM_ID = 8));

-- 6. Aplikacione Enterprise në Java [Totali: 7]
UPDATE FTIAPP.COURSES c
SET c.KREDITE = 7,
    c.KREDITE_LEKSION = 2.0,
    c.KREDITE_SEMINAR = 2.0,
    c.KREDITE_LABORATOR = 2.0,
    c.KREDITE_DETYRE_KURSI = 1.0,
    c.KREDITE_PRAKTIKE = 0.0
WHERE (c.COURSE_ID = 180 OR (LOWER(c.EMRI_COURSE) LIKE '%enterprise%java%' AND c.PROGRAM_ID = 8));

-- 7. Programim në Web [Totali: 6]
UPDATE FTIAPP.COURSES c
SET c.KREDITE = 6,
    c.KREDITE_LEKSION = 2.0,
    c.KREDITE_SEMINAR = 1.0,
    c.KREDITE_LABORATOR = 2.0,
    c.KREDITE_DETYRE_KURSI = 1.0,
    c.KREDITE_PRAKTIKE = 0.0
WHERE (c.COURSE_ID = 181 OR (LOWER(c.EMRI_COURSE) LIKE '%programim%web%' AND c.PROGRAM_ID = 8));

-- 8. Zhvillim Aplikacioni në Cloud (Lëndë me zgjedhje 1) [Totali: 5]
UPDATE FTIAPP.COURSES c
SET c.KREDITE = 5,
    c.KREDITE_LEKSION = 2.0,
    c.KREDITE_SEMINAR = 1.0,
    c.KREDITE_LABORATOR = 1.0,
    c.KREDITE_DETYRE_KURSI = 1.0,
    c.KREDITE_PRAKTIKE = 0.0
WHERE (c.COURSE_ID = 182 OR (LOWER(c.EMRI_COURSE) LIKE '%zhvillim%cloud%' AND c.PROGRAM_ID = 8));

-- 9. Praktikë profesionale [Totali: 6]
UPDATE FTIAPP.COURSES c
SET c.KREDITE = 6,
    c.KREDITE_LEKSION = 0.0,
    c.KREDITE_SEMINAR = 0.0,
    c.KREDITE_LABORATOR = 0.0,
    c.KREDITE_DETYRE_KURSI = 0.0,
    c.KREDITE_PRAKTIKE = 6.0
WHERE (c.COURSE_ID = 183 OR (LOWER(c.EMRI_COURSE) LIKE '%praktik%' AND c.PROGRAM_ID = 8));

-- 10. Diploma [Totali: 6]
UPDATE FTIAPP.COURSES c
SET c.KREDITE = 6,
    c.KREDITE_LEKSION = 0.0,
    c.KREDITE_SEMINAR = 0.0,
    c.KREDITE_LABORATOR = 0.0,
    c.KREDITE_DETYRE_KURSI = 0.0,
    c.KREDITE_PRAKTIKE = 0.0
WHERE (c.COURSE_ID = 184 OR (LOWER(c.EMRI_COURSE) LIKE '%diplom%' AND c.PROGRAM_ID = 8));
