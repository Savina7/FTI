package Savina.ftiApp.repository;

import Savina.ftiApp.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {

    List<Attendance> findByStudentStudentId(Integer studentId);

    List<Attendance> findByTeachingCourse_Course_CourseId(Integer courseId);

    List<Attendance> findByStudent_StudentIdAndTeachingCourse_Course_CourseId(Integer studentId, Integer courseId);

    Optional<Attendance> findByStudent_StudentIdAndTeachingCourse_TeachingCourseIdAndDataAttendance(Integer studentId, Integer teachingCourseId, LocalDate dataAttendance);
}
