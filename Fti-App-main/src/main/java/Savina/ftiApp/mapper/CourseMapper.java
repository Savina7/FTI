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
        dto.setKrediteLeksion(c.getKrediteLeksion() != null ? c.getKrediteLeksion().doubleValue() : 0.0);
        dto.setKrediteSeminar(c.getKrediteSeminar() != null ? c.getKrediteSeminar().doubleValue() : 0.0);
        dto.setKrediteLaborator(c.getKrediteLaborator() != null ? c.getKrediteLaborator().doubleValue() : 0.0);
        dto.setKrediteDetyreKursi(c.getKrediteDetyreKursi() != null ? c.getKrediteDetyreKursi().doubleValue() : 0.0);
        dto.setKreditePraktike(c.getKreditePraktike() != null ? c.getKreditePraktike().doubleValue() : 0.0);
        dto.setStatus(c.getStatus() != null ? c.getStatus() : "Active");
        dto.setStudyYear(c.getStudyYear());
        dto.setSemester(c.getSemester() != null ? c.getSemester() : "1");

        boolean isMaster = (c.getProgram() != null && c.getProgram().getNivel() != null && c.getProgram().getNivel().toLowerCase().contains("master"));
        int defaultWeeks = isMaster ? 12 : 14;
        dto.setDurationWeeks(c.getDurationWeeks() != null ? c.getDurationWeeks() : defaultWeeks);

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

        boolean isMaster = (program != null && program.getNivel() != null && program.getNivel().toLowerCase().contains("master"));
        int defaultWeeks = isMaster ? 12 : 14;

        Course course = new Course();
        course.setEmriCourse(req.getEmriCourse());
        course.setProgram(program);
        course.setKredite(req.getKredite());
        course.setKrediteLeksion(req.getKrediteLeksion() != null ? java.math.BigDecimal.valueOf(req.getKrediteLeksion()) : java.math.BigDecimal.ZERO);
        course.setKrediteSeminar(req.getKrediteSeminar() != null ? java.math.BigDecimal.valueOf(req.getKrediteSeminar()) : java.math.BigDecimal.ZERO);
        course.setKrediteLaborator(req.getKrediteLaborator() != null ? java.math.BigDecimal.valueOf(req.getKrediteLaborator()) : java.math.BigDecimal.ZERO);
        course.setKrediteDetyreKursi(req.getKrediteDetyreKursi() != null ? java.math.BigDecimal.valueOf(req.getKrediteDetyreKursi()) : java.math.BigDecimal.ZERO);
        course.setKreditePraktike(req.getKreditePraktike() != null ? java.math.BigDecimal.valueOf(req.getKreditePraktike()) : java.math.BigDecimal.ZERO);
        course.setStatus(req.getStatus() != null ? req.getStatus() : "Active");
        course.setStudyYear(req.getStudyYear());
        course.setSemester(req.getSemester() != null ? req.getSemester() : "1");
        course.setDurationWeeks(req.getDurationWeeks() != null ? req.getDurationWeeks() : defaultWeeks);
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
