package Savina.ftiApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Savina.ftiApp.entity.ExamSchedule;

@Repository
public interface ExamScheduleRepository extends JpaRepository<ExamSchedule, Integer> {

    List<ExamSchedule> findByProgram_ProgramId(Integer programId);

    List<ExamSchedule> findByProgram_ProgramIdAndType(Integer programId, String type);

    boolean existsByCourse_CourseIdAndType(Integer courseId, String type);

    List<ExamSchedule> findByExamDateBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
}
