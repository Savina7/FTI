package Savina.ftiApp.controller;

import Savina.ftiApp.dto.responseDTO.BranchStatsDto;
import Savina.ftiApp.dto.responseDTO.PedagogOptionsDto;
import Savina.ftiApp.dto.responseDTO.PedagogRegisterDto;
import Savina.ftiApp.dto.requestDTO.SaveAttendanceRequest;
import Savina.ftiApp.dto.requestDTO.SaveGradesRequest;
import Savina.ftiApp.service.PedagogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pedagog")
@RequiredArgsConstructor
public class PedagogController {

    private final PedagogService pedagogService;

    @GetMapping("/options")
    public ResponseEntity<PedagogOptionsDto> getPedagogOptions(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String email) {

        // Gjithmon perdor SecurityContext (JWT e validuar) si burim primar
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            email = auth.getName(); // email-i i pedagogut nga JWT
        }

        PedagogOptionsDto options = pedagogService.getPedagogOptions(userId, email);
        return ResponseEntity.ok(options);
    }

    @GetMapping("/branch-stats")
    public ResponseEntity<BranchStatsDto> getBranchStats(
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String dega,
            @RequestParam(required = false) Integer departmentId) {
        BranchStatsDto stats = pedagogService.getBranchStats(courseId, courseName, academicYear, dega, departmentId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/register")
    public ResponseEntity<PedagogRegisterDto> getRegisterData(
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) Integer classId,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String roleType) {
        PedagogRegisterDto data = pedagogService.getRegisterData(courseId, classId, academicYear, roleType);
        return ResponseEntity.ok(data);
    }

    @PostMapping("/grades")
    public ResponseEntity<Map<String, String>> saveGrades(@RequestBody SaveGradesRequest req) {
        pedagogService.saveGrades(req);
        return ResponseEntity.ok(Map.of("message", "Notat u ruajten me sukses."));
    }

    @PostMapping("/attendance")
    public ResponseEntity<Map<String, String>> saveAttendance(@RequestBody SaveAttendanceRequest req) {
        pedagogService.saveAttendance(req);
        return ResponseEntity.ok(Map.of("message", "Mungesat u ruajten me sukses."));
    }
}
