package Savina.ftiApp.controller;

import Savina.ftiApp.dto.responseDTO.CourseAdminDto;
import Savina.ftiApp.dto.requestDTO.CourseRequest;
import Savina.ftiApp.dto.responseDTO.DepartmentDto;
import Savina.ftiApp.dto.responseDTO.ProgramDto;
import Savina.ftiApp.service.AdminCourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/courses")
@RequiredArgsConstructor
public class AdminCourseController {

    private final AdminCourseService adminCourseService;

    @GetMapping
    public List<CourseAdminDto> getAllCourses() {
        return adminCourseService.getAllCourses();
    }

    @GetMapping("/programs")
    public List<ProgramDto> getAllPrograms() {
        return adminCourseService.getAllPrograms();
    }

    @GetMapping("/departments")
    public List<DepartmentDto> getAllDepartments() {
        return adminCourseService.getAllDepartments();
    }

    @GetMapping("/{id}")
    public CourseAdminDto getCourseById(@PathVariable Integer id) {
        return adminCourseService.getCourseById(id);
    }

    @PostMapping
    public CourseAdminDto createCourse(@Valid @RequestBody CourseRequest req) {
        return adminCourseService.createCourse(req);
    }

    @PutMapping("/{id}")
    public CourseAdminDto updateCourse(@PathVariable Integer id, @Valid @RequestBody CourseRequest req) {
        return adminCourseService.updateCourse(id, req);
    }

    @DeleteMapping("/{id}")
    public void deleteCourse(@PathVariable Integer id) {
        adminCourseService.deleteCourse(id);
    }
}
