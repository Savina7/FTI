package Savina.ftiApp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Savina.ftiApp.entity.FailedCourse;

@Repository
public interface FailedCourseRepository extends JpaRepository<FailedCourse, Integer> {

    List<FailedCourse> findByStudent_StudentId(Integer studentId);

    Optional<FailedCourse> findByStudent_StudentIdAndTeachingCourse_TeachingCourseId(Integer studentId, Integer teachingCourseId);
}
