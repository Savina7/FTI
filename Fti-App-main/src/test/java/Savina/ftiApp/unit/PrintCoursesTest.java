package Savina.ftiApp.unit;

import Savina.ftiApp.entity.Course;
import Savina.ftiApp.entity.TeachingCourse;
import Savina.ftiApp.repository.CourseRepository;
import Savina.ftiApp.repository.TeachingCourseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

public class PrintCoursesTest {

    @Test
    public void debugMigration() {
        System.out.println("=== TEST READY ===");
        try (java.sql.Connection conn = java.sql.DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521/XEPDB1", "FTIAPP", "1");
             java.sql.Statement stmt = conn.createStatement()) {

            System.out.println("Database connection OK.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
