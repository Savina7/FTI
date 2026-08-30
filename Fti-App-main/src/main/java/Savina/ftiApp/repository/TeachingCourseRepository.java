package Savina.ftiApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import Savina.ftiApp.entity.TeachingCourse;

@Repository
public interface TeachingCourseRepository extends JpaRepository<TeachingCourse, Integer> {

    List<TeachingCourse> findByCourseCourseId(Integer courseId);

    List<TeachingCourse> findByCourse_Program_ProgramId(Integer programId);

    List<TeachingCourse> findByProfessorProfessorId(Integer professorId);

    List<TeachingCourse> findByClasses_ClassId(Integer classId);

    @Query("SELECT DISTINCT tc FROM TeachingCourse tc "
            + "LEFT JOIN FETCH tc.course c "
            + "LEFT JOIN FETCH tc.classes cl "
            + "WHERE tc.professor.professorId = :professorId")
    List<TeachingCourse> findByProfessorIdWithDetails(@Param("professorId") Integer professorId);

    @Query("SELECT DISTINCT tc FROM TeachingCourse tc "
            + "LEFT JOIN FETCH tc.course c "
            + "LEFT JOIN FETCH tc.classes cl "
            + "WHERE tc.professor.professorId = :professorId")
    List<TeachingCourse> findTeachingCoursesByProfessorId(@Param("professorId") Integer professorId);
}
