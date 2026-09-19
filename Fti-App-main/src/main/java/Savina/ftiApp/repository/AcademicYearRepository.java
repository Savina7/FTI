package Savina.ftiApp.repository;

import Savina.ftiApp.entity.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AcademicYearRepository extends JpaRepository<AcademicYear, Integer> {

    Optional<AcademicYear> findByIsActive(Integer isActive);

    Optional<AcademicYear> findByYearLabel(String yearLabel);

    @Query("SELECT a.yearLabel FROM AcademicYear a ORDER BY a.yearLabel DESC")
    List<String> findAllYearLabelsDesc();

    @Modifying
    @Query("UPDATE AcademicYear a SET a.isActive = 0")
    void deactivateAll();
}
