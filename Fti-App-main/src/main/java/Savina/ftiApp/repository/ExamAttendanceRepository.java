package Savina.ftiApp.repository;

import Savina.ftiApp.entity.ExamAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamAttendanceRepository extends JpaRepository<ExamAttendance, Integer> {

    List<ExamAttendance> findByExamSchedule_ExamId(Integer examId);

    Optional<ExamAttendance> findByExamSchedule_ExamIdAndStudent_StudentId(Integer examId, Integer studentId);

    List<ExamAttendance> findByExamSchedule_ExamIdIn(List<Integer> examIds);
}
