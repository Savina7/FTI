package Savina.ftiApp.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Savina.ftiApp.dto.requestDTO.TeachingAllocationRequest;
import Savina.ftiApp.dto.responseDTO.TeachingAllocationDto;
import Savina.ftiApp.entity.Classes;
import Savina.ftiApp.entity.Course;
import Savina.ftiApp.entity.Professor;
import Savina.ftiApp.entity.ProfessorPreEnrollment;
import Savina.ftiApp.entity.Role;
import Savina.ftiApp.entity.TeachingCourse;
import Savina.ftiApp.entity.User;
import Savina.ftiApp.mapper.TeachingCourseMapper;
import Savina.ftiApp.repository.ClassesRepository;
import Savina.ftiApp.repository.CourseRepository;
import Savina.ftiApp.repository.ProfessorPreEnrollmentRepository;
import Savina.ftiApp.repository.ProfessorRepository;
import Savina.ftiApp.repository.RoleRepository;
import Savina.ftiApp.repository.TeachingCourseRepository;
import Savina.ftiApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminTeachingCourseService {

    private final TeachingCourseRepository teachingCourseRepository;
    private final CourseRepository courseRepository;
    private final ProfessorRepository professorRepository;
    private final ClassesRepository classesRepository;
    private final ProfessorPreEnrollmentRepository profEnrollmentRepo;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final TeachingCourseMapper teachingCourseMapper;
    private final AcademicYearService academicYearService;

    @Transactional(readOnly = true)
    public List<TeachingAllocationDto> getAllAllocations() {
        List<TeachingCourse> allTcs = teachingCourseRepository.findAll();
        Map<Integer, List<TeachingCourse>> groupedByCourse = allTcs.stream()
                .filter(tc -> tc.getCourse() != null)
                .collect(Collectors.groupingBy(tc -> tc.getCourse().getCourseId()));

        List<TeachingAllocationDto> result = new ArrayList<>();
        for (Map.Entry<Integer, List<TeachingCourse>> entry : groupedByCourse.entrySet()) {
            result.add(teachingCourseMapper.mapGroupToDto(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public TeachingAllocationDto getAllocationByCourseId(Integer courseId) {
        List<TeachingCourse> tcs = teachingCourseRepository.findByCourseCourseId(courseId);
        if (tcs.isEmpty()) {
            Course c = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("Lenda nuk u gjet me ID: " + courseId));
            return teachingCourseMapper.mapCourseToEmptyDto(c);
        }
        return teachingCourseMapper.mapGroupToDto(courseId, tcs);
    }

    @Transactional
    public TeachingAllocationDto saveAllocation(TeachingAllocationRequest req) {
        if (req.getCourseId() == null) {
            throw new RuntimeException("Lenda eshte e detyrueshme.");
        }

        Course course = courseRepository.findById(req.getCourseId())
                .orElseThrow(() -> new RuntimeException("Lenda nuk u gjet me ID: " + req.getCourseId()));

        if (req.getSemester() != null && !req.getSemester().isBlank()) {
            course.setSemester(req.getSemester());
        }
        if (req.getDurationWeeks() != null) {
            course.setDurationWeeks(req.getDurationWeeks());
        }
        courseRepository.save(course);

        boolean isMaster = (course.getProgram() != null && course.getProgram().getNivel() != null && course.getProgram().getNivel().toLowerCase().contains("master"));
        int durationWeeks = course.getDurationWeeks() != null ? course.getDurationWeeks() : (isMaster ? 12 : 14);

        double kL = course.getKrediteLeksion() != null ? course.getKrediteLeksion().doubleValue() : (course.getKredite() != null && course.getKredite() >= 6 ? 3.0 : 2.0);
        double kS = course.getKrediteSeminar() != null ? course.getKrediteSeminar().doubleValue() : 1.5;
        double kLb = course.getKrediteLaborator() != null ? course.getKrediteLaborator().doubleValue() : 1.0;
        double kDk = course.getKrediteDetyreKursi() != null ? course.getKrediteDetyreKursi().doubleValue() : 0.5;
        double kPr = course.getKreditePraktike() != null ? course.getKreditePraktike().doubleValue() : 0.0;

        double factorLec = isMaster ? 10.0 : 12.0;
        double factorSem = isMaster ? 12.0 : 14.0;
        double factorLab = 20.0;
        double factorCw = 5.0;

        double autoTotLec = (course.getKrediteLeksion() != null && course.getKrediteLeksion().doubleValue() == 0.0) ? 0.0 : kL * factorLec;
        double autoTotSem = (course.getKrediteSeminar() != null && course.getKrediteSeminar().doubleValue() == 0.0) ? 0.0 : kS * factorSem;
        double autoTotLab = (course.getKrediteLaborator() != null && course.getKrediteLaborator().doubleValue() == 0.0) ? 0.0 : kLb * factorLab;
        double autoTotCw = (course.getKrediteDetyreKursi() != null && course.getKrediteDetyreKursi().doubleValue() == 0.0) ? 0.0 : kDk * factorCw;
        double autoTotPr = (course.getKreditePraktike() != null && course.getKreditePraktike().doubleValue() == 0.0) ? 0.0 : kPr * 20.0;

        Double weeklyLecture = req.getWeeklyLectureHours() != null ? req.getWeeklyLectureHours() : (req.getWeeklyHours() != null ? req.getWeeklyHours() : 2.0);
        Double weeklySeminar = req.getWeeklySeminarHours() != null ? req.getWeeklySeminarHours() : (req.getWeeklyHours() != null ? req.getWeeklyHours() : 2.0);
        Double weeklyLab = req.getWeeklyLabHours() != null ? req.getWeeklyLabHours() : 1.0;
        Double weeklyCourseWork = req.getWeeklyCourseWorkHours() != null ? req.getWeeklyCourseWorkHours() : 1.0;
        Double weeklyPractice = req.getWeeklyPracticeHours() != null ? req.getWeeklyPracticeHours() : 2.0;

        Double lectureHours = req.getLectureHours() != null ? req.getLectureHours() : autoTotLec;
        Double seminarHours = req.getSeminarHours() != null ? req.getSeminarHours() : autoTotSem;
        Double labHours = req.getLabHours() != null ? req.getLabHours() : autoTotLab;
        Double courseWorkHours = req.getCourseWorkHours() != null ? req.getCourseWorkHours() : autoTotCw;
        Double practiceHours = req.getPracticeHours() != null ? req.getPracticeHours() : autoTotPr;

        String acadYear = (req.getAcademicYear() != null && !req.getAcademicYear().isBlank())
                ? req.getAcademicYear().trim() : academicYearService.getCurrentAcademicYear();

        List<TeachingCourse> existing = teachingCourseRepository.findByCourseCourseId(req.getCourseId());
        List<TeachingCourse> existingLeksion = existing.stream()
                .filter(tc -> "LEKSION".equalsIgnoreCase(tc.getRoleType()))
                .collect(Collectors.toList());
        List<TeachingCourse> existingSeminar = existing.stream()
                .filter(tc -> "SEMINAR".equalsIgnoreCase(tc.getRoleType()))
                .collect(Collectors.toList());
        List<TeachingCourse> existingLab = existing.stream()
                .filter(tc -> "LABORATOR".equalsIgnoreCase(tc.getRoleType()) || "LAB".equalsIgnoreCase(tc.getRoleType()))
                .collect(Collectors.toList());
        List<TeachingCourse> existingCourseWork = existing.stream()
                .filter(tc -> "DETYRE_KURSI".equalsIgnoreCase(tc.getRoleType()) || "DETYRE".equalsIgnoreCase(tc.getRoleType()) || "DETYRA".equalsIgnoreCase(tc.getRoleType()))
                .collect(Collectors.toList());
        List<TeachingCourse> existingPractice = existing.stream()
                .filter(tc -> "PRAKTIKE".equalsIgnoreCase(tc.getRoleType()) || "PRAKTIK".equalsIgnoreCase(tc.getRoleType()))
                .collect(Collectors.toList());

        if (req.getLectureProfessorId() != null) {
            Professor prof = findOrCreateProfessor(req.getLectureProfessorId());
            if (prof != null) {
                Set<Classes> lectureClasses = new HashSet<>();
                if (req.getLectureClassIds() != null && !req.getLectureClassIds().isEmpty()) {
                    lectureClasses.addAll(classesRepository.findAllById(req.getLectureClassIds()));
                } else if (course.getProgram() != null) {

                    List<Classes> progClasses = classesRepository.findByProgram_ProgramId(course.getProgram().getProgramId());
                    if (progClasses != null) {
                        lectureClasses.addAll(progClasses);
                    }
                }

                if (!existingLeksion.isEmpty()) {
                    TeachingCourse tc = existingLeksion.remove(0);
                    tc.setProfessor(prof);
                    tc.setAcademicYear(acadYear);
                    tc.setWeeklyHours(weeklyLecture);
                    tc.setTotalHours(lectureHours);
                    tc.setClasses(lectureClasses);
                    teachingCourseRepository.save(tc);
                } else {
                    TeachingCourse tc = TeachingCourse.builder()
                            .course(course)
                            .professor(prof)
                            .roleType("LEKSION")
                            .academicYear(acadYear)
                            .weeklyHours(weeklyLecture)
                            .totalHours(lectureHours)
                            .classes(lectureClasses)
                            .build();
                    teachingCourseRepository.save(tc);
                }
            }
        }

        if (req.getSeminars() != null) {
            for (TeachingAllocationRequest.SeminarAssignmentReq sem : req.getSeminars()) {
                if (sem.getProfessorId() != null) {
                    Professor prof = findOrCreateProfessor(sem.getProfessorId());
                    if (prof != null) {
                        Set<Classes> semClasses = resolveClassesForGroup(course, sem.getClassGroup(), sem.getClassIds());

                        if (!existingSeminar.isEmpty()) {
                            TeachingCourse tc = existingSeminar.remove(0);
                            tc.setProfessor(prof);
                            tc.setAcademicYear(acadYear);
                            tc.setWeeklyHours(weeklySeminar);
                            tc.setTotalHours(seminarHours);
                            tc.setClasses(semClasses);
                            teachingCourseRepository.save(tc);
                        } else {
                            TeachingCourse tc = TeachingCourse.builder()
                                    .course(course)
                                    .professor(prof)
                                    .roleType("SEMINAR")
                                    .academicYear(acadYear)
                                    .weeklyHours(weeklySeminar)
                                    .totalHours(seminarHours)
                                    .classes(semClasses)
                                    .build();
                            teachingCourseRepository.save(tc);
                        }
                    }
                }
            }
        }

        if (Boolean.TRUE.equals(req.getHasLab()) && req.getLabs() != null) {
            for (TeachingAllocationRequest.LabAssignmentReq lab : req.getLabs()) {
                if (lab.getProfessorId() != null) {
                    Professor prof = findOrCreateProfessor(lab.getProfessorId());
                    if (prof != null) {
                        Set<Classes> labClasses = resolveClassesForGroup(course, lab.getClassGroup(), lab.getClassIds());

                        if (!existingLab.isEmpty()) {
                            TeachingCourse tc = existingLab.remove(0);
                            tc.setProfessor(prof);
                            tc.setAcademicYear(acadYear);
                            tc.setWeeklyHours(weeklyLab);
                            tc.setTotalHours(labHours);
                            tc.setClasses(labClasses);
                            teachingCourseRepository.save(tc);
                        } else {
                            TeachingCourse tc = TeachingCourse.builder()
                                    .course(course)
                                    .professor(prof)
                                    .roleType("LABORATOR")
                                    .academicYear(acadYear)
                                    .weeklyHours(weeklyLab)
                                    .totalHours(labHours)
                                    .classes(labClasses)
                                    .build();
                            teachingCourseRepository.save(tc);
                        }
                    }
                }
            }
        }

        if (Boolean.TRUE.equals(req.getHasCourseWork()) && req.getCourseWorks() != null) {
            for (TeachingAllocationRequest.CourseWorkAssignmentReq cw : req.getCourseWorks()) {
                if (cw.getProfessorId() != null) {
                    Professor prof = findOrCreateProfessor(cw.getProfessorId());
                    if (prof != null) {
                        Set<Classes> cwClasses = resolveClassesForGroup(course, cw.getClassGroup(), cw.getClassIds());

                        if (!existingCourseWork.isEmpty()) {
                            TeachingCourse tc = existingCourseWork.remove(0);
                            tc.setProfessor(prof);
                            tc.setAcademicYear(acadYear);
                            tc.setWeeklyHours(weeklyCourseWork);
                            tc.setTotalHours(courseWorkHours);
                            tc.setClasses(cwClasses);
                            teachingCourseRepository.save(tc);
                        } else {
                            TeachingCourse tc = TeachingCourse.builder()
                                    .course(course)
                                    .professor(prof)
                                    .roleType("DETYRE_KURSI")
                                    .academicYear(acadYear)
                                    .weeklyHours(weeklyCourseWork)
                                    .totalHours(courseWorkHours)
                                    .classes(cwClasses)
                                    .build();
                            teachingCourseRepository.save(tc);
                        }
                    }
                }
            }
        }

        if (Boolean.TRUE.equals(req.getHasPractice()) && req.getPractices() != null) {
            for (TeachingAllocationRequest.PracticeAssignmentReq pr : req.getPractices()) {
                if (pr.getProfessorId() != null) {
                    Professor prof = findOrCreateProfessor(pr.getProfessorId());
                    if (prof != null) {
                        Set<Classes> prClasses = resolveClassesForGroup(course, pr.getClassGroup(), pr.getClassIds());

                        if (!existingPractice.isEmpty()) {
                            TeachingCourse tc = existingPractice.remove(0);
                            tc.setProfessor(prof);
                            tc.setAcademicYear(acadYear);
                            tc.setWeeklyHours(weeklyPractice);
                            tc.setTotalHours(practiceHours);
                            tc.setClasses(prClasses);
                            teachingCourseRepository.save(tc);
                        } else {
                            TeachingCourse tc = TeachingCourse.builder()
                                    .course(course)
                                    .professor(prof)
                                    .roleType("PRAKTIKE")
                                    .academicYear(acadYear)
                                    .weeklyHours(weeklyPractice)
                                    .totalHours(practiceHours)
                                    .classes(prClasses)
                                    .build();
                            teachingCourseRepository.save(tc);
                        }
                    }
                }
            }
        }

        List<TeachingCourse> toDelete = new ArrayList<>();
        toDelete.addAll(existingLeksion);
        toDelete.addAll(existingSeminar);
        toDelete.addAll(existingLab);
        toDelete.addAll(existingCourseWork);
        toDelete.addAll(existingPractice);
        for (TeachingCourse tc : toDelete) {
            tc.getClasses().clear();
            teachingCourseRepository.save(tc);
        }
        if (!toDelete.isEmpty()) {
            teachingCourseRepository.deleteAll(toDelete);
        }

        teachingCourseRepository.flush();

        List<TeachingCourse> saved = teachingCourseRepository.findByCourseCourseId(req.getCourseId());
        if (saved.isEmpty()) {
            return teachingCourseMapper.mapCourseToEmptyDto(course);
        }
        return teachingCourseMapper.mapGroupToDto(req.getCourseId(), saved);
    }

    @Transactional
    public void deleteAllocationByCourseId(Integer courseId) {
        List<TeachingCourse> existing = teachingCourseRepository.findByCourseCourseId(courseId);
        for (TeachingCourse tc : existing) {
            tc.getClasses().clear();
            teachingCourseRepository.save(tc);
        }
        teachingCourseRepository.deleteAll(existing);
        teachingCourseRepository.flush();
    }

    private Professor findOrCreateProfessor(Integer profId) {
        if (profId == null) {
            return null;
        }

        Optional<Professor> pOpt = professorRepository.findById(profId);
        if (pOpt.isPresent()) {
            return pOpt.get();
        }

        Optional<ProfessorPreEnrollment> peOpt = profEnrollmentRepo.findById(profId);
        if (peOpt.isPresent()) {
            ProfessorPreEnrollment pe = peOpt.get();
            User user = userRepo.findByEmail(pe.getEmail()).orElseGet(() -> {
                Role profRole = roleRepo.findByRoleName("PROFESSOR").orElse(null);
                User u = User.builder()
                        .emri(pe.getEmri())
                        .mbiemri(pe.getMbiemri())
                        .email(pe.getEmail())
                        .password("123456")
                        .verified("Y")
                        .status("VERIFIKUAR")
                        .createdAt(java.time.LocalDate.now())
                        .roles(profRole != null ? Set.of(profRole) : Set.of())
                        .build();
                return userRepo.save(u);
            });

            Professor newProf = Professor.builder()
                    .user(user)
                    .department(pe.getDepartment())
                    .build();
            return professorRepository.save(newProf);
        }

        return null;
    }

    private Set<Classes> resolveClassesForGroup(Course course, String classGroup, List<Integer> classIds) {
        Set<Classes> classesSet = new HashSet<>();
        if (classIds != null && !classIds.isEmpty()) {
            classesSet.addAll(classesRepository.findAllById(classIds));
            return classesSet;
        }

        if (course != null && course.getProgram() != null) {
            Savina.ftiApp.entity.Program prog = course.getProgram();
            Integer studyYear = course.getStudyYear() != null ? course.getStudyYear() : 1;
            List<Classes> progClasses = classesRepository.findByProgram_ProgramId(prog.getProgramId());
            if (progClasses != null && studyYear != null) {
                progClasses = progClasses.stream()
                        .filter(c -> c.getVitStudimit() == null || c.getVitStudimit().equals(studyYear))
                        .collect(Collectors.toList());
            }

            if (classGroup == null || classGroup.isBlank() || "Te gjitha klasat".equalsIgnoreCase(classGroup) || "Klasa A & B".equalsIgnoreCase(classGroup)) {
                if (progClasses != null) {
                    classesSet.addAll(progClasses);
                }
                return classesSet;
            }

            String targetName = classGroup.trim();
            if (progClasses != null) {
                for (Classes c : progClasses) {
                    if (c.getEmriClass() != null) {
                        String emri = c.getEmriClass().trim();
                        if (emri.equalsIgnoreCase(targetName)) {
                            classesSet.add(c);
                        } else if ((targetName.equalsIgnoreCase("Klasa A") || targetName.equalsIgnoreCase("Grupi A")) && (emri.equalsIgnoreCase("A") || emri.equalsIgnoreCase("Grupi A") || emri.endsWith(" A"))) {
                            classesSet.add(c);
                        } else if ((targetName.equalsIgnoreCase("Klasa B") || targetName.equalsIgnoreCase("Grupi B")) && (emri.equalsIgnoreCase("B") || emri.equalsIgnoreCase("Grupi B") || emri.endsWith(" B"))) {
                            classesSet.add(c);
                        } else if ((targetName.equalsIgnoreCase("Klasa C") || targetName.equalsIgnoreCase("Grupi C")) && (emri.equalsIgnoreCase("C") || emri.equalsIgnoreCase("Grupi C") || emri.endsWith(" C"))) {
                            classesSet.add(c);
                        } else if ((targetName.equalsIgnoreCase("Klasa D") || targetName.equalsIgnoreCase("Grupi D")) && (emri.equalsIgnoreCase("D") || emri.equalsIgnoreCase("Grupi D") || emri.endsWith(" D"))) {
                            classesSet.add(c);
                        }
                    }
                }
            }

            if (classesSet.isEmpty()) {
                Classes newClass = classesRepository.save(Classes.builder()
                        .program(prog)
                        .vitStudimit(studyYear)
                        .emriClass(targetName)
                        .build());
                classesSet.add(newClass);
            }
        }
        return classesSet;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getEvidenca(Integer professorId) {
        return getEvidenca(professorId, null, null);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getEvidenca(Integer professorId, String semester, String academicYear) {
        List<TeachingCourse> tcs;
        if (professorId != null) {
            try {
                tcs = teachingCourseRepository.findByProfessorIdWithDetails(professorId);
            } catch (Exception ex) {
                tcs = teachingCourseRepository.findByProfessorProfessorId(professorId);
            }
        } else {
            tcs = teachingCourseRepository.findAll();
        }

        // Group by course name so same-named courses across programs merge cleanly
        Map<String, Map<String, Object>> grouped = new java.util.LinkedHashMap<>();

        for (TeachingCourse tc : tcs) {
            if (tc.getCourse() == null) continue;
            Course c = tc.getCourse();

            // Filter semester if provided
            if (semester != null && !semester.isBlank()) {
                String cSem = c.getSemester() != null ? c.getSemester().trim() : "1";
                if (!matchesSemesterHelper(cSem, semester)) {
                    continue;
                }
            }

            // Filter academic year if provided
            if (academicYear != null && !academicYear.isBlank()) {
                String tcAy = tc.getAcademicYear() != null ? tc.getAcademicYear().trim() : "";
                if (!tcAy.isEmpty() && !tcAy.equalsIgnoreCase(academicYear.trim())) {
                    continue;
                }
            }

            String courseName = c.getEmriCourse() != null ? c.getEmriCourse().trim() : ("Lenda " + c.getCourseId());
            String groupKey = courseName.toLowerCase();

            Map<String, Object> entry = grouped.computeIfAbsent(groupKey, k -> {
                Map<String, Object> map = new java.util.LinkedHashMap<>();
                map.put("courseId", c.getCourseId());
                map.put("courseEmri", courseName);
                String prog = (c.getProgram() != null && c.getProgram().getSpecializimi() != null)
                        ? c.getProgram().getSpecializimi()
                        : "—";
                Set<String> progs = new java.util.LinkedHashSet<>();
                progs.add(prog);
                map.put("_programs", progs);
                map.put("programEmri", prog);
                map.put("semester", c.getSemester() != null ? c.getSemester() : "1");
                map.put("academicYear", tc.getAcademicYear() != null ? tc.getAcademicYear() : academicYearService.getCurrentAcademicYear());
                map.put("kredite", c.getKredite() != null ? c.getKredite() : 6);
                map.put("krediteLeksion", c.getKrediteLeksion() != null ? c.getKrediteLeksion().doubleValue() : null);
                map.put("krediteSeminar", c.getKrediteSeminar() != null ? c.getKrediteSeminar().doubleValue() : null);
                map.put("krediteLaborator", c.getKrediteLaborator() != null ? c.getKrediteLaborator().doubleValue() : null);
                map.put("krediteDetyreKursi", c.getKrediteDetyreKursi() != null ? c.getKrediteDetyreKursi().doubleValue() : null);
                map.put("kreditePraktike", c.getKreditePraktike() != null ? c.getKreditePraktike().doubleValue() : null);
                map.put("studyYear", c.getStudyYear());
                map.put("oreLeksion", 0.0);
                map.put("oreSeminar", 0.0);
                map.put("oreLaborator", 0.0);
                map.put("oreDetyreKursi", 0.0);
                map.put("orePraktike", 0.0);
                map.put("grupiLeksion", "");
                map.put("grupiSeminar", "");
                map.put("grupiLaborator", "");
                map.put("grupiDetyreKursi", "");
                map.put("grupiPraktike", "");
                return map;
            });

            if (c.getProgram() != null && c.getProgram().getSpecializimi() != null) {
                @SuppressWarnings("unchecked")
                Set<String> progs = (Set<String>) entry.get("_programs");
                progs.add(c.getProgram().getSpecializimi());
                entry.put("programEmri", String.join(" / ", progs));
            }

            String role = tc.getRoleType() != null ? tc.getRoleType().trim().toUpperCase() : "LEKSION";

            // Extract class groups
            List<String> cNames = (tc.getClasses() != null && !tc.getClasses().isEmpty())
                    ? tc.getClasses().stream()
                        .map(Classes::getEmriClass)
                        .filter(java.util.Objects::nonNull)
                        .map(AdminTeachingCourseService::formatSingleGroupHelper)
                        .filter(s -> !s.isEmpty())
                        .distinct()
                        .collect(Collectors.toList())
                    : List.of();

            int classCount = !cNames.isEmpty() ? cNames.size() : 1;
            String groupStr = !cNames.isEmpty() ? String.join(", ", cNames) : "A";

            boolean isMaster = (c.getProgram() != null && c.getProgram().getNivel() != null && c.getProgram().getNivel().toLowerCase().contains("master"));
            double factorLec = isMaster ? 10.0 : 12.0;
            double factorSem = isMaster ? 12.0 : 14.0;
            double factorLab = 20.0;
            double factorCw = 5.0;

            double kL = c.getKrediteLeksion() != null ? c.getKrediteLeksion().doubleValue() : (c.getKredite() != null && c.getKredite() >= 6 ? 3.0 : 2.0);
            double kS = c.getKrediteSeminar() != null ? c.getKrediteSeminar().doubleValue() : 1.5;
            double kLb = c.getKrediteLaborator() != null ? c.getKrediteLaborator().doubleValue() : 1.0;
            double kDk = c.getKrediteDetyreKursi() != null ? c.getKrediteDetyreKursi().doubleValue() : 0.5;
            double kPr = c.getKreditePraktike() != null ? c.getKreditePraktike().doubleValue() : 0.0;

            double autoTotLec = (c.getKrediteLeksion() != null && c.getKrediteLeksion().doubleValue() == 0.0) ? 0.0 : kL * factorLec;
            double autoTotSem = (c.getKrediteSeminar() != null && c.getKrediteSeminar().doubleValue() == 0.0) ? 0.0 : kS * factorSem;
            double autoTotLab = (c.getKrediteLaborator() != null && c.getKrediteLaborator().doubleValue() == 0.0) ? 0.0 : kLb * factorLab;
            double autoTotCw = (c.getKrediteDetyreKursi() != null && c.getKrediteDetyreKursi().doubleValue() == 0.0) ? 0.0 : kDk * factorCw;
            double autoTotPr = (c.getKreditePraktike() != null && c.getKreditePraktike().doubleValue() == 0.0) ? 0.0 : kPr * 20.0;

            if (role.contains("LEK")) {
                double baseHrs = tc.getTotalHours() != null ? tc.getTotalHours() : autoTotLec;
                entry.put("oreLeksion", (Double) entry.get("oreLeksion") + baseHrs);
                entry.put("grupiLeksion", "Të gjitha klasat");
            } else if (role.contains("SEM")) {
                double baseHrs = tc.getTotalHours() != null ? tc.getTotalHours() : autoTotSem;
                entry.put("oreSeminar", (Double) entry.get("oreSeminar") + (baseHrs * classCount));
                entry.put("grupiSeminar", mergeGroupsHelper((String) entry.get("grupiSeminar"), groupStr));
            } else if (role.contains("LAB")) {
                double baseHrs = tc.getTotalHours() != null ? tc.getTotalHours() : autoTotLab;
                entry.put("oreLaborator", (Double) entry.get("oreLaborator") + (baseHrs * classCount));
                entry.put("grupiLaborator", mergeGroupsHelper((String) entry.get("grupiLaborator"), groupStr));
            } else if (role.contains("DETYR")) {
                double baseHrs = tc.getTotalHours() != null ? tc.getTotalHours() : autoTotCw;
                entry.put("oreDetyreKursi", (Double) entry.get("oreDetyreKursi") + (baseHrs * classCount));
                entry.put("grupiDetyreKursi", mergeGroupsHelper((String) entry.get("grupiDetyreKursi"), groupStr));
            } else if (role.contains("PRAKTIK")) {
                double baseHrs = tc.getTotalHours() != null ? tc.getTotalHours() : autoTotPr;
                entry.put("orePraktike", (Double) entry.get("orePraktike") + (baseHrs * classCount));
                entry.put("grupiPraktike", mergeGroupsHelper((String) entry.get("grupiPraktike"), groupStr));
            }
        }

        // Clean up temporary helper fields and return list (vetem lendet ku pedagogu ka ore reale > 0)
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> m : grouped.values()) {
            m.remove("_programs");
            double totalOre = (Double) m.getOrDefault("oreLeksion", 0.0)
                    + (Double) m.getOrDefault("oreSeminar", 0.0)
                    + (Double) m.getOrDefault("oreLaborator", 0.0)
                    + (Double) m.getOrDefault("oreDetyreKursi", 0.0)
                    + (Double) m.getOrDefault("orePraktike", 0.0);
            if (totalOre > 0) {
                result.add(m);
            }
        }
        return result;
    }

    private static String formatSingleGroupHelper(String name) {
        if (name == null) return "";
        String s = name.trim();
        if (s.toLowerCase().startsWith("grupi ") || s.toLowerCase().startsWith("grup ")) {
            return s.substring(6).trim();
        }
        if (s.toLowerCase().startsWith("inxhinieri ") && s.contains("-")) {
            return s.substring(s.indexOf('-') + 1).replace("Grupi", "").replace("grupi", "").trim();
        }
        return s;
    }

    private static String mergeGroupsHelper(String g1, String g2) {
        if (g1 == null || g1.isBlank()) return g2 != null ? g2 : "";
        if (g2 == null || g2.isBlank()) return g1;
        Set<String> set = new java.util.LinkedHashSet<>();
        for (String p : g1.split(",")) if (!p.trim().isEmpty()) set.add(p.trim());
        for (String p : g2.split(",")) if (!p.trim().isEmpty()) set.add(p.trim());
        return String.join(", ", set);
    }

    private static boolean matchesSemesterHelper(String courseSem, String selectedSem) {
        if (selectedSem == null || selectedSem.isBlank()) return true;
        String c = courseSem != null ? courseSem.trim().toUpperCase() : "1";
        String s = selectedSem.trim().toUpperCase();
        if (s.equals("1") || s.equals("I")) {
            return c.equals("1") || c.equals("I") || c.contains("1");
        }
        if (s.equals("2") || s.equals("II")) {
            return c.equals("2") || c.equals("II") || c.contains("2");
        }
        return c.equalsIgnoreCase(s);
    }
}
