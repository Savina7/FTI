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

    @Query("select distinct tc from TeachingCourse tc "
            + "left join fetch tc.course c "
            + "left join fetch tc.classes cl "
            + "where tc.professor.professorId = :professorId")
    List<TeachingCourse> findByProfessorIdWithDetails(@Param("professorId") Integer professorId);

    @Query("select distinct tc from TeachingCourse tc "
            + "left join fetch tc.course c "
            + "left join fetch tc.classes cl "
            + "where tc.professor.professorId = :professorId")
    List<TeachingCourse> findTeachingCoursesByProfessorId(@Param("professorId") Integer professorId);

    @Query("SELECT DISTINCT tc.academicYear FROM TeachingCourse tc WHERE tc.academicYear IS NOT NULL AND tc.academicYear <> ''")
    List<String> findDistinctAcademicYears();
}
