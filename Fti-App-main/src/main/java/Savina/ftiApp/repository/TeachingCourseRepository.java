package Savina.ftiApp.repository;

import Savina.ftiApp.entity.TeachingCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeachingCourseRepository extends JpaRepository<TeachingCourse, Integer> {
    List<TeachingCourse> findByCourseCourseId(Integer courseId);
    List<TeachingCourse> findByProfessorProfessorId(Integer professorId);
    List<TeachingCourse> findByClasses_ClassId(Integer classId);

    @Query("SELECT DISTINCT tc FROM TeachingCourse tc " +
           "LEFT JOIN FETCH tc.course c " +
           "LEFT JOIN FETCH tc.classes cl " +
           "WHERE tc.professor.professorId = :professorId")
    List<TeachingCourse> findByProfessorIdWithDetails(@Param("professorId") Integer professorId);

    @Query("SELECT DISTINCT tc FROM TeachingCourse tc " +
           "LEFT JOIN FETCH tc.course c " +
           "LEFT JOIN FETCH tc.classes cl " +
           "WHERE tc.professor.professorId = :professorId")
    List<TeachingCourse> findTeachingCoursesByProfessorId(@Param("professorId") Integer professorId);
}
