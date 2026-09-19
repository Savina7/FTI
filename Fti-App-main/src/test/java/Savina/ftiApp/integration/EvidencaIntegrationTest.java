package Savina.ftiApp.integration;

import Savina.ftiApp.entity.*;
import Savina.ftiApp.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
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
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class EvidencaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    @DisplayName("Integration: Evidenca API kthen te dhenat e plota per pedagogun, oret dhe grupet")
    void testEvidencaApiFlow() throws Exception {
        Professor prof = professorRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Nuk u gjet asnje pedagog per testim."));

        Program prog = programRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Nuk u gjet asnje program per testim."));

        Classes cls = classesRepository.save(Classes.builder()
                .emriClass("Grupi A-Evidenca")
                .program(prog)
                .vitStudimit(1)
                .build());

        Course course = courseRepository.save(Course.builder()
                .emriCourse("Lende Test Evidenca " + (System.currentTimeMillis() % 100000))
                .kredite(6)
                .krediteLeksion(BigDecimal.valueOf(3.0))
                .krediteSeminar(BigDecimal.valueOf(1.5))
                .krediteLaborator(BigDecimal.valueOf(1.0))
                .semester("1")
                .studyYear(1)
                .program(prog)
                .status("AKTIV")
                .build());

        TeachingCourse tc = teachingCourseRepository.save(TeachingCourse.builder()
                .course(course)
                .professor(prof)
                .roleType("LEKSION")
                .academicYear("2025-2026")
                .classes(Set.of(cls))
                .build());

        // Call Evidenca endpoint
        MvcResult result = mockMvc.perform(get("/api/admin/teaching-courses/evidenca")
                        .param("professorId", String.valueOf(prof.getProfessorId()))
                        .param("semester", "1")
                        .param("academicYear", "2025-2026")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode array = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(array.isArray()).isTrue();
        assertThat(array.size()).isGreaterThanOrEqualTo(1);

        // Find created course in the returned JSON
        boolean found = false;
        for (JsonNode item : array) {
            if (item.has("courseEmri") && item.get("courseEmri").asText().equals(course.getEmriCourse())) {
                found = true;
                assertThat(item.get("oreLeksion").asDouble()).isGreaterThan(0.0);
                assertThat(item.get("semester").asText()).isEqualTo("1");
                break;
            }
        }
        assertThat(found).isTrue();
    }

    @Test
    @DisplayName("Integration: Evidenca API kthen liste boshe ose te filtruar nese semestri nuk perputhet")
    void testEvidencaApiSemesterFilter() throws Exception {
        Professor prof = professorRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Nuk u gjet asnje pedagog per testim."));

        mockMvc.perform(get("/api/admin/teaching-courses/evidenca")
                        .param("professorId", String.valueOf(prof.getProfessorId()))
                        .param("semester", "99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
