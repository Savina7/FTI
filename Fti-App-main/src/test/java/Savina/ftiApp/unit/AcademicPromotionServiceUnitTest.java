package Savina.ftiApp.unit;

import Savina.ftiApp.dto.responseDTO.PromotionResultDto;
import Savina.ftiApp.entity.Classes;
import Savina.ftiApp.entity.Course;
import Savina.ftiApp.entity.Grade;
import Savina.ftiApp.entity.Student;
import Savina.ftiApp.entity.TeachingCourse;
import Savina.ftiApp.repository.ClassesRepository;
import Savina.ftiApp.repository.GradeRepository;
import Savina.ftiApp.repository.StudentRepository;
import Savina.ftiApp.service.AcademicPromotionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcademicPromotionServiceUnitTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private ClassesRepository classesRepository;

    @InjectMocks
    private AcademicPromotionService promotionService;

    @Test
    @DisplayName("Unit: Studenti i vitit 1 me mbi 30 kredite promovohet ne vitin 2")
    void testPromoteYear1To2Success() {
        Student student = new Student();
        student.setStudentId(10);
        student.setVitStudimit(1);
        student.setStatus("ACTIVE");

        // Krijo lende me 35 kredite te kaluara
        Course c1 = new Course();
        c1.setCourseId(1);
        c1.setKredite(35);

        TeachingCourse tc = new TeachingCourse();
        tc.setCourse(c1);

        Grade g = new Grade();
        g.setTeachingCourse(tc);
        g.setGrade(BigDecimal.valueOf(8.0));

        when(studentRepository.findAll()).thenReturn(List.of(student));
        when(gradeRepository.findByStudentStudentId(10)).thenReturn(List.of(g));

        PromotionResultDto result = promotionService.promoteAllStudents();

        assertThat(result).isNotNull();
        assertThat(result.getPromotedYear1To2()).isEqualTo(1);
        assertThat(student.getVitStudimit()).isEqualTo(2);
        verify(studentRepository, times(1)).save(student);
    }

    @Test
    @DisplayName("Unit: Studenti i vitit 1 me me pak se 30 kredite mbetet perserites (REPEATING)")
    void testStudentYear1Repeating() {
        Student student = new Student();
        student.setStudentId(20);
        student.setVitStudimit(1);
        student.setStatus("ACTIVE");

        // Vetem 20 kredite (nuk mjaftojne per vitin 2)
        Course c1 = new Course();
        c1.setCourseId(1);
        c1.setKredite(20);

        TeachingCourse tc = new TeachingCourse();
        tc.setCourse(c1);

        Grade g = new Grade();
        g.setTeachingCourse(tc);
        g.setGrade(BigDecimal.valueOf(7.0));

        when(studentRepository.findAll()).thenReturn(List.of(student));
        when(gradeRepository.findByStudentStudentId(20)).thenReturn(List.of(g));

        PromotionResultDto result = promotionService.promoteAllStudents();

        assertThat(result).isNotNull();
        assertThat(result.getRepeatingYear1()).isEqualTo(1);
        assertThat(student.getStatus()).isEqualTo("REPEATING");
        assertThat(student.getVitStudimit()).isEqualTo(1);
        verify(studentRepository, times(1)).save(student);
    }
}
