package Savina.ftiApp.service;

import Savina.ftiApp.dto.responseDTO.TeachingAllocationDto;
import Savina.ftiApp.dto.requestDTO.TeachingAllocationRequest;
import Savina.ftiApp.entity.Classes;
import Savina.ftiApp.entity.Course;
import Savina.ftiApp.entity.Professor;
import Savina.ftiApp.entity.TeachingCourse;
import Savina.ftiApp.entity.ProfessorPreEnrollment;
import Savina.ftiApp.entity.Role;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
        Integer duration = req.getDurationWeeks() != null ? req.getDurationWeeks() : 15;

        // Existing records for this course to update in-place (prevents ORA-02292 foreign key violations)
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

        // 1. Lecture Professor (LEKSION)
        if (req.getLectureProfessorId() != null) {
            Professor prof = findOrCreateProfessor(req.getLectureProfessorId());
            if (prof != null) {
                Set<Classes> lectureClasses = new HashSet<>();
                if (req.getLectureClassIds() != null && !req.getLectureClassIds().isEmpty()) {
                    lectureClasses.addAll(classesRepository.findAllById(req.getLectureClassIds()));
                } else if (course.getProgram() != null) {
                    // Assign all classes of the course's program/year as default for Lektor
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
                    tc.setClasses(lectureClasses);
                    teachingCourseRepository.save(tc);
                } else {
                    TeachingCourse tc = TeachingCourse.builder()
                            .course(course)
                            .professor(prof)
                            .roleType("LEKSION")
                            .semester(semester)
                            .durationWeeks(duration)
                            .classes(lectureClasses)
                            .build();
                    teachingCourseRepository.save(tc);
                }
            }
        }

        // 2. Seminar Professors (SEMINAR)
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
                            tc.setClasses(semClasses);
                            teachingCourseRepository.save(tc);
                        } else {
                            TeachingCourse tc = TeachingCourse.builder()
                                    .course(course)
                                    .professor(prof)
                                    .roleType("SEMINAR")
                                    .semester(semester)
                                    .durationWeeks(duration)
                                    .classes(semClasses)
                                    .build();
                            teachingCourseRepository.save(tc);
                        }
                    }
                }
            }
        }

        // 3. Lab Professors (LABORATOR)
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
                            tc.setClasses(labClasses);
                            teachingCourseRepository.save(tc);
                        } else {
                            TeachingCourse tc = TeachingCourse.builder()
                                    .course(course)
                                    .professor(prof)
                                    .roleType("LABORATOR")
                                    .semester(semester)
                                    .durationWeeks(duration)
                                    .classes(labClasses)
                                    .build();
                            teachingCourseRepository.save(tc);
                        }
                    }
                }
            }
        }

        // Remove any unused remaining rows if any existed
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

        // Re-fetch within same transaction to return saved data
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
        if (profId == null) return null;

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
                if (progClasses != null) classesSet.addAll(progClasses);
                return classesSet;
            }

            String targetName = classGroup.trim();
            if (progClasses != null) {
                for (Classes c : progClasses) {
                    if (c.getEmriClass() != null) {
                        String emri = c.getEmriClass().trim();
                        if (emri.equalsIgnoreCase(targetName)) {
                            classesSet.add(c);
                        } else if (targetName.equalsIgnoreCase("Klasa A") && (emri.equalsIgnoreCase("A") || emri.equalsIgnoreCase("Grupi A") || emri.endsWith(" A"))) {
                            classesSet.add(c);
                        } else if (targetName.equalsIgnoreCase("Klasa B") && (emri.equalsIgnoreCase("B") || emri.equalsIgnoreCase("Grupi B") || emri.endsWith(" B"))) {
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
}
