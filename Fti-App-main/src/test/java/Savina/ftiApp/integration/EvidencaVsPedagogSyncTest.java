package Savina.ftiApp.integration;

import Savina.ftiApp.dto.responseDTO.PedagogOptionsDto;
import Savina.ftiApp.entity.Professor;
import Savina.ftiApp.entity.TeachingCourse;
import Savina.ftiApp.repository.ProfessorRepository;
import Savina.ftiApp.repository.TeachingCourseRepository;
import Savina.ftiApp.service.AdminTeachingCourseService;
import Savina.ftiApp.service.PedagogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@SpringBootTest
public class EvidencaVsPedagogSyncTest {

    @Autowired
    private ProfessorRepository professorRepo;

    @Autowired
    private TeachingCourseRepository teachingCourseRepo;

    @Autowired
    private PedagogService pedagogService;

    @Autowired
    private AdminTeachingCourseService adminTeachingCourseService;

    @Test
    @Transactional(readOnly = true)
    public void compareAllProfessorsSync() {
        System.out.println("================================================================================");
        System.out.println("DEEP AUDIT & COMPARISON: PEDAGOG OPTIONS vs. EVIDENCA API vs. DATABASE FOR ALL PROFS");
        System.out.println("================================================================================");

        List<Professor> professors = professorRepo.findAll();
        System.out.println("Total professors found: " + professors.size());

        int totalChecked = 0;
        int mismatchCount = 0;

        for (Professor prof : professors) {
            Integer profId = prof.getProfessorId();
            String email = (prof.getUser() != null && prof.getUser().getEmail() != null) ? prof.getUser().getEmail() : null;
            String name = (prof.getUser() != null) ? (prof.getUser().getEmri() + " " + prof.getUser().getMbiemri()) : ("Prof " + profId);

            List<TeachingCourse> dbTcs = teachingCourseRepo.findByProfessorIdWithDetails(profId);
            if (dbTcs == null || dbTcs.isEmpty()) {
                dbTcs = teachingCourseRepo.findByProfessorProfessorId(profId);
            }

            if (dbTcs.isEmpty()) {
                continue; // Professor with 0 teaching courses
            }

            totalChecked++;
            System.out.println("\n--------------------------------------------------------------------------------");
            System.out.println(String.format("👉 PROFESSOR [%d] %s (%s) - DB Total TCs: %d", profId, name, email, dbTcs.size()));

            // 1. Database raw summary
            Map<String, Set<String>> dbCourseRoles = new LinkedHashMap<>();
            for (TeachingCourse tc : dbTcs) {
                if (tc.getCourse() == null) continue;
                String cName = tc.getCourse().getEmriCourse();
                String role = tc.getRoleType() != null ? tc.getRoleType().trim().toUpperCase() : "UNKNOWN";
                String sem = tc.getCourse().getSemester() != null ? tc.getCourse().getSemester().trim() : "1";
                String key = cName + " (Sem " + sem + ")";
                dbCourseRoles.computeIfAbsent(key, k -> new LinkedHashSet<>()).add(role);
            }
            System.out.println("   [DB Raw Courses & Roles]:");
            dbCourseRoles.forEach((c, roles) -> System.out.println("     - " + c + ": " + roles));

            // 2. Pedagog Options API
            PedagogOptionsDto options = pedagogService.getPedagogOptions(prof.getUser() != null ? prof.getUser().getUserId() : null, email);
            List<String> optCourseNames = (options != null && options.getCourses() != null)
                    ? options.getCourses().stream().map(c -> c.getName() + " [roles: " + c.getRoleTypes() + "]").collect(Collectors.toList())
                    : Collections.emptyList();
            System.out.println("   [Pedagog Options Courses]:");
            optCourseNames.forEach(c -> System.out.println("     - " + c));

            // 3. Evidenca API for Semester 1
            List<Map<String, Object>> evSem1 = adminTeachingCourseService.getEvidenca(profId, "1", "2025-2026");
            System.out.println("   [Evidenca Sem 1]: " + evSem1.size() + " courses");
            for (Map<String, Object> ev : evSem1) {
                System.out.println(String.format("     - %s | Prog: %s | Lek: %sh (%s) | Sem: %sh (%s) | Lab: %sh (%s) | DK: %sh (%s)",
                        ev.get("courseEmri"), ev.get("programEmri"),
                        ev.get("oreLeksion"), ev.get("grupiLeksion"),
                        ev.get("oreSeminar"), ev.get("grupiSeminar"),
                        ev.get("oreLaborator"), ev.get("grupiLaborator"),
                        ev.get("oreDetyreKursi"), ev.get("grupiDetyreKursi")));
            }

            // 4. Evidenca API for Semester 2
            List<Map<String, Object>> evSem2 = adminTeachingCourseService.getEvidenca(profId, "2", "2025-2026");
            System.out.println("   [Evidenca Sem 2]: " + evSem2.size() + " courses");
            for (Map<String, Object> ev : evSem2) {
                System.out.println(String.format("     - %s | Prog: %s | Lek: %sh (%s) | Sem: %sh (%s) | Lab: %sh (%s) | DK: %sh (%s)",
                        ev.get("courseEmri"), ev.get("programEmri"),
                        ev.get("oreLeksion"), ev.get("grupiLeksion"),
                        ev.get("oreSeminar"), ev.get("grupiSeminar"),
                        ev.get("oreLaborator"), ev.get("grupiLaborator"),
                        ev.get("oreDetyreKursi"), ev.get("grupiDetyreKursi")));
            }

            // Check if any course from DB is completely missing in Evidenca (Sem 1 + Sem 2)
            Set<String> evAllCourseNames = new HashSet<>();
            for (Map<String, Object> ev : evSem1) evAllCourseNames.add(((String) ev.get("courseEmri")).toLowerCase().trim());
            for (Map<String, Object> ev : evSem2) evAllCourseNames.add(((String) ev.get("courseEmri")).toLowerCase().trim());

            for (TeachingCourse tc : dbTcs) {
                if (tc.getCourse() == null) continue;
                String rawName = tc.getCourse().getEmriCourse() != null ? tc.getCourse().getEmriCourse().toLowerCase().trim() : "";
                if (!evAllCourseNames.contains(rawName)) {
                    System.err.println(String.format("⚠️ MISMATCH: Course '%s' (ID %d, Sem %s) is in DB for prof %s, but MISSING in Evidenca!",
                            rawName, tc.getCourse().getCourseId(), tc.getCourse().getSemester(), name));
                    mismatchCount++;
                }
            }
        }

        System.out.println("\n================================================================================");
        System.out.println(String.format("AUDIT SUMMARY: Checked %d active professors with teaching courses.", totalChecked));
        System.out.println(String.format("Total Missing Course Mismatches: %d", mismatchCount));
        System.out.println("================================================================================");
    }
}
