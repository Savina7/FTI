package Savina.ftiApp.repository;

import Savina.ftiApp.entity.StudentPreEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentPreEnrollmentRepository extends JpaRepository<StudentPreEnrollment, Integer> {

    Optional<StudentPreEnrollment> findByNrMatrikulimit(String nrMatrikulimit);

    Optional<StudentPreEnrollment> findByNrMatrikulimitIgnoreCase(String nrMatrikulimit);

    Optional<StudentPreEnrollment> findByEmail(String email);

    Optional<StudentPreEnrollment> findByEmailIgnoreCase(String email);

    boolean existsByNrMatrikulimitIgnoreCase(String nrMatrikulimit);

    boolean existsByEmailIgnoreCase(String email);
}
