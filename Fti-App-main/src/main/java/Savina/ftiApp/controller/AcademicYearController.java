package Savina.ftiApp.controller;

import Savina.ftiApp.dto.responseDTO.AcademicYearDto;
import Savina.ftiApp.service.AcademicYearService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/academic-year")
@RequiredArgsConstructor
public class AcademicYearController {

    private final AcademicYearService academicYearService;

    @GetMapping("/current")
    public AcademicYearDto getCurrentAcademicYear() {
        return AcademicYearDto.builder()
                .currentAcademicYear(academicYearService.getCurrentAcademicYear())
                .availableYears(academicYearService.getAvailableAcademicYears())
                .build();
    }

    @PostMapping("/set")
    public AcademicYearDto setCurrentAcademicYear(@RequestBody Map<String, String> body) {
        String year = body.get("academicYear");
        String updated = academicYearService.setCurrentAcademicYear(year);
        return AcademicYearDto.builder()
                .currentAcademicYear(updated)
                .availableYears(academicYearService.getAvailableAcademicYears())
                .build();
    }
}
