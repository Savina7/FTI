package Savina.ftiApp.integration;

import Savina.ftiApp.dto.responseDTO.PromotionResultDto;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AcademicPromotionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private TeachingCourseRepository teachingCourseRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Test
    @DisplayName("Rrjedha e Promovimit Akademik Vjetor: Admini mbyll vitin -> Studentet promovohen ose perserisin vitin")
    void testAcademicPromotionFlow() throws Exception {
        Program program = programRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Nuk u gjet asnje program ne DB per testim."));

        Professor defaultProf = professorRepository.findAll().stream().findFirst().orElse(null);

        long suffix = System.currentTimeMillis() % 1000000;

        User userPass = userRepository.save(User.builder()
                .emri("Student")
                .mbiemri("Kalues")
                .email("kalues" + suffix + "@fti.edu.al")
                .password("hash123")
                .status("ACTIVE")
                .verified("Y")
                .createdAt(LocalDate.now())
                .build());

        Student studentPass = studentRepository.save(Student.builder()
                .user(userPass)
                .program(program)
                .nrMatrikulimit(String.format("PASS%08d", suffix))
                .vitStudimit(1)
                .totalKredite(0)
                .status("ACTIVE")
                .build());

        List<Savina.ftiApp.entity.Course> y1Courses = courseRepository.findByProgramProgramIdAndStudyYear(program.getProgramId(), 1);
        int passCredits = 0;
        for (Savina.ftiApp.entity.Course c : y1Courses) {
            if (passCredits >= 30) break;
            Savina.ftiApp.entity.TeachingCourse tc = teachingCourseRepository.findByCourseCourseId(c.getCourseId()).stream().findFirst()
                    .orElseGet(() -> teachingCourseRepository.save(Savina.ftiApp.entity.TeachingCourse.builder()
                            .course(c)
                            .professor(defaultProf)
                            .roleType("LEKSION")
                            .build()));
            gradeRepository.save(Savina.ftiApp.entity.Grade.builder()
                    .student(studentPass)
                    .teachingCourse(tc)
                    .grade(new java.math.BigDecimal("9.0"))
                    .status("PASSED")
                    .dateGiven(LocalDate.now())
                    .build());
            passCredits += (c.getKredite() != null ? c.getKredite() : 6);
        }

        User userRepeat = userRepository.save(User.builder()
                .emri("Student")
                .mbiemri("Perserites")
                .email("perserites" + suffix + "@fti.edu.al")
                .password("hash123")
                .status("ACTIVE")
                .verified("Y")
                .createdAt(LocalDate.now())
                .build());

        Student studentRepeat = studentRepository.save(Student.builder()
                .user(userRepeat)
                .program(program)
                .nrMatrikulimit(String.format("REP%09d", suffix))
                .vitStudimit(1)
                .totalKredite(0)
                .status("ACTIVE")
                .build());

        if (!y1Courses.isEmpty()) {
            Savina.ftiApp.entity.Course c = y1Courses.get(0);
            Savina.ftiApp.entity.TeachingCourse tc = teachingCourseRepository.findByCourseCourseId(c.getCourseId()).stream().findFirst()
                    .orElseGet(() -> teachingCourseRepository.save(Savina.ftiApp.entity.TeachingCourse.builder().course(c).build()));
            gradeRepository.save(Savina.ftiApp.entity.Grade.builder()
                    .student(studentRepeat)
                    .teachingCourse(tc)
                    .grade(new java.math.BigDecimal("8.0"))
                    .status("PASSED")
                    .dateGiven(LocalDate.now())
                    .build());
        }

        MvcResult result = mockMvc.perform(post("/api/admin/students/promote-academic-year")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProcessed").isNumber())
                .andReturn();

        PromotionResultDto resDto = objectMapper.readValue(result.getResponse().getContentAsString(), PromotionResultDto.class);
        assertThat(resDto.getTotalProcessed()).isGreaterThan(0);

        Student updatedPass = studentRepository.findById(studentPass.getStudentId()).orElseThrow();
        assertThat(updatedPass.getVitStudimit()).isEqualTo(2);

        Student updatedRepeat = studentRepository.findById(studentRepeat.getStudentId()).orElseThrow();
        assertThat(updatedRepeat.getVitStudimit()).isEqualTo(1);
    }
}
