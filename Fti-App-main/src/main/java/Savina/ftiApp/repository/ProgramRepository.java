package Savina.ftiApp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import Savina.ftiApp.entity.Program;

@Repository
public interface ProgramRepository extends JpaRepository<Program, Integer> {

    Optional<Program> findFirstByNivelAndSpecializimiAndDiplomeDyfishte(String nivel, String specializimi, String diplomeDyfishte);

    Optional<Program> findFirstByNivelAndSpecializimi(String nivel, String specializimi);

    Optional<Program> findFirstByNivel(String nivel);

    @Query("SELECT DISTINCT p.nivel FROM Program p WHERE p.nivel IS NOT NULL ORDER BY p.nivel")
    List<String> findDistinctNivele();

    @Query("SELECT DISTINCT p.specializimi FROM Program p WHERE p.specializimi IS NOT NULL ORDER BY p.specializimi")
    List<String> findDistinctSpecializime();

    @Query("SELECT DISTINCT p.specializimi FROM Program p WHERE (:nivel IS NULL OR LOWER(p.nivel) = LOWER(:nivel)) AND ((:isDouble = true AND (LOWER(p.diplomeDyfishte) IN ('po','y','1','true') OR LOWER(p.lloji) LIKE '%double%' OR LOWER(p.lloji) LIKE '%d2%')) OR (:isDouble = false AND (p.diplomeDyfishte IS NULL OR LOWER(p.diplomeDyfishte) IN ('jo','n','0','false')) AND (p.lloji IS NULL OR (LOWER(p.lloji) NOT LIKE '%double%' AND LOWER(p.lloji) NOT LIKE '%d2%')))) ORDER BY p.specializimi")
    List<String> findSpecializimeByNivelAndDoubleDegree(@Param("nivel") String nivel, @Param("isDouble") Boolean isDouble);

    @Query("SELECT DISTINCT p.specializimi FROM Program p WHERE (:nivel IS NULL OR LOWER(p.nivel) = LOWER(:nivel)) ORDER BY p.specializimi")
    List<String> findSpecializimeByNivelOnly(@Param("nivel") String nivel);
}
