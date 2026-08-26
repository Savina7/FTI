package Savina.ftiApp.controller;

import Savina.ftiApp.dto.responseDTO.TeachingAllocationDto;
import Savina.ftiApp.dto.requestDTO.TeachingAllocationRequest;
import Savina.ftiApp.service.AdminTeachingCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/teaching-courses")
@RequiredArgsConstructor
public class AdminTeachingCourseController {

    private final AdminTeachingCourseService adminTeachingCourseService;

    @GetMapping
    public ResponseEntity<List<TeachingAllocationDto>> getAllAllocations() {
        return ResponseEntity.ok(adminTeachingCourseService.getAllAllocations());
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<TeachingAllocationDto> getAllocationByCourseId(@PathVariable Integer courseId) {
        return ResponseEntity.ok(adminTeachingCourseService.getAllocationByCourseId(courseId));
    }

    @PostMapping
    public ResponseEntity<TeachingAllocationDto> saveAllocation(@RequestBody TeachingAllocationRequest req) {
        TeachingAllocationDto result = adminTeachingCourseService.saveAllocation(req);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> deleteAllocation(@PathVariable Integer courseId) {
        adminTeachingCourseService.deleteAllocationByCourseId(courseId);
        return ResponseEntity.ok().build();
    }
}
