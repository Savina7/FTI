package Savina.ftiApp.repository;

import Savina.ftiApp.entity.ProfessorPreEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfessorPreEnrollmentRepository extends JpaRepository<ProfessorPreEnrollment, Integer> {

    Optional<ProfessorPreEnrollment> findByEmail(String email);

    Optional<ProfessorPreEnrollment> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
