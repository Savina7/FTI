package Savina.ftiApp.repository;

import Savina.ftiApp.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Integer> {

    List<Grade> findByStudentStudentId(Integer studentId);

    List<Grade> findByTeachingCourse_Course_CourseId(Integer courseId);

    Optional<Grade> findByStudent_StudentIdAndTeachingCourse_Course_CourseId(Integer studentId, Integer courseId);
}
