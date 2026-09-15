package Savina.ftiApp.controller;

import Savina.ftiApp.dto.responseDTO.AuthResponse;
import Savina.ftiApp.dto.requestDTO.ProfessorRegisterRequest;
import Savina.ftiApp.dto.requestDTO.StudentRegisterRequest;
import Savina.ftiApp.dto.requestDTO.VerifyRequest;
import Savina.ftiApp.entity.Role;
import Savina.ftiApp.entity.User;
import Savina.ftiApp.security.JwtService;
import Savina.ftiApp.service.RegisterService;
import Savina.ftiApp.service.VerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import Savina.ftiApp.dto.responseDTO.DepartmentDto;
import Savina.ftiApp.repository.DepartmentRepository;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RegisterController {

    private final RegisterService     registerService;
    private final VerificationService verificationService;
    private final JwtService          jwtService;
    private final DepartmentRepository departmentRepo;

    @GetMapping("/departments")
    public List<DepartmentDto> getDepartments() {
        return departmentRepo.findAll().stream()
                .map(d -> new DepartmentDto(d.getDepartmentId(), d.getEmerDepartamenti()))
                .collect(Collectors.toList());
    }

    @PostMapping("/register/student")
    public AuthResponse registerStudent(@Valid @RequestBody StudentRegisterRequest req) {
        Integer userId = registerService.registerStudent(req);
        return new AuthResponse(null, userId, req.getEmail(), "STUDENT", "Regjistrimi u krye me sukses. Kontrolloni email-in per kodin e verifikimit.");
    }

    @PostMapping("/register/professor")
    public AuthResponse registerProfessor(@Valid @RequestBody ProfessorRegisterRequest req) {
        Integer userId = registerService.registerProfessor(req);
        return new AuthResponse(null, userId, req.getEmail(), "PROFESSOR", "Regjistrimi u krye me sukses. Kontrolloni email-in per kodin e verifikimit.");
    }

    @PostMapping("/verify")
    public AuthResponse verifyCode(@Valid @RequestBody VerifyRequest req) {
        User user = verificationService.verifyCode(req.getUserId(), req.getCode());

        String roleName = user.getRoles().stream()
                .findFirst()
                .map(Role::getRoleName)
                .orElse("STUDENT");

        String token = jwtService.generateToken(user.getUserId(), user.getEmail(), roleName);

        return new AuthResponse(token, user.getUserId(), user.getEmail(), roleName, "Llogaria u verifikua me sukses!");
    }

    @PostMapping("/verify/resend")
    public AuthResponse resendCode(@RequestParam Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId eshte i detyrueshem.");
        }
        verificationService.resendCode(userId);
        return new AuthResponse("Kodi i ri u dergua me sukses.");
    }
}
