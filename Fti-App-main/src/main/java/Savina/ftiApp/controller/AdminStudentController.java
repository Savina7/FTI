package Savina.ftiApp.controller;

import Savina.ftiApp.dto.responseDTO.StudentAdminDto;
import Savina.ftiApp.dto.requestDTO.StudentPreEnrollmentRequest;
import Savina.ftiApp.service.AdminStudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/students")
@RequiredArgsConstructor
public class AdminStudentController {

    private final AdminStudentService adminStudentService;

    @GetMapping
    public List<StudentAdminDto> getAllStudents() {
        return adminStudentService.getAllStudents();
    }

    @PostMapping("/pre-enrollment")
    public StudentAdminDto addStudentPreEnrollment(@Valid @RequestBody StudentPreEnrollmentRequest req) {
        return adminStudentService.addStudentPreEnrollment(req);
    }

    @PostMapping("/pre-enrollment/{id}/verify")
    public StudentAdminDto verifyStudentDirectly(@PathVariable Integer id) {
        return adminStudentService.verifyAndRegisterDirectly(id);
    }

    @DeleteMapping("/pre-enrollment/{id}")
    public void deleteStudentPreEnrollment(@PathVariable Integer id) {
        adminStudentService.deleteStudentPreEnrollment(id);
    }

    @DeleteMapping("/{id}")
    public void deleteStudent(@PathVariable Integer id, @RequestParam(required = false) String status) {
        adminStudentService.deleteStudent(id, status);
    }

    @PutMapping("/{id}")
    public StudentAdminDto updateStudent(@PathVariable Integer id, @Valid @RequestBody StudentPreEnrollmentRequest req) {
        return adminStudentService.updateStudent(id, req);
    }

    @GetMapping("/nivelet")
    public List<String> getNivelet() {
        return adminStudentService.getAllNivele();
    }

    @GetMapping("/specializimet")
    public List<String> getSpecializimet(
            @RequestParam(required = false) String nivel,
            @RequestParam(required = false) Boolean doubleDegree) {
        if (nivel != null && !nivel.trim().isEmpty()) {
            if (doubleDegree != null) {
                return adminStudentService.getAllSpecializimeFiltered(nivel, doubleDegree);
            } else {
                return adminStudentService.getAllSpecializimeByNivelOnly(nivel);
            }
        } else if (doubleDegree != null) {
            return adminStudentService.getAllSpecializimeFiltered(null, doubleDegree);
        }
        return adminStudentService.getAllSpecializime();
    }
}
