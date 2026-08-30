package Savina.ftiApp.repository;

import Savina.ftiApp.entity.ExamSeason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExamSeasonRepository extends JpaRepository<ExamSeason, Integer> {

    Optional<ExamSeason> findByAcademicYearAndSeasonTypeAndProgram_ProgramId(String academicYear, String seasonType, Integer programId);

    Optional<ExamSeason> findByAcademicYearAndSeasonTypeAndProgramIsNull(String academicYear, String seasonType);
}
