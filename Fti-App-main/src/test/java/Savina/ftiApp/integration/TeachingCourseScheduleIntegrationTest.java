package Savina.ftiApp.integration;

import Savina.ftiApp.dto.requestDTO.ScheduleRequest;
import Savina.ftiApp.dto.requestDTO.TeachingAllocationRequest;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TeachingCourseScheduleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ClassesRepository classesRepository;

    @Test
    @DisplayName("Rrjedha e Alokimit dhe Orarit: Alokimi -> Ruajtja e Orarit -> Zbulimi i Konfliktit -> Fshirja")
    void testTeachingCourseAndScheduleFlow() throws Exception {
        Classes clazz = classesRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Nuk u gjet asnje klase ne DB per testim."));

        Course course = courseRepository.save(Course.builder()
                .emriCourse("Lende Test " + (System.currentTimeMillis() % 100000))
                .kredite(6)
                .studyYear(1)
                .status("AKTIV")
                .program(clazz.getProgram())
                .build());

        Professor professor = professorRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Nuk u gjet asnje pedagog ne DB per testim."));

        Room room = roomRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Nuk u gjet asnje salle ne DB per testim."));

        // =========================================================================
        // HAPI 1: Admini kryen alokimin e plote te lendes (Leksion, Seminar, Lab, Oret)
        // =========================================================================
        TeachingAllocationRequest.SeminarAssignmentReq seminarReq = TeachingAllocationRequest.SeminarAssignmentReq.builder()
                .professorId(professor.getProfessorId())
                .classGroup("Grupi A")
                .classIds(List.of(clazz.getClassId()))
                .build();

        TeachingAllocationRequest allocReq = TeachingAllocationRequest.builder()
                .courseId(course.getCourseId())
                .semester("1")
                .durationWeeks(15)
                .totalHours(60)
                .lectureHours(30)
                .seminarHours(15)
                .labHours(15)
                .lectureProfessorId(professor.getProfessorId())
                .lectureClassIds(List.of(clazz.getClassId()))
                .hasLab(true)
                .seminars(List.of(seminarReq))
                .labs(List.of())
                .build();

        mockMvc.perform(post("/api/admin/teaching-courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(allocReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(course.getCourseId()))
                .andExpect(jsonPath("$.lectureHours").value(30));

        // =========================================================================
        // HAPI 2: Leximi i alokimit nga DB me GET
        // =========================================================================
        mockMvc.perform(get("/api/admin/teaching-courses/" + course.getCourseId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(course.getCourseId()))
                .andExpect(jsonPath("$.lectureHours").value(30))
                .andExpect(jsonPath("$.seminarHours").value(15));

        // =========================================================================
        // HAPI 3: Verifikimi i disponueshmerise se salles perpara caktimit te orarit
        // =========================================================================
        mockMvc.perform(get("/api/admin/schedules/check-room")
                        .param("roomId", String.valueOf(room.getRoomId()))
                        .param("dayOfWeek", "E Hene")
                        .param("startTime", "08:00")
                        .param("endTime", "10:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));

        // =========================================================================
        // HAPI 4: Krijimi i ores mesimore ne orar
        // =========================================================================
        ScheduleRequest schedReq = ScheduleRequest.builder()
                .courseId(course.getCourseId())
                .professorId(professor.getProfessorId())
                .roomId(room.getRoomId())
                .classId(clazz.getClassId())
                .programId(course.getProgram() != null ? course.getProgram().getProgramId() : null)
                .studyYear(course.getStudyYear())
                .roleType("LEKSION")
                .dayOfWeek("E Hene")
                .startTime("08:00")
                .endTime("10:00")
                .semester("1")
                .build();

        MvcResult schedResult = mockMvc.perform(post("/api/admin/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(schedReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scheduleId").isNumber())
                .andExpect(jsonPath("$.dayOfWeek").value("E Hene"))
                .andReturn();

        JsonNode root = objectMapper.readTree(schedResult.getResponse().getContentAsString());
        int scheduleId = root.get("scheduleId").asInt();

        // =========================================================================
        // HAPI 5: Testimi i zbulimit te konfliktit (E njejta salle & orar -> 400 Bad Request)
        // =========================================================================
        mockMvc.perform(post("/api/admin/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(schedReq)))
                .andExpect(status().isBadRequest());

        // =========================================================================
        // HAPI 6: Fshirja e ores nga orari dhe fshirja e alokimit
        // =========================================================================
        mockMvc.perform(delete("/api/admin/schedules/" + scheduleId))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/admin/teaching-courses/" + course.getCourseId()))
                .andExpect(status().isOk());
    }
}
