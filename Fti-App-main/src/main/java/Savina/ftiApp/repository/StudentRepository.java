package Savina.ftiApp.repository;

import Savina.ftiApp.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer> {

    Optional<Student> findByNrMatrikulimit(String nrMatrikulimit);

    boolean existsByNrMatrikulimitIgnoreCase(String nrMatrikulimit);

    Optional<Student> findByUserUserId(Integer userId);

    Optional<Student> findByUserEmailIgnoreCase(String email);

    List<Student> findByClasses_ClassId(Integer classId);

    List<Student> findByClasses_ClassIdIn(java.util.Collection<Integer> classIds);

    List<Student> findByProgram_ProgramId(Integer programId);

    List<Student> findByProgram_ProgramIdAndVitStudimit(Integer programId, Integer vitStudimit);
}
