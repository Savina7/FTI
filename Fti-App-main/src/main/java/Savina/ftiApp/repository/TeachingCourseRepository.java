package Savina.ftiApp.repository;

import Savina.ftiApp.entity.TeachingCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeachingCourseRepository extends JpaRepository<TeachingCourse, Integer> {
    List<TeachingCourse> findByCourseCourseId(Integer courseId);
    List<TeachingCourse> findByProfessorProfessorId(Integer professorId);
    List<TeachingCourse> findByClasses_ClassId(Integer classId);
}
