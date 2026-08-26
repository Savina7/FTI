package Savina.ftiApp.controller;

import Savina.ftiApp.dto.responseDTO.StudentAttendanceDto;
import Savina.ftiApp.dto.responseDTO.StudentGradeDto;
import Savina.ftiApp.dto.responseDTO.StudentProfileDto;
import Savina.ftiApp.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping("/me")
    public ResponseEntity<StudentProfileDto> getStudentProfile(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String email) {
        StudentProfileDto profile = studentService.getStudentProfile(userId, email);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/grades")
    public ResponseEntity<List<StudentGradeDto>> getStudentGrades(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String email) {
        List<StudentGradeDto> grades = studentService.getStudentGrades(userId, email);
        return ResponseEntity.ok(grades);
    }

    @GetMapping("/mungesat")
    public ResponseEntity<List<StudentAttendanceDto>> getStudentAttendances(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String email) {
        List<StudentAttendanceDto> attendances = studentService.getStudentAttendances(userId, email);
        return ResponseEntity.ok(attendances);
    }
}
