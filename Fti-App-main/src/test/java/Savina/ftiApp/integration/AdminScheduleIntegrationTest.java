package Savina.ftiApp.integration;

import Savina.ftiApp.dto.requestDTO.ScheduleRequest;
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
public class AdminScheduleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ClassesRepository classesRepository;

    @Autowired
    private CourseScheduleRepository courseScheduleRepository;

    @Test
    @DisplayName("Integration: Schedules Endpoints - getCourses me weekly hours, check-room, dhe save/delete schedule")
    void testScheduleEndpointsFullFlow() throws Exception {
        Program prog = programRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Nuk ka program ne DB."));

        Professor prof = professorRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Nuk ka pedagog ne DB."));

        Room room = roomRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Nuk ka salle ne DB."));

        Classes cls = classesRepository.save(Classes.builder()
                .emriClass("Grupi A-Sched")
                .program(prog)
                .vitStudimit(1)
                .build());

        Course course = courseRepository.save(Course.builder()
                .emriCourse("Test Orari Lende " + (System.currentTimeMillis() % 100000))
                .kredite(6)
                .studyYear(1)
                .semester("1")
                .program(prog)
                .status("AKTIV")
                .build());

        // 1. Test getCourses endpoint to verify weekly hours fields
        MvcResult coursesRes = mockMvc.perform(get("/api/admin/schedules/courses")
                        .param("programId", String.valueOf(prog.getProgramId()))
                        .param("studyYear", "1")
                        .param("semester", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode coursesArr = objectMapper.readTree(coursesRes.getResponse().getContentAsString());
        assertThat(coursesArr.isArray()).isTrue();
        boolean courseFound = false;
        for (JsonNode cNode : coursesArr) {
            if (cNode.get("courseId").asInt() == course.getCourseId()) {
                courseFound = true;
                assertThat(cNode.has("weeklyHoursLeksion")).isTrue();
                assertThat(cNode.has("weeklyHoursSeminar")).isTrue();
                assertThat(cNode.get("weeklyHoursLeksion").asDouble()).isGreaterThan(0.0);
                break;
            }
        }
        assertThat(courseFound).isTrue();

        // 2. Test check-room availability
        mockMvc.perform(get("/api/admin/schedules/check-room")
                        .param("roomId", String.valueOf(room.getRoomId()))
                        .param("professorId", String.valueOf(prof.getProfessorId()))
                        .param("dayOfWeek", "E Premte")
                        .param("startTime", "12:00")
                        .param("endTime", "14:00")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));

        // 3. Save Schedule via POST
        ScheduleRequest req = ScheduleRequest.builder()
                .courseId(course.getCourseId())
                .professorId(prof.getProfessorId())
                .roomId(room.getRoomId())
                .classId(cls.getClassId())
                .roleType("LEKSION")
                .dayOfWeek("E Premte")
                .startTime("12:00")
                .endTime("14:00")
                .semester("1")
                .academicYear("2025-2026")
                .build();

        MvcResult saveRes = mockMvc.perform(post("/api/admin/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scheduleId").isNumber())
                .andExpect(jsonPath("$.courseName").value(course.getEmriCourse()))
                .andReturn();

        int scheduleId = objectMapper.readTree(saveRes.getResponse().getContentAsString()).get("scheduleId").asInt();

        // 4. Test conflict detection on the same room & time
        mockMvc.perform(post("/api/admin/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        // 5. Delete schedule
        mockMvc.perform(delete("/api/admin/schedules/" + scheduleId))
                .andExpect(status().isOk());
    }
}
