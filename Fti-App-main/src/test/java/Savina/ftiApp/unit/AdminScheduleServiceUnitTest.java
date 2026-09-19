package Savina.ftiApp.unit;

import Savina.ftiApp.dto.requestDTO.ScheduleRequest;
import Savina.ftiApp.dto.responseDTO.RoomAvailabilityDto;
import Savina.ftiApp.dto.responseDTO.ScheduleResponseDto;
import Savina.ftiApp.entity.*;
import Savina.ftiApp.repository.*;
import Savina.ftiApp.service.AdminScheduleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminScheduleServiceUnitTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private CourseScheduleRepository courseScheduleRepository;
    @Mock
    private TeachingCourseRepository teachingCourseRepository;
    @Mock
    private ProfessorRepository professorRepository;
    @Mock
    private ClassesRepository classesRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private ProgramRepository programRepository;
    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private AdminScheduleService scheduleService;

    @Test
    @DisplayName("Unit: getCourses llogarit sakte oret javore (weeklyHoursLeksion, weeklyHoursSeminar)")
    void testGetCourses_WeeklyHoursCalculation() {
        Course c1 = Course.builder()
                .courseId(1)
                .emriCourse("Struktura te Dhenash")
                .kredite(6)
                .krediteLeksion(BigDecimal.valueOf(3.0))
                .krediteSeminar(BigDecimal.valueOf(1.5))
                .krediteLaborator(BigDecimal.valueOf(1.0))
                .studyYear(2)
                .semester("1")
                .build();

        Course c2 = Course.builder()
                .courseId(2)
                .emriCourse("Programim Web")
                .kredite(4)
                .studyYear(2)
                .semester("2")
                .build();

        when(courseRepository.findByProgramProgramIdAndStudyYear(1, 2)).thenReturn(List.of(c1, c2));

        List<Map<String, Object>> result = scheduleService.getCourses(1, 2, null);

        assertThat(result).hasSize(2);

        Map<String, Object> map1 = result.get(0);
        assertThat(map1.get("courseId")).isEqualTo(1);
        assertThat(map1.get("weeklyHoursLeksion")).isEqualTo(3.0);
        assertThat(map1.get("weeklyHoursSeminar")).isEqualTo(1.5);
        assertThat(map1.get("weeklyHoursLaborator")).isEqualTo(1.0);

        Map<String, Object> map2 = result.get(1);
        assertThat(map2.get("courseId")).isEqualTo(2);
        assertThat(map2.get("weeklyHoursLeksion")).isEqualTo(2.0); // fallback for credits < 6
        assertThat(map2.get("weeklyHoursSeminar")).isEqualTo(1.5); // default
        assertThat(map2.get("weeklyHoursLaborator")).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Unit: determineCourseSemester kthen semestrin e sakte nga formati numerik ose tekst")
    void testDetermineCourseSemester() {
        Course c1 = Course.builder().semester("1").build();
        Course c2 = Course.builder().semester("Semestri II").build();
        Course c3 = Course.builder().semester("").build();

        assertThat(scheduleService.determineCourseSemester(c1)).isEqualTo("1");
        assertThat(scheduleService.determineCourseSemester(c2)).isEqualTo("Semestri II");
        assertThat(scheduleService.determineCourseSemester(c3)).isEqualTo("1");
        assertThat(scheduleService.determineCourseSemester(null)).isEqualTo("1");
    }

    @Test
    @DisplayName("Unit: checkRoomAvailability tregon te disponueshme kur nuk ka perplasje orari")
    void testCheckRoomAvailability_Available() {
        Room r = Room.builder().roomId(1).roomName("Salla 101").build();
        Professor p = Professor.builder().professorId(2).build();

        when(roomRepository.findById(1)).thenReturn(Optional.of(r));
        when(professorRepository.findById(2)).thenReturn(Optional.of(p));
        when(courseScheduleRepository.findOverlappingRoomSchedules(eq(1), anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(Collections.emptyList());
        when(courseScheduleRepository.findOverlappingProfessorSchedules(eq(2), anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(Collections.emptyList());

        RoomAvailabilityDto result = scheduleService.checkRoomAvailability(1, 2, "E Hene", "08:00", "10:00", null);

        assertThat(result).isNotNull();
        assertThat(result.isAvailable()).isTrue();
        assertThat(result.isRoomAvailable()).isTrue();
        assertThat(result.isProfessorAvailable()).isTrue();
    }

    @Test
    @DisplayName("Unit: checkRoomAvailability tregon konflikt ne rast se salla eshte e zene")
    void testCheckRoomAvailability_RoomConflict() {
        Room r = Room.builder().roomId(1).roomName("Salla 101").build();
        CourseSchedule conflict = CourseSchedule.builder()
                .scheduleId(99)
                .room(r)
                .dayOfWeek("E Hene")
                .startTime("08:00")
                .endTime("10:00")
                .build();

        when(roomRepository.findById(1)).thenReturn(Optional.of(r));
        when(courseScheduleRepository.findOverlappingRoomSchedules(eq(1), anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(List.of(conflict));

        RoomAvailabilityDto result = scheduleService.checkRoomAvailability(1, null, "E Hene", "08:00", "10:00", null);

        assertThat(result.isAvailable()).isFalse();
        assertThat(result.isRoomAvailable()).isFalse();
        assertThat(result.getRoomConflictMessage()).contains("eshte e zene");
    }

    @Test
    @DisplayName("Unit: saveSchedule ruan me sukses orarin e ri")
    void testSaveSchedule_Success() {
        Program prog = Program.builder().programId(1).specializimi("Inxhinieri Informatike").build();
        Course course = Course.builder().courseId(10).emriCourse("Baza te Dhenash").program(prog).studyYear(2).build();
        Professor prof = Professor.builder().professorId(5).user(User.builder().emri("Agim").mbiemri("Hoxha").build()).build();
        Room room = Room.builder().roomId(3).roomName("Lab 1").build();
        Classes cls = Classes.builder().classId(20).emriClass("Grupi A").program(prog).vitStudimit(2).build();

        TeachingCourse tc = TeachingCourse.builder()
                .teachingCourseId(100)
                .course(course)
                .professor(prof)
                .roleType("LEKSION")
                .build();

        when(courseRepository.findById(10)).thenReturn(Optional.of(course));
        when(professorRepository.findById(5)).thenReturn(Optional.of(prof));
        when(roomRepository.findById(3)).thenReturn(Optional.of(room));
        when(classesRepository.findById(20)).thenReturn(Optional.of(cls));
        when(teachingCourseRepository.findByCourseCourseId(10)).thenReturn(List.of(tc));

        when(courseScheduleRepository.findOverlappingRoomSchedules(anyInt(), anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(Collections.emptyList());
        when(courseScheduleRepository.findOverlappingProfessorSchedules(anyInt(), anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(Collections.emptyList());

        CourseSchedule savedEntity = CourseSchedule.builder()
                .scheduleId(50)
                .teachingCourse(tc)
                .classes(cls)
                .room(room)
                .dayOfWeek("E Hene")
                .startTime("08:00")
                .endTime("10:00")
                .build();

        when(courseScheduleRepository.save(any(CourseSchedule.class))).thenReturn(savedEntity);

        ScheduleRequest req = ScheduleRequest.builder()
                .courseId(10)
                .professorId(5)
                .roomId(3)
                .classId(20)
                .roleType("LEKSION")
                .dayOfWeek("E Hene")
                .startTime("08:00")
                .endTime("10:00")
                .academicYear("2025-2026")
                .build();

        ScheduleResponseDto result = scheduleService.saveSchedule(req);

        assertThat(result).isNotNull();
        assertThat(result.getScheduleId()).isEqualTo(50);
        assertThat(result.getCourseName()).isEqualTo("Baza te Dhenash");
        assertThat(result.getProfessorName()).isEqualTo("Agim Hoxha");
        assertThat(result.getRoleType()).isEqualTo("LEKSION");
        verify(courseScheduleRepository, times(1)).save(any(CourseSchedule.class));
    }

    @Test
    @DisplayName("Unit: saveSchedule hedh gabim nese ora e mbarimit eshte me e vogel ose e barabarte me oren e fillimit")
    void testSaveSchedule_InvalidTime() {
        ScheduleRequest req = ScheduleRequest.builder()
                .courseId(10)
                .roomId(3)
                .dayOfWeek("E Hene")
                .startTime("10:00")
                .endTime("08:00")
                .build();

        assertThatThrownBy(() -> scheduleService.saveSchedule(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ora e mbarimit duhet te jete me e madhe se ora e fillimit.");
    }

    @Test
    @DisplayName("Unit: deleteSchedule fshin orarin me sukses")
    void testDeleteSchedule() {
        when(courseScheduleRepository.existsById(50)).thenReturn(true);
        doNothing().when(courseScheduleRepository).deleteById(50);

        scheduleService.deleteSchedule(50);

        verify(courseScheduleRepository, times(1)).deleteById(50);
    }
}
