package Savina.ftiApp.repository;

import Savina.ftiApp.entity.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfessorRepository extends JpaRepository<Professor, Integer> {
    Optional<Professor> findByUserUserId(Integer userId);
    Optional<Professor> findByUserEmailIgnoreCase(String email);

    @Query("SELECT p FROM Professor p LEFT JOIN FETCH p.user u WHERE u.userId = :userId")
    Optional<Professor> findByUserIdWithUser(@Param("userId") Integer userId);

    @Query("SELECT p FROM Professor p LEFT JOIN FETCH p.user u WHERE LOWER(TRIM(u.email)) = LOWER(TRIM(:email))")
    Optional<Professor> findByUserEmailWithUser(@Param("email") String email);

    @Query("SELECT p FROM Professor p LEFT JOIN FETCH p.user u WHERE u.userId = :userId")
    Optional<Professor> findProfessorByUserId(@Param("userId") Integer userId);

    @Query("SELECT p FROM Professor p LEFT JOIN FETCH p.user u WHERE LOWER(TRIM(u.email)) = LOWER(TRIM(:email))")
    Optional<Professor> findProfessorByUserEmail(@Param("email") String email);
}
