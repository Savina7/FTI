package Savina.ftiApp.unit;

import Savina.ftiApp.entity.*;
import Savina.ftiApp.repository.*;
import Savina.ftiApp.service.AcademicYearService;
import Savina.ftiApp.service.AdminTeachingCourseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvidencaServiceUnitTest {

    @Mock
    private TeachingCourseRepository teachingCourseRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private ProfessorRepository professorRepository;
    @Mock
    private ClassesRepository classesRepository;
    @Mock
    private ProgramRepository programRepository;
    @Mock
    private AcademicYearService academicYearService;

    @InjectMocks
    private AdminTeachingCourseService adminTeachingCourseService;

    @Test
    @DisplayName("Unit: getEvidenca llogarit oret e leksionit, seminarit dhe laboratorit per program Bachelor")
    void testGetEvidenca_BachelorCalculations() {
        Program prog = Program.builder()
                .programId(1)
                .specializimi("Inxhinieri Informatike")
                .nivel("Bachelor")
                .build();

        Course course = Course.builder()
                .courseId(10)
                .emriCourse("Programim i Avancuar")
                .program(prog)
                .kredite(6)
                .krediteLeksion(BigDecimal.valueOf(3.0))
                .krediteSeminar(BigDecimal.valueOf(1.5))
                .krediteLaborator(BigDecimal.valueOf(1.0))
                .semester("1")
                .build();

        Professor prof = Professor.builder().professorId(1).build();

        Classes cl1 = Classes.builder().classId(1).emriClass("Grupi A").build();
        Classes cl2 = Classes.builder().classId(2).emriClass("Grupi B").build();

        TeachingCourse tcLek = TeachingCourse.builder()
                .teachingCourseId(101)
                .course(course)
                .professor(prof)
                .roleType("LEKSION")
                .academicYear("2025-2026")
                .classes(Set.of(cl1))
                .build();

        TeachingCourse tcSem = TeachingCourse.builder()
                .teachingCourseId(102)
                .course(course)
                .professor(prof)
                .roleType("SEMINAR")
                .academicYear("2025-2026")
                .classes(Set.of(cl1, cl2))
                .build();

        when(teachingCourseRepository.findByProfessorIdWithDetails(1)).thenReturn(List.of(tcLek, tcSem));

        List<Map<String, Object>> evidenca = adminTeachingCourseService.getEvidenca(1, "1", "2025-2026");

        assertThat(evidenca).hasSize(1);
        Map<String, Object> row = evidenca.get(0);
        assertThat(row.get("courseEmri")).isEqualTo("Programim i Avancuar");
        assertThat(row.get("programEmri")).isEqualTo("Inxhinieri Informatike");

        // Bachelor factorLec = 12.0 => 3.0 * 12.0 * 1 = 36.0 ore
        assertThat(row.get("oreLeksion")).isEqualTo(36.0);

        // Bachelor factorSem = 14.0 => 1.5 * 14.0 * 2 groups = 42.0 ore
        assertThat(row.get("oreSeminar")).isEqualTo(42.0);
    }

    @Test
    @DisplayName("Unit: getEvidenca llogarit oret me faktorin e Masterit (Lec: 10, Sem: 12)")
    void testGetEvidenca_MasterCalculations() {
        Program progMaster = Program.builder()
                .programId(2)
                .specializimi("Inxhinieri Softuerike")
                .nivel("Master Shkencor")
                .build();

        Course course = Course.builder()
                .courseId(20)
                .emriCourse("Arkitektura e Sistemeve")
                .program(progMaster)
                .kredite(6)
                .krediteLeksion(BigDecimal.valueOf(3.0))
                .krediteSeminar(BigDecimal.valueOf(1.5))
                .semester("2")
                .build();

        Professor prof = Professor.builder().professorId(2).build();

        TeachingCourse tcLek = TeachingCourse.builder()
                .teachingCourseId(201)
                .course(course)
                .professor(prof)
                .roleType("LEKSION")
                .academicYear("2025-2026")
                .classes(Set.of())
                .build();

        when(teachingCourseRepository.findByProfessorIdWithDetails(2)).thenReturn(List.of(tcLek));

        List<Map<String, Object>> evidenca = adminTeachingCourseService.getEvidenca(2, "2", "2025-2026");

        assertThat(evidenca).hasSize(1);
        Map<String, Object> row = evidenca.get(0);

        // Master factorLec = 10.0 => 3.0 * 10.0 * 1 = 30.0 ore
        assertThat(row.get("oreLeksion")).isEqualTo(30.0);
    }

    @Test
    @DisplayName("Unit: getEvidenca filtron sakte sipas semestrit te caktuar")
    void testGetEvidenca_SemesterFiltering() {
        Course cSem1 = Course.builder().courseId(1).emriCourse("Lenda 1").semester("1").build();
        Course cSem2 = Course.builder().courseId(2).emriCourse("Lenda 2").semester("2").build();
        Professor prof = Professor.builder().professorId(3).build();

        TeachingCourse tc1 = TeachingCourse.builder().teachingCourseId(1).course(cSem1).professor(prof).academicYear("2025-2026").build();
        TeachingCourse tc2 = TeachingCourse.builder().teachingCourseId(2).course(cSem2).professor(prof).academicYear("2025-2026").build();

        when(teachingCourseRepository.findByProfessorIdWithDetails(3)).thenReturn(List.of(tc1, tc2));

        List<Map<String, Object>> sem1Result = adminTeachingCourseService.getEvidenca(3, "1", "2025-2026");
        assertThat(sem1Result).hasSize(1);
        assertThat(sem1Result.get(0).get("courseEmri")).isEqualTo("Lenda 1");

        List<Map<String, Object>> sem2Result = adminTeachingCourseService.getEvidenca(3, "2", "2025-2026");
        assertThat(sem2Result).hasSize(1);
        assertThat(sem2Result.get(0).get("courseEmri")).isEqualTo("Lenda 2");
    }
}
