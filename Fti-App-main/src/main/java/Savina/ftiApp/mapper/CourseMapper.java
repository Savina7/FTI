package Savina.ftiApp.mapper;

import Savina.ftiApp.dto.requestDTO.CourseRequest;
import Savina.ftiApp.dto.responseDTO.CourseAdminDto;
import Savina.ftiApp.dto.responseDTO.DepartmentDto;
import Savina.ftiApp.dto.responseDTO.ProgramDto;
import Savina.ftiApp.entity.Course;
import Savina.ftiApp.entity.Department;
import Savina.ftiApp.entity.Program;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {

    public CourseAdminDto toCourseAdminDto(Course c) {
        if (c == null) return null;

        CourseAdminDto dto = new CourseAdminDto();
        dto.setCourseId(c.getCourseId());
        dto.setEmriCourse(c.getEmriCourse());
        dto.setKredite(c.getKredite());
        dto.setStatus(c.getStatus() != null ? c.getStatus() : "Active");
        dto.setStudyYear(c.getStudyYear());

        if (c.getProgram() != null) {
            dto.setProgramId(c.getProgram().getProgramId());
            dto.setProgramEmri(c.getProgram().getSpecializimi());
            dto.setProgramNivel(c.getProgram().getNivel());
            if (c.getProgram().getDepartment() != null) {
                dto.setDepartmentId(c.getProgram().getDepartment().getDepartmentId());
                dto.setDepartmentName(c.getProgram().getDepartment().getEmerDepartamenti());
            } else {
                dto.setDepartmentName(c.getProgram().getSpecializimi());
            }
        }

        return dto;
    }

    public Course toEntity(CourseRequest req, Program program) {
        if (req == null) return null;

        Course course = new Course();
        course.setEmriCourse(req.getEmriCourse());
        course.setProgram(program);
        course.setKredite(req.getKredite());
        course.setStatus(req.getStatus() != null ? req.getStatus() : "Active");
        course.setStudyYear(req.getStudyYear());
        return course;
    }

    public ProgramDto toProgramDto(Program p) {
        if (p == null) return null;

        ProgramDto dto = new ProgramDto();
        dto.setProgramId(p.getProgramId());
        dto.setEmri(p.getSpecializimi());
        dto.setNivel(p.getNivel());
        if (p.getDepartment() != null) {
            dto.setDepartmentId(p.getDepartment().getDepartmentId());
        }
        return dto;
    }

    public DepartmentDto toDepartmentDto(Department d) {
        if (d == null) return null;

        DepartmentDto dto = new DepartmentDto();
        dto.setDepartmentId(d.getDepartmentId());
        dto.setEmerDepartamenti(d.getEmerDepartamenti());
        return dto;
    }
}
