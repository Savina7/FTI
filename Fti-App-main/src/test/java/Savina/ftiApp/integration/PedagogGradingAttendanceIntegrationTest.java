package Savina.ftiApp.integration;

import Savina.ftiApp.dto.requestDTO.SaveAttendanceRequest;
import Savina.ftiApp.dto.requestDTO.SaveGradesRequest;
import Savina.ftiApp.dto.responseDTO.TopicDto;
import Savina.ftiApp.entity.*;
import Savina.ftiApp.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.security.test.context.support.WithMockUser;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "pedagog.test@fti.edu.al", roles = {"PROFESSOR", "PEDAGOG"})
public class PedagogGradingAttendanceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TeachingCourseRepository teachingCourseRepository;

    @Autowired
    private ClassesRepository classesRepository;

    @Autowired
    private ProgramRepository programRepository;

    @Test
    @DisplayName("Rrjedha e Notave dhe Mungesave: Pedagogu hedh noten & mungesat -> Studenti i sheh ne llogarine e tij")
    void testPedagogGradingAndAttendanceFlow() throws Exception {
        Program program = programRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Nuk u gjet asnje program ne DB per testim."));

        Course course = courseRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Nuk u gjet asnje lende ne DB per testim."));

        Professor professor = professorRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Nuk u gjet asnje pedagog ne DB per testim."));

        Classes clazz = classesRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Nuk u gjet asnje klase ne DB per testim."));

        long suffix = System.currentTimeMillis() % 1000000;
        String studentEmail = "student.grade" + suffix + "@fti.edu.al";

        // Krijojme nje student te lidhur me kete klase dhe lende
        User user = userRepository.save(User.builder()
                .emri("Gentian")
                .mbiemri("Kola")
                .email(studentEmail)
                .password("hash123")
                .status("ACTIVE")
                .verified("Y")
                .createdAt(LocalDate.now())
                .build());

        Student student = studentRepository.save(Student.builder()
                .user(user)
                .program(program)
                .classes(clazz)
                .nrMatrikulimit(String.format("GRD%09d", suffix))
                .vitStudimit(1)
                .totalKredite(0)
                .status("ACTIVE")
                .build());

        TeachingCourse tc = teachingCourseRepository.findByCourseCourseId(course.getCourseId()).stream().findFirst()
                .orElseGet(() -> teachingCourseRepository.save(TeachingCourse.builder()
                        .course(course)
                        .professor(professor)
                        .classes(Set.of(clazz))
                        .roleType("LEKSION")
                        .build()));

        // =========================================================================
        // HAPI 1: Pedagogu regjistron temen mesimore per Javen 1
        // =========================================================================
        TopicDto topicDto = TopicDto.builder()
                .courseId(course.getCourseId())
                .teachingCourseId(tc.getTeachingCourseId())
                .weekNumber(1)
                .topicDate(LocalDate.now())
                .title("Java 1: Hyrje ne Lende")
                .description("Prezantimi i silabusit dhe rregullave")
                .build();

        mockMvc.perform(post("/api/topics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(topicDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Java 1: Hyrje ne Lende"))
                .andExpect(jsonPath("$.weekNumber").value(1));

        // =========================================================================
        // HAPI 2: Pedagogu hap regjistrin me GET dhe verifikon qe u shfaq kolona e mungeses se Javes 1
        // =========================================================================
        mockMvc.perform(get("/api/pedagog/register")
                        .param("courseId", String.valueOf(course.getCourseId()))
                        .param("classId", String.valueOf(clazz.getClassId()))
                        .param("roleType", "LEKSION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attendanceColumns").isArray())
                .andExpect(jsonPath("$.attendanceColumns[0].title").value(containsString("Jave 1")));

        // =========================================================================
        // HAPI 3: Pedagogu vendos noten per studentin (Nota 9.5 - KALUAR)
        // =========================================================================
        SaveGradesRequest.StudentGradeEntry gradeEntry = SaveGradesRequest.StudentGradeEntry.builder()
                .studentId(student.getStudentId())
                .grade(new BigDecimal("9.5"))
                .status("KALUAR")
                .build();

        SaveGradesRequest gradesReq = SaveGradesRequest.builder()
                .courseId(course.getCourseId())
                .classId(clazz.getClassId())
                .grades(List.of(gradeEntry))
                .build();

        mockMvc.perform(post("/api/pedagog/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gradesReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Notat u ruajten me sukses."));

        // =========================================================================
        // HAPI 3: Pedagogu vendos mungesat per studentin
        // =========================================================================
        SaveAttendanceRequest.StudentAttendanceEntry attendanceEntry = SaveAttendanceRequest.StudentAttendanceEntry.builder()
                .studentId(student.getStudentId())
                .attendance(Map.of("J1_L", true, "J2_L", false))
                .build();

        SaveAttendanceRequest attReq = SaveAttendanceRequest.builder()
                .courseId(course.getCourseId())
                .classId(clazz.getClassId())
                .attendances(List.of(attendanceEntry))
                .build();

        mockMvc.perform(post("/api/pedagog/attendance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(attReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Mungesat u ruajten me sukses."));

        // =========================================================================
        // HAPI 4: Studenti sheh noten e re ne profilin e tij
        // =========================================================================
        mockMvc.perform(get("/api/student/grades")
                        .param("email", studentEmail))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // =========================================================================
        // HAPI 5: Studenti sheh mungesat e tij
        // =========================================================================
        mockMvc.perform(get("/api/student/mungesat")
                        .param("email", studentEmail))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
