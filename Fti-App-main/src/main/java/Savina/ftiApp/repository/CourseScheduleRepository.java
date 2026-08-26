package Savina.ftiApp.repository;

import Savina.ftiApp.entity.CourseSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseScheduleRepository extends JpaRepository<CourseSchedule, Integer> {

    @Query("SELECT DISTINCT cs.academicYear FROM CourseSchedule cs WHERE cs.academicYear IS NOT NULL AND cs.academicYear <> ''")
    List<String> findDistinctAcademicYears();
}
