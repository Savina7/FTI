package Savina.ftiApp.service;

import Savina.ftiApp.dto.responseDTO.CourseAdminDto;
import Savina.ftiApp.dto.requestDTO.CourseRequest;
import Savina.ftiApp.dto.responseDTO.DepartmentDto;
import Savina.ftiApp.dto.responseDTO.ProgramDto;
import Savina.ftiApp.entity.Course;
import Savina.ftiApp.entity.Program;
import Savina.ftiApp.mapper.CourseMapper;
import Savina.ftiApp.repository.CourseRepository;
import Savina.ftiApp.repository.DepartmentRepository;
import Savina.ftiApp.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCourseService {

    private final CourseRepository courseRepository;
    private final ProgramRepository programRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseMapper courseMapper;

    @Transactional(readOnly = true)
    public List<CourseAdminDto> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(courseMapper::toCourseAdminDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseAdminDto getCourseById(Integer id) {
        Course c = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lenda nuk u gjet me ID: " + id));
        return courseMapper.toCourseAdminDto(c);
    }

    @Transactional
    public CourseAdminDto createCourse(CourseRequest req) {
        Program program = programRepository.findById(req.getProgramId())
                .orElseThrow(() -> new RuntimeException("Programi nuk u gjet me ID: " + req.getProgramId()));

        Course course = courseMapper.toEntity(req, program);
        Course saved = courseRepository.save(course);
        return courseMapper.toCourseAdminDto(saved);
    }

    @Transactional
    public CourseAdminDto updateCourse(Integer id, CourseRequest req) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lenda nuk u gjet me ID: " + id));

        Program program = programRepository.findById(req.getProgramId())
                .orElseThrow(() -> new RuntimeException("Programi nuk u gjet me ID: " + req.getProgramId()));

        course.setEmriCourse(req.getEmriCourse());
        course.setProgram(program);
        course.setKredite(req.getKredite());
        course.setStatus(req.getStatus() != null ? req.getStatus() : "Active");
        course.setStudyYear(req.getStudyYear());

        Course updated = courseRepository.save(course);
        return courseMapper.toCourseAdminDto(updated);
    }

    @Transactional
    public void deleteCourse(Integer id) {
        if (!courseRepository.existsById(id)) {
            throw new RuntimeException("Lenda nuk u gjet me ID: " + id);
        }
        courseRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ProgramDto> getAllPrograms() {
        return programRepository.findAll().stream()
                .map(courseMapper::toProgramDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DepartmentDto> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(courseMapper::toDepartmentDto)
                .collect(Collectors.toList());
    }
}
