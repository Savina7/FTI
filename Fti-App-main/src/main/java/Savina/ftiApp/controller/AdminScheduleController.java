package Savina.ftiApp.controller;

import Savina.ftiApp.dto.requestDTO.ScheduleRequest;
import Savina.ftiApp.dto.responseDTO.RoomAvailabilityDto;
import Savina.ftiApp.dto.responseDTO.ScheduleResponseDto;
import Savina.ftiApp.service.AdminScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/schedules")
@RequiredArgsConstructor
public class AdminScheduleController {

    private final AdminScheduleService adminScheduleService;

    @GetMapping("/departments")
    public ResponseEntity<List<Map<String, Object>>> getDepartments() {
        return ResponseEntity.ok(adminScheduleService.getDepartments());
    }

    @GetMapping("/levels")
    public ResponseEntity<List<String>> getLevels(@RequestParam(required = false) Integer departmentId) {
        return ResponseEntity.ok(adminScheduleService.getLevels(departmentId));
    }

    @GetMapping("/branches")
    public ResponseEntity<List<Map<String, Object>>> getBranches(
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) String nivel) {
        return ResponseEntity.ok(adminScheduleService.getBranches(departmentId, nivel));
    }

    @GetMapping("/years")
    public ResponseEntity<List<Integer>> getYears(@RequestParam(required = false) Integer programId) {
        return ResponseEntity.ok(adminScheduleService.getYears(programId));
    }

    @GetMapping("/courses")
    public ResponseEntity<List<Map<String, Object>>> getCourses(
            @RequestParam(required = false) Integer programId,
            @RequestParam(required = false) Integer studyYear,
            @RequestParam(required = false) String semester) {
        return ResponseEntity.ok(adminScheduleService.getCourses(programId, studyYear, semester));
    }

    @GetMapping("/course-types")
    public ResponseEntity<List<String>> getCourseTypes() {
        return ResponseEntity.ok(adminScheduleService.getCourseTypes());
    }

    @GetMapping("/professors")
    public ResponseEntity<Map<String, Object>> getProfessors(
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) String roleType) {
        return ResponseEntity.ok(adminScheduleService.getProfessorsForCourseAndType(courseId, roleType));
    }

    @GetMapping("/classes")
    public ResponseEntity<List<Map<String, Object>>> getClasses(
            @RequestParam(required = false) Integer programId,
            @RequestParam(required = false) Integer studyYear,
            @RequestParam(required = false) Integer courseId) {
        return ResponseEntity.ok(adminScheduleService.getClasses(programId, studyYear, courseId));
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<Map<String, Object>>> getRooms() {
        return ResponseEntity.ok(adminScheduleService.getRooms());
    }

    @GetMapping("/check-room")
    public ResponseEntity<RoomAvailabilityDto> checkRoom(
            @RequestParam(required = false) Integer roomId,
            @RequestParam(required = false) Integer professorId,
            @RequestParam String dayOfWeek,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam(required = false) Integer excludeScheduleId) {
        return ResponseEntity.ok(adminScheduleService.checkRoomAvailability(roomId, professorId, dayOfWeek, startTime, endTime, excludeScheduleId));
    }

    @GetMapping
    public ResponseEntity<List<ScheduleResponseDto>> getSchedules(
            @RequestParam(required = false) Integer programId,
            @RequestParam(required = false) Integer studyYear) {
        return ResponseEntity.ok(adminScheduleService.getSchedules(programId, studyYear));
    }

    @PostMapping
    public ResponseEntity<?> saveSchedule(@RequestBody ScheduleRequest req) {
        try {
            ScheduleResponseDto saved = adminScheduleService.saveSchedule(req);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Integer id) {
        adminScheduleService.deleteSchedule(id);
        return ResponseEntity.ok().build();
    }
}
