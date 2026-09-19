package Savina.ftiApp.unit;

import Savina.ftiApp.dto.requestDTO.ExamScheduleRequestDto;
import Savina.ftiApp.dto.responseDTO.ExamScheduleDto;
import Savina.ftiApp.entity.*;
import Savina.ftiApp.repository.*;
import Savina.ftiApp.service.AdminExamScheduleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminExamScheduleServiceUnitTest {

    @Mock
    private ExamScheduleRepository examScheduleRepo;
    @Mock
    private CourseRepository courseRepo;
    @Mock
    private ProgramRepository programRepo;
    @Mock
    private RoomRepository roomRepo;
    @Mock
    private TeachingCourseRepository teachingCourseRepo;
    @Mock
    private ExamSeasonRepository examSeasonRepo;

    @InjectMocks
    private AdminExamScheduleService examScheduleService;

    @Test
    @DisplayName("Unit: getCoursesForProgramAndSemester rendit lendet sipas vitit dhe emrit")
    void testGetCoursesForProgramAndSemester() {
        Course c1 = Course.builder().courseId(1).emriCourse("Zgjidhje Numerike").studyYear(2).build();
        Course c2 = Course.builder().courseId(2).emriCourse("Algjeber").studyYear(1).build();

        when(courseRepo.findByProgramProgramId(1)).thenReturn(List.of(c1, c2));

        List<Map<String, Object>> list = examScheduleService.getCoursesForProgramAndSemester(1, "1");

        assertThat(list).hasSize(2);
        assertThat(list.get(0).get("emriCourse")).isEqualTo("Algjeber"); // Year 1 first
        assertThat(list.get(1).get("emriCourse")).isEqualTo("Zgjidhje Numerike"); // Year 2 second
    }

    @Test
    @DisplayName("Unit: saveExam ruan provimin me sukses kur nuk ka konflikt")
    void testSaveExamSchedule_Success() {
        Course course = Course.builder().courseId(5).emriCourse("Fizike").studyYear(1).semester("1").build();
        Room room = Room.builder().roomId(10).roomName("Salla 102").build();
        Program prog = Program.builder().programId(1).specializimi("Inxhinieri Elektronike").build();

        when(courseRepo.findById(5)).thenReturn(Optional.of(course));
        when(programRepo.findById(1)).thenReturn(Optional.of(prog));
        when(roomRepo.findAllById(List.of(10))).thenReturn(List.of(room));
        when(examScheduleRepo.findByExamDateBetween(any(), any())).thenReturn(Collections.emptyList());

        ExamSchedule saved = ExamSchedule.builder()
                .examId(100)
                .course(course)
                .rooms(Set.of(room))
                .examDate(LocalDateTime.of(2026, 6, 15, 9, 0))
                .endTime("11:00")
                .type("VERE")
                .build();

        when(examScheduleRepo.save(any(ExamSchedule.class))).thenReturn(saved);

        ExamScheduleRequestDto req = ExamScheduleRequestDto.builder()
                .courseId(5)
                .roomIds(List.of(10))
                .programId(1)
                .examDate("2026-06-15")
                .startTime("09:00")
                .endTime("11:00")
                .season("VERE")
                .build();

        ExamScheduleDto dto = examScheduleService.saveExam(req);

        assertThat(dto).isNotNull();
        assertThat(dto.getExamId()).isEqualTo(100);
        assertThat(dto.getCourseName()).isEqualTo("Fizike");
        verify(examScheduleRepo, times(1)).save(any(ExamSchedule.class));
    }

    @Test
    @DisplayName("Unit: saveExam hedh gabim nese salla ka konflikt orari per provim")
    void testSaveExamSchedule_RoomConflict() {
        Course course = Course.builder().courseId(5).emriCourse("Fizike").studyYear(1).build();
        Room room = Room.builder().roomId(10).roomName("Salla 102").build();
        Program prog = Program.builder().programId(1).build();

        when(courseRepo.findById(5)).thenReturn(Optional.of(course));
        when(programRepo.findById(1)).thenReturn(Optional.of(prog));

        ExamSchedule conflict = ExamSchedule.builder()
                .examId(99)
                .rooms(Set.of(room))
                .examDate(LocalDateTime.of(2026, 6, 15, 9, 0))
                .endTime("11:00")
                .build();

        when(examScheduleRepo.findByExamDateBetween(any(), any())).thenReturn(List.of(conflict));

        ExamScheduleRequestDto req = ExamScheduleRequestDto.builder()
                .courseId(5)
                .roomIds(List.of(10))
                .programId(1)
                .examDate("2026-06-15")
                .startTime("09:00")
                .endTime("11:00")
                .build();

        assertThatThrownBy(() -> examScheduleService.saveExam(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Salla 'Salla 102' është e zënë!");
    }

    @Test
    @DisplayName("Unit: deleteExam fshin provimin me sukses")
    void testDeleteExamSchedule() {
        doNothing().when(examScheduleRepo).deleteById(100);

        examScheduleService.deleteExam(100);

        verify(examScheduleRepo, times(1)).deleteById(100);
    }
}
