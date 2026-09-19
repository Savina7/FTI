package Savina.ftiApp.unit;

import Savina.ftiApp.entity.Course;
import Savina.ftiApp.entity.Professor;
import Savina.ftiApp.entity.TeachingCourse;
import Savina.ftiApp.repository.ProfessorRepository;
import Savina.ftiApp.repository.TeachingCourseRepository;
import Savina.ftiApp.service.AdminTeachingCourseService;
import Savina.ftiApp.service.PedagogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest
public class PrintCoursesTest {

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private TeachingCourseRepository teachingCourseRepository;

    @Autowired
    private AdminTeachingCourseService adminTeachingCourseService;

    @Autowired
    private PedagogService pedagogService;

    @Test
    @org.springframework.transaction.annotation.Transactional
    public void debugAlbaHaveriku() {
        Professor alba = professorRepository.findAll().stream()
                .filter(p -> p.getUser() != null && ("Alba".equalsIgnoreCase(p.getUser().getEmri()) || "Haveriku".equalsIgnoreCase(p.getUser().getMbiemri())))
                .findFirst().orElse(null);

        if (alba == null) {
            System.out.println("ALBA HAVERIKU NOT FOUND");
            return;
        }

        System.out.println("=== ALBA HAVERIKU (PROF ID: " + alba.getProfessorId() + ") ===");
        List<TeachingCourse> tcs = teachingCourseRepository.findByProfessorProfessorId(alba.getProfessorId());
        for (TeachingCourse tc : tcs) {
            Course c = tc.getCourse();
            String prog = (c != null && c.getProgram() != null) ? c.getProgram().getSpecializimi() : "—";
            String nivel = (c != null && c.getProgram() != null && c.getProgram().getNivel() != null) ? c.getProgram().getNivel().toString() : "PA NIVEL";
            int sem = (c != null && c.getSemester() != null) ? Integer.parseInt(c.getSemester()) : 1;
            int vit = (c != null && c.getStudyYear() != null) ? c.getStudyYear() : 1;
            
            System.out.println("Nivel: " + nivel + 
                    " | Course: " + (c != null ? c.getEmriCourse() : "null") + 
                    " | Dega: " + prog + 
                    " | Viti: " + vit + 
                    " | Semestri: " + sem + 
                    " | Roli: " + tc.getRoleType() + 
                    " | Javore: " + tc.getWeeklyHours() + 
                    " | Totale: " + tc.getTotalHours() + 
                    " | Klasat: " + (tc.getClasses() != null ? tc.getClasses().stream().map(cl -> cl.getEmriClass()).toList() : "[]"));
        }
    }
}
