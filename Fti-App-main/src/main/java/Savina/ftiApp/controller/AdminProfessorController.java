package Savina.ftiApp.controller;

import Savina.ftiApp.dto.responseDTO.ProfessorAdminDto;
import Savina.ftiApp.dto.requestDTO.ProfessorPreEnrollmentRequest;
import Savina.ftiApp.service.AdminProfessorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/professors")
@RequiredArgsConstructor
public class AdminProfessorController {

    private final AdminProfessorService adminProfessorService;

    @GetMapping
    public List<ProfessorAdminDto> getAllProfessors() {
        return adminProfessorService.getAllProfessors();
    }

    @PostMapping("/pre-enrollment")
    public ProfessorAdminDto addProfessorPreEnrollment(@Valid @RequestBody ProfessorPreEnrollmentRequest req) {
        return adminProfessorService.addProfessorPreEnrollment(req);
    }

    @PostMapping("/pre-enrollment/{id}/verify")
    public ProfessorAdminDto verifyProfessorDirectly(@PathVariable Integer id) {
        return adminProfessorService.verifyAndRegisterDirectly(id);
    }

    @PutMapping("/{id}")
    public ProfessorAdminDto updateProfessor(@PathVariable Integer id, @Valid @RequestBody ProfessorPreEnrollmentRequest req) {
        return adminProfessorService.updateProfessor(id, req);
    }

    @DeleteMapping("/{id}")
    public void deleteProfessor(@PathVariable Integer id, @RequestParam(required = false) String status) {
        adminProfessorService.deleteProfessor(id, status);
    }

    @GetMapping("/departamentet")
    public List<String> getDepartamentet() {
        return adminProfessorService.getAllDepartments();
    }
}
