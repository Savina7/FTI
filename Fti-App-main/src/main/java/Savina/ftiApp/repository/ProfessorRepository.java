package Savina.ftiApp.repository;

import Savina.ftiApp.entity.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfessorRepository extends JpaRepository<Professor, Integer> {
    Optional<Professor> findByUserUserId(Integer userId);
    Optional<Professor> findByUserEmailIgnoreCase(String email);
}
