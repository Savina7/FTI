package Savina.ftiApp.mapper;

import Savina.ftiApp.dto.responseDTO.TeachingAllocationDto;
import Savina.ftiApp.entity.Classes;
import Savina.ftiApp.entity.Course;
import Savina.ftiApp.entity.Professor;
import Savina.ftiApp.entity.TeachingCourse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TeachingCourseMapper {

    public TeachingAllocationDto mapCourseToEmptyDto(Course c) {
        if (c == null) return null;

        Integer deptId = null;
        String deptName = null;
        Integer progId = null;
        String progName = null;
        String progNivel = null;
        if (c.getProgram() != null) {
            progId = c.getProgram().getProgramId();
            progName = c.getProgram().getSpecializimi();
            progNivel = c.getProgram().getNivel();
            if (c.getProgram().getDepartment() != null) {
                deptId = c.getProgram().getDepartment().getDepartmentId();
                deptName = c.getProgram().getDepartment().getEmerDepartamenti();
            }
        }

        TeachingAllocationDto dto = new TeachingAllocationDto();
        dto.setCourseId(c.getCourseId());
        dto.setCourseEmri(c.getEmriCourse());
        dto.setCourseKredite(c.getKredite());
        dto.setStudyYear(c.getStudyYear());
        dto.setDepartmentId(deptId);
        dto.setDepartmentName(deptName != null ? deptName : progName);
        dto.setProgramId(progId);
        dto.setProgramName(progName);
        dto.setProgramNivel(progNivel);
        dto.setSemester("1");
        dto.setDurationWeeks(18);
        dto.setTotalHours(30);
        dto.setLectureHours(30);
        dto.setSeminarHours(30);
        dto.setLabHours(15);
        dto.setLectureProfessor(null);
        dto.setSeminarProfessors(List.of());
        dto.setLabProfessors(List.of());
        return dto;
    }

    public TeachingAllocationDto mapGroupToDto(Integer courseId, List<TeachingCourse> tcs) {
        if (tcs == null || tcs.isEmpty()) return null;

        TeachingCourse first = tcs.get(0);
        Course c = first.getCourse();
        if (c == null) return null;

        Integer deptId = null;
        String deptName = null;
        Integer progId = null;
        String progName = null;
        String progNivel = null;

        if (c.getProgram() != null) {
            progId = c.getProgram().getProgramId();
            progName = c.getProgram().getSpecializimi();
            progNivel = c.getProgram().getNivel();
            if (c.getProgram().getDepartment() != null) {
                deptId = c.getProgram().getDepartment().getDepartmentId();
                deptName = c.getProgram().getDepartment().getEmerDepartamenti();
            } else {
                deptName = c.getProgram().getSpecializimi();
            }
        }

        TeachingAllocationDto.ProfessorAssignment lecture = null;
        List<TeachingAllocationDto.ProfessorAssignment> seminars = new ArrayList<>();
        List<TeachingAllocationDto.ProfessorAssignment> labs = new ArrayList<>();
        Integer lectureHours = null;
        Integer seminarHours = null;
        Integer labHours = null;

        for (TeachingCourse tc : tcs) {
            String role = tc.getRoleType() != null ? tc.getRoleType().toUpperCase() : "LEKSION";
            if ("LEKSION".equals(role)) {
                if (tc.getTotalHours() != null && lectureHours == null) lectureHours = tc.getTotalHours();
            } else if ("SEMINAR".equals(role)) {
                if (tc.getTotalHours() != null && seminarHours == null) seminarHours = tc.getTotalHours();
            } else if ("LABORATOR".equals(role)) {
                if (tc.getTotalHours() != null && labHours == null) labHours = tc.getTotalHours();
            }

            if (tc.getProfessor() == null) continue;

            Professor p = tc.getProfessor();
            String profName = formatProfName(p);

            List<String> cNames = tc.getClasses() != null
                    ? tc.getClasses().stream()
                        .map(Classes::getEmriClass)
                        .filter(java.util.Objects::nonNull)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .distinct()
                        .collect(Collectors.toList())
                    : List.of();

            List<Integer> cIds = tc.getClasses() != null
                    ? tc.getClasses().stream().map(Classes::getClassId).filter(java.util.Objects::nonNull).distinct().collect(Collectors.toList())
                    : List.of();

            String groupText = cNames.isEmpty() ? "Te gjitha klasat" : String.join(", ", cNames);

            TeachingAllocationDto.ProfessorAssignment assign = new TeachingAllocationDto.ProfessorAssignment();
            assign.setTeachingCourseId(tc.getTeachingCourseId());
            assign.setProfessorId(p.getProfessorId());
            assign.setProfessorName(profName);
            assign.setClassGroup(groupText);
            assign.setClassIds(cIds);
            assign.setClassNames(cNames);

            if ("LEKSION".equals(role) && lecture == null) {
                lecture = assign;
            } else if ("SEMINAR".equals(role)) {
                seminars.add(assign);
            } else if ("LABORATOR".equals(role)) {
                labs.add(assign);
            } else {
                if (lecture == null) lecture = assign;
                else seminars.add(assign);
            }
        }

        TeachingAllocationDto dto = new TeachingAllocationDto();
        dto.setCourseId(c.getCourseId());
        dto.setCourseEmri(c.getEmriCourse());
        dto.setCourseKredite(c.getKredite());
        dto.setStudyYear(c.getStudyYear());
        dto.setDepartmentId(deptId);
        dto.setDepartmentName(deptName);
        dto.setProgramId(progId);
        dto.setProgramName(progName);
        dto.setProgramNivel(progNivel);
        dto.setSemester(first.getSemester());
        dto.setDurationWeeks(first.getDurationWeeks() != null ? first.getDurationWeeks() : 18);
        dto.setTotalHours(first.getTotalHours() != null ? first.getTotalHours() : 30);
        dto.setLectureHours(lectureHours != null ? lectureHours : (first.getTotalHours() != null ? first.getTotalHours() : 30));
        dto.setSeminarHours(seminarHours != null ? seminarHours : (first.getTotalHours() != null ? first.getTotalHours() : 30));
        dto.setLabHours(labHours != null ? labHours : 15);
        dto.setLectureProfessor(lecture);
        dto.setSeminarProfessors(seminars);
        dto.setLabProfessors(labs);
        return dto;
    }

    public String formatProfName(Professor p) {
        if (p == null || p.getUser() == null) return "Pedagog";
        String emri = p.getUser().getEmri() != null ? p.getUser().getEmri() : "";
        String mbiemri = p.getUser().getMbiemri() != null ? p.getUser().getMbiemri() : "";
        return (emri + " " + mbiemri).trim();
    }
}
