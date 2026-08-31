package Savina.ftiApp.repository;

import Savina.ftiApp.entity.FailedCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FailedCourseRepository extends JpaRepository<FailedCourse, Integer> {
    List<FailedCourse> findByStudent_StudentId(Integer studentId);
    Optional<FailedCourse> findByStudent_StudentIdAndTeachingCourse_TeachingCourseId(Integer studentId, Integer teachingCourseId);
}
