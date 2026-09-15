package Savina.ftiApp.controller;

import Savina.ftiApp.dto.requestDTO.ExamScheduleRequestDto;
import Savina.ftiApp.dto.responseDTO.ExamScheduleDto;
import Savina.ftiApp.entity.Room;
import Savina.ftiApp.repository.RoomRepository;
import Savina.ftiApp.service.AdminExamScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/exams")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminExamScheduleController {

    private final AdminExamScheduleService examScheduleService;
    private final RoomRepository roomRepo;
    private final Savina.ftiApp.repository.ProgramRepository programRepo;

    @GetMapping("/programs")
    public ResponseEntity<List<Map<String, Object>>> getPrograms() {
        List<Savina.ftiApp.entity.Program> list = programRepo.findAll();
        List<Map<String, Object>> result = list.stream().map(p -> {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("programId", p.getProgramId());
            String name = (p.getNivel() != null ? p.getNivel() + " " : "")
                    + (p.getSpecializimi() != null ? p.getSpecializimi() : "");
            m.put("name", name.trim());
            return m;
        }).sorted((a, b) -> ((String) a.get("name")).compareToIgnoreCase((String) b.get("name")))
        .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/courses")
    public ResponseEntity<List<Map<String, Object>>> getCourses(
            @RequestParam Integer programId,
            @RequestParam(required = false) String semester) {
        return ResponseEntity.ok(examScheduleService.getCoursesForProgramAndSemester(programId, semester));
    }

    @GetMapping
    public ResponseEntity<List<ExamScheduleDto>> getExams(
            @RequestParam Integer programId,
            @RequestParam(required = false) String season) {
        return ResponseEntity.ok(examScheduleService.getExamsByProgramAndSeason(programId, season));
    }

    @PostMapping
    public ResponseEntity<ExamScheduleDto> saveExam(@RequestBody ExamScheduleRequestDto req) {
        return ResponseEntity.ok(examScheduleService.saveExam(req));
    }

    @DeleteMapping("/{examId}")
    public ResponseEntity<Map<String, String>> deleteExam(@PathVariable Integer examId) {
        examScheduleService.deleteExam(examId);
        return ResponseEntity.ok(Map.of("message", "Provimi u fshi me sukses"));
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<Room>> getRooms() {
        return ResponseEntity.ok(roomRepo.findAll());
    }

    @GetMapping("/season-dates")
    public ResponseEntity<Savina.ftiApp.dto.responseDTO.ExamSeasonDto> getSeasonDates(
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String season,
            @RequestParam(required = false) Integer programId) {
        return ResponseEntity.ok(examScheduleService.getSeasonDates(academicYear, season, programId));
    }

    @PostMapping("/season-dates")
    public ResponseEntity<Savina.ftiApp.dto.responseDTO.ExamSeasonDto> saveSeasonDates(
            @RequestBody Savina.ftiApp.dto.responseDTO.ExamSeasonDto dto) {
        return ResponseEntity.ok(examScheduleService.saveSeasonDates(dto));
    }
}
