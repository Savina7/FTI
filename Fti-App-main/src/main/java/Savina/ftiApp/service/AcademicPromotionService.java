package Savina.ftiApp.service;

import Savina.ftiApp.dto.responseDTO.PromotionResultDto;
import Savina.ftiApp.dto.responseDTO.StudentPromotionDetailDto;
import Savina.ftiApp.entity.Classes;
import Savina.ftiApp.entity.Course;
import Savina.ftiApp.entity.Grade;
import Savina.ftiApp.entity.Student;
import Savina.ftiApp.repository.ClassesRepository;
import Savina.ftiApp.repository.GradeRepository;
import Savina.ftiApp.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AcademicPromotionService {

    private final StudentRepository studentRepository;
    private final GradeRepository gradeRepository;
    private final ClassesRepository classesRepository;
    private final AcademicYearService academicYearService;

    @Transactional
    public PromotionResultDto promoteAllStudents() {
        List<Student> students = studentRepository.findAll();

        int promoted1To2 = 0;
        int promoted2To3 = 0;
        int graduated = 0;
        int repeating1 = 0;
        int repeating2 = 0;
        int repeating3 = 0;
        int totalProcessed = 0;

        List<StudentPromotionDetailDto> details = new ArrayList<>();

        for (Student student : students) {

            if ("GRADUATED".equalsIgnoreCase(student.getStatus())) {
                continue;
            }

            totalProcessed++;

            int oldYear = student.getVitStudimit() != null ? student.getVitStudimit() : 1;
            String oldStatus = student.getStatus() != null ? student.getStatus() : "ACTIVE";

            List<Grade> grades = gradeRepository.findByStudentStudentId(student.getStudentId());

            Map<Integer, Course> passedCourses = new HashMap<>();

            for (Grade g : grades) {
                if (g.getTeachingCourse() != null && g.getTeachingCourse().getCourse() != null) {
                    Course c = g.getTeachingCourse().getCourse();
                    boolean isPassed = false;

                    if (g.getGrade() != null && g.getGrade().compareTo(BigDecimal.valueOf(5.0)) >= 0) {
                        isPassed = true;
                    } else if ("PASSED".equalsIgnoreCase(g.getStatus()) || "KALUAR".equalsIgnoreCase(g.getStatus()) || "PERMIRESUAR".equalsIgnoreCase(g.getStatus())) {
                        isPassed = true;
                    }

                    if (isPassed) {
                        passedCourses.put(c.getCourseId(), c);
                    }
                }
            }

            int creditsYear1 = 0;
            int creditsYear2 = 0;
            int creditsYear3 = 0;

            for (Course c : passedCourses.values()) {
                int credits = c.getKredite() != null ? c.getKredite() : 0;
                Integer studyYear = c.getStudyYear();
                if (studyYear == null || studyYear == 1) {
                    creditsYear1 += credits;
                } else if (studyYear == 2) {
                    creditsYear2 += credits;
                } else {
                    creditsYear3 += credits;
                }
            }

            int totalCredits = creditsYear1 + creditsYear2 + creditsYear3;
            student.setTotalKredite(totalCredits);

            int newYear = oldYear;
            String newStatus = "ACTIVE";
            boolean promoted = false;
            String reason = "";

            if (oldYear == 1) {

                if (creditsYear1 >= 30) {
                    newYear = 2;
                    newStatus = "ACTIVE";
                    promoted = true;
                    promoted1To2++;
                    reason = "U promovua në Vitin 2 (ka marrë " + creditsYear1 + " kredite nga Viti 1, kërkohen ≥ 30).";
                    assignNextClass(student, 2);
                } else {
                    newYear = 1;
                    newStatus = "REPEATING";
                    promoted = false;
                    repeating1++;
                    reason = "Mbeti përsëritës në Vitin 1 (ka marrë " + creditsYear1 + " kredite nga Viti 1, kërkohen ≥ 30).";
                }
            } else if (oldYear == 2) {

                int credits1And2 = creditsYear1 + creditsYear2;
                if (credits1And2 >= 80) {
                    newYear = 3;
                    newStatus = "ACTIVE";
                    promoted = true;
                    promoted2To3++;
                    reason = "U promovua në Vitin 3 (ka marrë " + credits1And2 + " kredite nga Viti 1+2, kërkohen ≥ 80).";
                    assignNextClass(student, 3);
                } else {
                    newYear = 2;
                    newStatus = "REPEATING";
                    promoted = false;
                    repeating2++;
                    reason = "Mbeti përsëritës në Vitin 2 (ka marrë " + credits1And2 + " kredite nga Viti 1+2, kërkohen ≥ 80).";
                }
            } else {

                if (totalCredits >= 180) {
                    newYear = 3;
                    newStatus = "GRADUATED";
                    promoted = true;
                    graduated++;
                    reason = "U diplomua me sukses (ka marrë " + totalCredits + " kredite gjithsej, kërkohen ≥ 180).";
                } else {
                    newYear = 3;
                    newStatus = "REPEATING";
                    promoted = false;
                    repeating3++;
                    reason = "Mbeti në Vitin 3 për shlyerje lëndësh (ka marrë " + totalCredits + " nga 180 kredite).";
                }
            }

            student.setVitStudimit(newYear);
            student.setStatus(newStatus);
            studentRepository.save(student);

            String emri = (student.getUser() != null && student.getUser().getEmri() != null) ? student.getUser().getEmri() : "";
            String mbiemri = (student.getUser() != null && student.getUser().getMbiemri() != null) ? student.getUser().getMbiemri() : "";

            details.add(StudentPromotionDetailDto.builder()
                    .studentId(student.getStudentId())
                    .emri(emri)
                    .mbiemri(mbiemri)
                    .nrMatrikulimit(student.getNrMatrikulimit())
                    .oldYear(oldYear)
                    .newYear(newYear)
                    .creditsYear1(creditsYear1)
                    .creditsYear2(creditsYear2)
                    .totalCredits(totalCredits)
                    .previousStatus(oldStatus)
                    .newStatus(newStatus)
                    .promoted(promoted)
                    .reason(reason)
                    .build());
        }

        String nextAcademicYear = academicYearService.advanceAcademicYear();

        String summaryMessage = String.format(
                "Përmbyllja e vitit akademik përfundoi me sukses! Viti i ri akademik: %s. U përpunuan %d studentë: %d kaluan në Vitin 2, %d kaluan në Vitin 3, %d u diplomuan, %d mbetën përsëritës.",
                nextAcademicYear, totalProcessed, promoted1To2, promoted2To3, graduated, (repeating1 + repeating2 + repeating3)
        );

        log.info(summaryMessage);

        return PromotionResultDto.builder()
                .totalProcessed(totalProcessed)
                .promotedYear1To2(promoted1To2)
                .promotedYear2To3(promoted2To3)
                .graduated(graduated)
                .repeatingYear1(repeating1)
                .repeatingYear2(repeating2)
                .repeatingYear3(repeating3)
                .message(summaryMessage)
                .newAcademicYear(nextAcademicYear)
                .details(details)
                .build();
    }

    private void assignNextClass(Student student, int targetYear) {
        if (student.getProgram() == null) {
            return;
        }

        List<Classes> availableClasses = classesRepository.findByProgram_ProgramIdAndVitStudimit(
                student.getProgram().getProgramId(),
                targetYear
        );

        if (availableClasses.isEmpty()) {
            return;
        }

        Classes currentClass = student.getClasses();
        if (currentClass != null && currentClass.getEmriClass() != null) {
            String currName = currentClass.getEmriClass().trim();
            for (Classes cl : availableClasses) {
                if (cl.getEmriClass() != null && cl.getEmriClass().trim().equalsIgnoreCase(currName)) {
                    student.setClasses(cl);
                    return;
                }
            }
        }

        student.setClasses(availableClasses.get(0));
    }
}
