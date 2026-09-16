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

        String semester = req.getSemester() != null ? req.getSemester() : "1";
        Integer duration = req.getDurationWeeks() != null ? req.getDurationWeeks() : 18;
        Integer defaultHours = req.getTotalHours() != null ? req.getTotalHours() : 30;
        Integer lectureHours = req.getLectureHours() != null ? req.getLectureHours() : defaultHours;
        Integer seminarHours = req.getSeminarHours() != null ? req.getSeminarHours() : defaultHours;
        Integer labHours = req.getLabHours() != null ? req.getLabHours() : (req.getTotalHours() != null ? req.getTotalHours() : 15);

        List<TeachingCourse> existing = teachingCourseRepository.findByCourseCourseId(req.getCourseId());
        List<TeachingCourse> existingLeksion = existing.stream()
                .filter(tc -> "LEKSION".equalsIgnoreCase(tc.getRoleType()))
                .collect(Collectors.toList());
        List<TeachingCourse> existingSeminar = existing.stream()
                .filter(tc -> "SEMINAR".equalsIgnoreCase(tc.getRoleType()))
                .collect(Collectors.toList());
        List<TeachingCourse> existingLab = existing.stream()
                .filter(tc -> "LABORATOR".equalsIgnoreCase(tc.getRoleType()))
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
                    tc.setSemester(semester);
                    tc.setDurationWeeks(duration);
                    tc.setTotalHours(lectureHours);
                    tc.setClasses(lectureClasses);
                    teachingCourseRepository.save(tc);
                } else {
                    TeachingCourse tc = TeachingCourse.builder()
                            .course(course)
                            .professor(prof)
                            .roleType("LEKSION")
                            .semester(semester)
                            .durationWeeks(duration)
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
                            tc.setSemester(semester);
                            tc.setDurationWeeks(duration);
                            tc.setTotalHours(seminarHours);
                            tc.setClasses(semClasses);
                            teachingCourseRepository.save(tc);
                        } else {
                            TeachingCourse tc = TeachingCourse.builder()
                                    .course(course)
                                    .professor(prof)
                                    .roleType("SEMINAR")
                                    .semester(semester)
                                    .durationWeeks(duration)
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
                            tc.setSemester(semester);
                            tc.setDurationWeeks(duration);
                            tc.setTotalHours(labHours);
                            tc.setClasses(labClasses);
                            teachingCourseRepository.save(tc);
                        } else {
                            TeachingCourse tc = TeachingCourse.builder()
                                    .course(course)
                                    .professor(prof)
                                    .roleType("LABORATOR")
                                    .semester(semester)
                                    .durationWeeks(duration)
                                    .totalHours(labHours)
                                    .classes(labClasses)
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
        List<TeachingAllocationDto> all = getAllAllocations();
        List<Map<String, Object>> list = new ArrayList<>();
        for (TeachingAllocationDto alloc : all) {
            boolean hasLecture = alloc.getLectureProfessor() != null &&
                    (professorId == null || professorId.equals(alloc.getLectureProfessor().getProfessorId()));
            boolean hasSeminar = alloc.getSeminarProfessors() != null && alloc.getSeminarProfessors().stream()
                    .anyMatch(sp -> professorId == null || professorId.equals(sp.getProfessorId()));
            boolean hasLab = alloc.getLabProfessors() != null && alloc.getLabProfessors().stream()
                    .anyMatch(lp -> professorId == null || professorId.equals(lp.getProfessorId()));

            if (professorId != null) {
                if (hasLecture || hasSeminar || hasLab) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("professorId", professorId);
                    map.put("courseId", alloc.getCourseId());
                    map.put("courseEmri", alloc.getCourseEmri());
                    map.put("programEmri", alloc.getProgramName());
                    map.put("kredite", alloc.getCourseKredite());
                    map.put("oreLeksion", hasLecture ? (alloc.getLectureHours() != null ? alloc.getLectureHours() : 30) : 0);
                    map.put("oreSeminar", hasSeminar ? (alloc.getSeminarHours() != null ? alloc.getSeminarHours() : 30) : 0);
                    map.put("oreLaborator", hasLab ? (alloc.getLabHours() != null ? alloc.getLabHours() : 15) : 0);
                    list.add(map);
                }
            } else {
                Set<Integer> pIds = new HashSet<>();
                if (alloc.getLectureProfessor() != null && alloc.getLectureProfessor().getProfessorId() != null) {
                    pIds.add(alloc.getLectureProfessor().getProfessorId());
                }
                if (alloc.getSeminarProfessors() != null) {
                    alloc.getSeminarProfessors().forEach(sp -> {
                        if (sp.getProfessorId() != null) pIds.add(sp.getProfessorId());
                    });
                }
                if (alloc.getLabProfessors() != null) {
                    alloc.getLabProfessors().forEach(lp -> {
                        if (lp.getProfessorId() != null) pIds.add(lp.getProfessorId());
                    });
                }
                for (Integer pId : pIds) {
                    boolean pLec = alloc.getLectureProfessor() != null && pId.equals(alloc.getLectureProfessor().getProfessorId());
                    boolean pSem = alloc.getSeminarProfessors() != null && alloc.getSeminarProfessors().stream().anyMatch(sp -> pId.equals(sp.getProfessorId()));
                    boolean pLab = alloc.getLabProfessors() != null && alloc.getLabProfessors().stream().anyMatch(lp -> pId.equals(lp.getProfessorId()));

                    Map<String, Object> map = new HashMap<>();
                    map.put("professorId", pId);
                    map.put("courseId", alloc.getCourseId());
                    map.put("courseEmri", alloc.getCourseEmri());
                    map.put("programEmri", alloc.getProgramName());
                    map.put("kredite", alloc.getCourseKredite());
                    map.put("oreLeksion", pLec ? (alloc.getLectureHours() != null ? alloc.getLectureHours() : 30) : 0);
                    map.put("oreSeminar", pSem ? (alloc.getSeminarHours() != null ? alloc.getSeminarHours() : 30) : 0);
                    map.put("oreLaborator", pLab ? (alloc.getLabHours() != null ? alloc.getLabHours() : 15) : 0);
                    list.add(map);
                }
            }
        }
        return list;
    }
}
