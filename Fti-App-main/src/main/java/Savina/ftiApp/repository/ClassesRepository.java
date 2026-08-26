package Savina.ftiApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Savina.ftiApp.entity.Classes;
import Savina.ftiApp.entity.Program;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClassesRepository extends JpaRepository<Classes, Integer> {

    Optional<Classes> findFirstByProgramAndVitStudimit(Program program, Integer vitStudimit);

    List<Classes> findByProgram_ProgramId(Integer programId);
}
