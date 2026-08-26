package Savina.ftiApp.controller;

import Savina.ftiApp.dto.responseDTO.AuthResponse;
import Savina.ftiApp.dto.requestDTO.LoginRequest;
import Savina.ftiApp.service.LoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return loginService.login(req);
    }
}
