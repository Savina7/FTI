package Savina.ftiApp.unit;

import Savina.ftiApp.entity.Course;
import Savina.ftiApp.entity.Program;
import Savina.ftiApp.repository.CourseRepository;
import Savina.ftiApp.repository.ProgramRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
public class PrintCoursesTest {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ProgramRepository programRepository;

    @Test
    @Transactional
    public void printCoursesForProgram() {
        System.out.println("=== ALL PROGRAMS ===");
        List<Program> programs = programRepository.findAll();
        for (Program p : programs) {
            System.out.println("Program ID: " + p.getProgramId() + " | Dega: " + p.getSpecializimi() + " | Nivel: " + p.getNivel() + " | Lloji: " + p.getLloji() + " | DiplomeDyfishte: " + p.getDiplomeDyfishte());
        }

        System.out.println("=== ALL COURSES ===");
        List<Course> courses = courseRepository.findAll();
        for (Course c : courses) {
            if (c.getProgram() != null && c.getProgram().getProgramId() == 8L) {
                System.out.println("MPS -> Course ID: " + c.getCourseId() + " | Lenda: " + c.getEmriCourse() + " | Viti: " + c.getStudyYear() + " | Kredite: " + c.getKredite());
            }
        }
    }
}
