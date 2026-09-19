package Savina.ftiApp.service;

import Savina.ftiApp.entity.AcademicYear;
import Savina.ftiApp.repository.AcademicYearRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AcademicYearService {

    private final AcademicYearRepository academicYearRepository;

    public static final String DEFAULT_ACADEMIC_YEAR = "2025-2026";

    @PostConstruct
    @Transactional
    public void initAndCleanupAcademicYears() {
        try {
            Optional<AcademicYear> activeYear = academicYearRepository.findByIsActive(1);
            if (activeYear.isEmpty()) {
                academicYearRepository.deactivateAll();
                AcademicYear defaultYear = academicYearRepository.findByYearLabel(DEFAULT_ACADEMIC_YEAR)
                        .orElseGet(() -> AcademicYear.builder()
                                .yearLabel(DEFAULT_ACADEMIC_YEAR)
                                .isActive(1)
                                .build());
                defaultYear.setIsActive(1);
                academicYearRepository.save(defaultYear);
            }

            // Clean up unactivated dummy years from old migrations (e.g., 2026-2027 or 2024-2025 before they are promoted)
            List<AcademicYear> allYears = academicYearRepository.findAll();
            for (AcademicYear y : allYears) {
                if (y.getIsActive() != 1 && !DEFAULT_ACADEMIC_YEAR.equals(y.getYearLabel())) {
                    academicYearRepository.delete(y);
                }
            }
        } catch (Exception e) {
            log.warn("init academic year cleanup notice: {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public String getCurrentAcademicYear() {
        return academicYearRepository.findByIsActive(1)
                .map(AcademicYear::getYearLabel)
                .orElse(DEFAULT_ACADEMIC_YEAR);
    }

    @Transactional
    public String advanceAcademicYear() {
        String current = getCurrentAcademicYear();
        String nextYear = calculateNextAcademicYear(current);

        log.info("Avancimi i vitit akademik: nga {} në {}", current, nextYear);

        academicYearRepository.deactivateAll();

        AcademicYear nextAcademicYear = academicYearRepository.findByYearLabel(nextYear)
                .orElseGet(() -> AcademicYear.builder()
                        .yearLabel(nextYear)
                        .isActive(1)
                        .build());

        nextAcademicYear.setIsActive(1);
        academicYearRepository.save(nextAcademicYear);

        return nextYear;
    }

    @Transactional
    public String setCurrentAcademicYear(String yearLabel) {
        if (yearLabel == null || yearLabel.isBlank()) {
            throw new IllegalArgumentException("Viti akademik nuk mund të jetë bosh");
        }
        String normalized = yearLabel.trim();

        academicYearRepository.deactivateAll();

        AcademicYear targetYear = academicYearRepository.findByYearLabel(normalized)
                .orElseGet(() -> AcademicYear.builder()
                        .yearLabel(normalized)
                        .isActive(1)
                        .build());

        targetYear.setIsActive(1);
        academicYearRepository.save(targetYear);

        return targetYear.getYearLabel();
    }

    @Transactional(readOnly = true)
    public List<String> getAvailableAcademicYears() {
        Set<String> yearSet = new TreeSet<>((a, b) -> b.compareTo(a));

        List<String> dbYears = academicYearRepository.findAllYearLabelsDesc();
        if (dbYears != null) yearSet.addAll(dbYears);

        String current = getCurrentAcademicYear();
        yearSet.add(current);

        List<String> result = new ArrayList<>();
        result.add(current);
        for (String y : yearSet) {
            if (!y.equals(current)) {
                result.add(y);
            }
        }
        return result;
    }

    public static String calculateNextAcademicYear(String academicYear) {
        if (academicYear == null || !academicYear.contains("-")) {
            return "2026-2027";
        }
        try {
            String[] parts = academicYear.trim().split("-");
            int start = Integer.parseInt(parts[0].trim());
            int end = Integer.parseInt(parts[1].trim());
            return (start + 1) + "-" + (end + 1);
        } catch (Exception e) {
            return "2026-2027";
        }
    }
}
