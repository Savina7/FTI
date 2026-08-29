package Savina.ftiApp.repository;

import Savina.ftiApp.entity.CourseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseTypeRepository extends JpaRepository<CourseType, Integer> {
    Optional<CourseType> findByEmriTypeIgnoreCase(String emriType);
}
