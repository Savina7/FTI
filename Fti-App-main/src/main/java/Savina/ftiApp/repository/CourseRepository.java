package Savina.ftiApp.repository;

import Savina.ftiApp.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {

    List<Course> findByProgramProgramId(Integer programId);

    List<Course> findByDepartmentsDepartmentId(Integer departmentId);
}
