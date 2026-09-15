package Savina.ftiApp.service;

import Savina.ftiApp.dto.responseDTO.StudentAttendanceDto;
import Savina.ftiApp.dto.responseDTO.StudentGradeDto;
import Savina.ftiApp.dto.responseDTO.StudentProfileDto;
import Savina.ftiApp.entity.Attendance;
import Savina.ftiApp.entity.Course;
import Savina.ftiApp.entity.Grade;
import Savina.ftiApp.entity.Student;
import Savina.ftiApp.mapper.StudentMapper;
import Savina.ftiApp.repository.AttendanceRepository;
import Savina.ftiApp.repository.CourseRepository;
import Savina.ftiApp.repository.GradeRepository;
import Savina.ftiApp.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepo;
    private final GradeRepository gradeRepo;
    private final CourseRepository courseRepo;
    private final AttendanceRepository attendanceRepo;
    private final StudentMapper studentMapper;

    @Transactional(readOnly = true)
    public StudentProfileDto getStudentProfile(Integer userId, String email) {
        Student student = null;

        if (userId != null) {
            student = studentRepo.findByUserUserId(userId).orElse(null);
        }
        if (student == null && email != null && !email.isBlank()) {
            student = studentRepo.findByUserEmailIgnoreCase(email.trim()).orElse(null);
        }
        if (student == null) {
            student = studentRepo.findAll().stream().findFirst().orElse(null);
        }

        if (student == null) {
            return studentMapper.toEmptyProfileDto();
        }

        List<Grade> grades = gradeRepo.findByStudentStudentId(student.getStudentId());

        double totalWeightedPoints = 0.0;
        int sumCreditsFromGrades = 0;

        for (Grade g : grades) {
            if (g.getGrade() != null && g.getTeachingCourse() != null && g.getTeachingCourse().getCourse() != null) {
                double gradeVal = g.getGrade().doubleValue();
                Integer courseCredits = g.getTeachingCourse().getCourse().getKredite();
                int krediteVal = courseCredits != null ? courseCredits : 0;

                if (krediteVal > 0 && gradeVal >= 5.0) {
                    totalWeightedPoints += (gradeVal * krediteVal);
                    sumCreditsFromGrades += krediteVal;
                }
            }
        }

        Integer totalKredite = sumCreditsFromGrades;

        double mesatarja = 0.0;
        String mesatarjaFormatted = "0.00";

        if (sumCreditsFromGrades > 0) {
            mesatarja = totalWeightedPoints / sumCreditsFromGrades;
            DecimalFormat df = new DecimalFormat("0.00#", new DecimalFormatSymbols(Locale.US));
            df.setRoundingMode(RoundingMode.HALF_UP);
            mesatarjaFormatted = df.format(mesatarja);
        }

        return studentMapper.toStudentProfileDto(student, totalKredite, mesatarja, mesatarjaFormatted);
    }

    @Transactional(readOnly = true)
    public List<StudentGradeDto> getStudentGrades(Integer userId, String email) {
        Student student = null;
        if (userId != null) {
            student = studentRepo.findByUserUserId(userId).orElse(null);
        }
        if (student == null && email != null && !email.isBlank()) {
            student = studentRepo.findByUserEmailIgnoreCase(email.trim()).orElse(null);
        }
        if (student == null) {
            student = studentRepo.findAll().stream().findFirst().orElse(null);
        }

        if (student == null) {
            return Collections.emptyList();
        }

        List<Grade> grades = gradeRepo.findByStudentStudentId(student.getStudentId());
        Map<Integer, Grade> courseGradeMap = new HashMap<>();

        for (Grade g : grades) {
            if (g.getTeachingCourse() != null && g.getTeachingCourse().getCourse() != null) {
                courseGradeMap.put(g.getTeachingCourse().getCourse().getCourseId(), g);
            }
        }

        List<Course> allCourses = new ArrayList<>();
        if (student.getProgram() != null) {
            allCourses.addAll(courseRepo.findByProgramProgramId(student.getProgram().getProgramId()));
            if (student.getProgram().getDepartment() != null) {
                allCourses.addAll(courseRepo.findByDepartmentsDepartmentId(student.getProgram().getDepartment().getDepartmentId()));
            }
        }

        if (allCourses.isEmpty()) {
            allCourses = courseRepo.findAll();
        }

        Set<Integer> processedCourseIds = new HashSet<>();
        List<StudentGradeDto> dtoList = new ArrayList<>();

        for (Course c : allCourses) {
            if (processedCourseIds.contains(c.getCourseId())) {
                continue;
            }
            processedCourseIds.add(c.getCourseId());

            Grade g = courseGradeMap.get(c.getCourseId());

            Integer gradeId = g != null ? g.getGradeId() : null;
            String notaStr = "X";
            Double notaVal = null;
            String status = "PENDING";

            if (g != null && g.getGrade() != null) {
                notaVal = g.getGrade().doubleValue();
                if (notaVal % 1 == 0) {
                    notaStr = String.valueOf(notaVal.intValue());
                } else {
                    notaStr = String.valueOf(notaVal);
                }
                status = g.getStatus() != null ? g.getStatus() : "PASSED";
            }

            dtoList.add(studentMapper.toStudentGradeDto(
                    gradeId,
                    c.getCourseId(),
                    c.getEmriCourse(),
                    c.getKredite(),
                    notaStr,
                    notaVal,
                    c.getStudyYear() != null ? c.getStudyYear() : (student.getVitStudimit() != null ? student.getVitStudimit() : 1),
                    status,
                    g != null && g.getDateGiven() != null ? g.getDateGiven().toString() : ""
            ));
        }

        for (Grade g : grades) {
            if (g.getTeachingCourse() != null && g.getTeachingCourse().getCourse() != null) {
                Course c = g.getTeachingCourse().getCourse();
                if (!processedCourseIds.contains(c.getCourseId())) {
                    processedCourseIds.add(c.getCourseId());

                    String notaStr = "X";
                    Double notaVal = null;
                    if (g.getGrade() != null) {
                        notaVal = g.getGrade().doubleValue();
                        if (notaVal % 1 == 0) {
                            notaStr = String.valueOf(notaVal.intValue());
                        } else {
                            notaStr = String.valueOf(notaVal);
                        }
                    }

                    dtoList.add(studentMapper.toStudentGradeDto(
                            g.getGradeId(),
                            c.getCourseId(),
                            c.getEmriCourse(),
                            c.getKredite(),
                            notaStr,
                            notaVal,
                            c.getStudyYear() != null ? c.getStudyYear() : (student.getVitStudimit() != null ? student.getVitStudimit() : 1),
                            g.getStatus() != null ? g.getStatus() : "PASSED",
                            g.getDateGiven() != null ? g.getDateGiven().toString() : ""
                    ));
                }
            }
        }

        return dtoList;
    }

    @Transactional(readOnly = true)
    public List<StudentAttendanceDto> getStudentAttendances(Integer userId, String email) {
        Student student = null;
        if (userId != null) {
            student = studentRepo.findByUserUserId(userId).orElse(null);
        }
        if (student == null && email != null && !email.isBlank()) {
            student = studentRepo.findByUserEmailIgnoreCase(email.trim()).orElse(null);
        }
        if (student == null) {
            student = studentRepo.findAll().stream().findFirst().orElse(null);
        }

        if (student == null) {
            return Collections.emptyList();
        }

        int currentStudentYear = student.getVitStudimit() != null ? student.getVitStudimit() : 1;

        List<Attendance> attendances = attendanceRepo.findByStudentStudentId(student.getStudentId());

        Map<Integer, List<String>> seminarAbsencesMap = new HashMap<>();
        Map<Integer, List<String>> labAbsencesMap = new HashMap<>();
        Map<Integer, Integer> courseSemTotalMap = new HashMap<>();
        Map<Integer, Integer> courseLabTotalMap = new HashMap<>();

        for (Attendance a : attendances) {
            if (a.getTeachingCourse() != null && a.getTeachingCourse().getCourse() != null) {
                Integer courseId = a.getTeachingCourse().getCourse().getCourseId();
                Integer tHours = a.getTeachingCourse().getTotalHours();
                Integer duration = a.getTeachingCourse().getDurationWeeks();
                int totalH = (tHours != null && tHours > 0) ? tHours : ((duration != null && duration > 0) ? duration : 15);
                String roleType = a.getTeachingCourse().getRoleType() != null ? a.getTeachingCourse().getRoleType().toUpperCase() : "";

                if (roleType.contains("LAB")) {
                    courseLabTotalMap.put(courseId, totalH);
                } else {
                    courseSemTotalMap.put(courseId, totalH);
                }

                if (a.getStatus() == null || a.getStatus() == 0) {
                    String dateStr = a.getDataAttendance() != null ? studentMapper.formatDateAlbanian(a.getDataAttendance()) : "-";
                    if (roleType.contains("LAB")) {
                        labAbsencesMap.computeIfAbsent(courseId, k -> new ArrayList<>()).add(dateStr);
                    } else {
                        seminarAbsencesMap.computeIfAbsent(courseId, k -> new ArrayList<>()).add(dateStr);
                    }
                }
            }
        }

        List<Course> allCourses = new ArrayList<>();
        if (student.getProgram() != null) {
            allCourses.addAll(courseRepo.findByProgramProgramId(student.getProgram().getProgramId()));
            if (student.getProgram().getDepartment() != null) {
                allCourses.addAll(courseRepo.findByDepartmentsDepartmentId(student.getProgram().getDepartment().getDepartmentId()));
            }
        }

        if (allCourses.isEmpty()) {
            allCourses = courseRepo.findAll();
        }

        Set<Integer> processedCourseIds = new HashSet<>();
        List<StudentAttendanceDto> dtoList = new ArrayList<>();

        for (Course c : allCourses) {
            if (processedCourseIds.contains(c.getCourseId())) {
                continue;
            }

            int courseYear = c.getStudyYear() != null ? c.getStudyYear() : currentStudentYear;
            List<String> semDates = seminarAbsencesMap.getOrDefault(c.getCourseId(), Collections.emptyList());
            List<String> labDates = labAbsencesMap.getOrDefault(c.getCourseId(), Collections.emptyList());

            int semCount = semDates.size();
            int labCount = labDates.size();

            int semTotal = courseSemTotalMap.getOrDefault(c.getCourseId(), 15);
            int labTotal = courseLabTotalMap.getOrDefault(c.getCourseId(), 15);
            if (semTotal <= 0) {
                semTotal = 15;
            }
            if (labTotal <= 0) {
                labTotal = 15;
            }

            double semAbsencePercentage = (double) semCount / semTotal;
            double labAbsencePercentage = (double) labCount / labTotal;

            boolean isReAttendance = semAbsencePercentage > 0.30;

            if (courseYear == currentStudentYear || isReAttendance) {
                processedCourseIds.add(c.getCourseId());

                String semStatus = semAbsencePercentage > 0.30 ? "Rrezikon" : "Kalon";
                String labStatus = labAbsencePercentage > 0.30 ? "Rrezikon" : "Kalon";

                dtoList.add(studentMapper.toStudentAttendanceDto(
                        c.getCourseId(),
                        c.getEmriCourse(),
                        semCount,
                        semTotal,
                        semStatus,
                        semDates,
                        labCount,
                        labTotal,
                        labStatus,
                        labDates
                ));
            }
        }

        return dtoList;
    }

    @Transactional
    public void toggleImprovementRequest(Integer userId, String email, Integer courseId, boolean isImprovement) {
        Student student = null;
        if (userId != null) {
            student = studentRepo.findByUserUserId(userId).orElse(null);
        }
        if (student == null && email != null && !email.isBlank()) {
            student = studentRepo.findByUserEmailIgnoreCase(email.trim()).orElse(null);
        }
        if (student == null) {
            student = studentRepo.findAll().stream().findFirst().orElse(null);
        }
        if (student == null) {
            throw new IllegalArgumentException("Studenti nuk u gjet.");
        }

        List<Grade> studentGrades = gradeRepo.findByStudentStudentId(student.getStudentId());

        if (isImprovement) {
            long currentImprovements = studentGrades.stream()
                    .filter(g -> "IMPROVED".equalsIgnoreCase(g.getStatus()) 
                              || "PERMIRESIM".equalsIgnoreCase(g.getStatus()) 
                              || "P".equalsIgnoreCase(g.getStatus())
                              || "PERMIRESUAR".equalsIgnoreCase(g.getStatus()))
                    .count();
            if (currentImprovements >= 2) {
                throw new IllegalArgumentException("Keni arritur limitin maksimal prej 2 lendesh per permiresim.");
            }
        }

        Grade targetGrade = studentGrades.stream()
                .filter(g -> g.getTeachingCourse() != null && g.getTeachingCourse().getCourse() != null && g.getTeachingCourse().getCourse().getCourseId().equals(courseId))
                .findFirst()
                .orElse(null);

        if (targetGrade == null && courseId != null) {
            targetGrade = gradeRepo.findByStudent_StudentIdAndTeachingCourse_Course_CourseId(student.getStudentId(), courseId).orElse(null);
        }

        if (targetGrade != null) {
            if (isImprovement) {
                if ("PERMIRESUAR".equalsIgnoreCase(targetGrade.getStatus())) {
                    throw new IllegalArgumentException("Kjo lëndë është përmirësuar tashmë.");
                }
                if (targetGrade.getGrade() != null && targetGrade.getGrade().doubleValue() < 5.0) {
                    throw new IllegalArgumentException("Nuk lejohet permiresimi per noten ngelese.");
                }
                targetGrade.setStatus("IMPROVED");
            } else {
                if ("IMPROVED".equalsIgnoreCase(targetGrade.getStatus())) {
                    targetGrade.setStatus("PASSED");
                }
            }
            gradeRepo.save(targetGrade);
            log.info("Student ID={} ndryshoi statusin e permiresimit per lenden ID={} ne {}", student.getStudentId(), courseId, isImprovement);
        }
    }
}
