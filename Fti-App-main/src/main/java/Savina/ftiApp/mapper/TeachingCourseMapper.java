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
        dto.setKrediteLeksion(c.getKrediteLeksion() != null ? c.getKrediteLeksion().doubleValue() : null);
        dto.setKrediteSeminar(c.getKrediteSeminar() != null ? c.getKrediteSeminar().doubleValue() : null);
        dto.setKrediteLaborator(c.getKrediteLaborator() != null ? c.getKrediteLaborator().doubleValue() : null);
        dto.setKrediteDetyreKursi(c.getKrediteDetyreKursi() != null ? c.getKrediteDetyreKursi().doubleValue() : null);
        dto.setKreditePraktike(c.getKreditePraktike() != null ? c.getKreditePraktike().doubleValue() : null);
        dto.setStudyYear(c.getStudyYear());
        dto.setDepartmentId(deptId);
        dto.setDepartmentName(deptName != null ? deptName : progName);
        dto.setProgramId(progId);
        dto.setProgramName(progName);
        dto.setProgramNivel(progNivel);
        boolean isMaster = (c.getProgram() != null && c.getProgram().getNivel() != null && c.getProgram().getNivel().toLowerCase().contains("master"));
        int defaultWeeks = isMaster ? 12 : 14;
        dto.setSemester(c.getSemester() != null ? c.getSemester() : "1");
        dto.setDurationWeeks(c.getDurationWeeks() != null ? c.getDurationWeeks() : defaultWeeks);
        dto.setWeeklyHours(2);
        dto.setWeeklyLectureHours(2);
        dto.setWeeklySeminarHours(2);
        dto.setWeeklyLabHours(1);
        dto.setWeeklyCourseWorkHours(1);
        dto.setWeeklyPracticeHours(2);
        dto.setTotalHours(2 * defaultWeeks);
        dto.setLectureHours(2 * defaultWeeks);
        dto.setSeminarHours(2 * defaultWeeks);
        dto.setLabHours(1 * defaultWeeks);
        dto.setCourseWorkHours(1 * defaultWeeks);
        dto.setPracticeHours(2 * defaultWeeks);
        dto.setLectureProfessor(null);
        dto.setSeminarProfessors(List.of());
        dto.setLabProfessors(List.of());
        dto.setCourseWorkProfessors(List.of());
        dto.setPracticeProfessors(List.of());
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
        List<TeachingAllocationDto.ProfessorAssignment> courseWorks = new ArrayList<>();
        List<TeachingAllocationDto.ProfessorAssignment> practices = new ArrayList<>();
        Integer lectureHours = null;
        Integer seminarHours = null;
        Integer labHours = null;
        Integer courseWorkHours = null;
        Integer practiceHours = null;

        Integer weeklyLectureHours = null;
        Integer weeklySeminarHours = null;
        Integer weeklyLabHours = null;
        Integer weeklyCourseWorkHours = null;
        Integer weeklyPracticeHours = null;

        for (TeachingCourse tc : tcs) {
            String role = tc.getRoleType() != null ? tc.getRoleType().toUpperCase() : "LEKSION";
            if ("LEKSION".equals(role)) {
                if (tc.getTotalHours() != null && lectureHours == null) lectureHours = tc.getTotalHours();
                if (tc.getWeeklyHours() != null && weeklyLectureHours == null) weeklyLectureHours = tc.getWeeklyHours();
            } else if ("SEMINAR".equals(role)) {
                if (tc.getTotalHours() != null && seminarHours == null) seminarHours = tc.getTotalHours();
                if (tc.getWeeklyHours() != null && weeklySeminarHours == null) weeklySeminarHours = tc.getWeeklyHours();
            } else if ("LABORATOR".equals(role) || "LAB".equals(role)) {
                if (tc.getTotalHours() != null && labHours == null) labHours = tc.getTotalHours();
                if (tc.getWeeklyHours() != null && weeklyLabHours == null) weeklyLabHours = tc.getWeeklyHours();
            } else if ("DETYRE_KURSI".equals(role) || "DETYRE".equals(role) || "DETYRA".equals(role)) {
                if (tc.getTotalHours() != null && courseWorkHours == null) courseWorkHours = tc.getTotalHours();
                if (tc.getWeeklyHours() != null && weeklyCourseWorkHours == null) weeklyCourseWorkHours = tc.getWeeklyHours();
            } else if ("PRAKTIKE".equals(role) || "PRAKTIK".equals(role)) {
                if (tc.getTotalHours() != null && practiceHours == null) practiceHours = tc.getTotalHours();
                if (tc.getWeeklyHours() != null && weeklyPracticeHours == null) weeklyPracticeHours = tc.getWeeklyHours();
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

            if ("LEKSION".equals(role)) {
                if (lecture == null) lecture = assign;
            } else if ("SEMINAR".equals(role)) {
                seminars.add(assign);
            } else if ("LABORATOR".equals(role) || "LAB".equals(role)) {
                labs.add(assign);
            } else if ("DETYRE_KURSI".equals(role) || "DETYRE".equals(role) || "DETYRA".equals(role)) {
                courseWorks.add(assign);
            } else if ("PRAKTIKE".equals(role) || "PRAKTIK".equals(role)) {
                practices.add(assign);
            } else {
                if (lecture == null) lecture = assign;
                else seminars.add(assign);
            }
        }

        TeachingAllocationDto dto = new TeachingAllocationDto();
        dto.setCourseId(c.getCourseId());
        dto.setCourseEmri(c.getEmriCourse());
        dto.setCourseKredite(c.getKredite());
        dto.setKrediteLeksion(c.getKrediteLeksion() != null ? c.getKrediteLeksion().doubleValue() : null);
        dto.setKrediteSeminar(c.getKrediteSeminar() != null ? c.getKrediteSeminar().doubleValue() : null);
        dto.setKrediteLaborator(c.getKrediteLaborator() != null ? c.getKrediteLaborator().doubleValue() : null);
        dto.setKrediteDetyreKursi(c.getKrediteDetyreKursi() != null ? c.getKrediteDetyreKursi().doubleValue() : null);
        dto.setKreditePraktike(c.getKreditePraktike() != null ? c.getKreditePraktike().doubleValue() : null);
        dto.setStudyYear(c.getStudyYear());
        dto.setDepartmentId(deptId);
        dto.setDepartmentName(deptName);
        dto.setProgramId(progId);
        dto.setProgramName(progName);
        boolean isMaster = (c.getProgram() != null && c.getProgram().getNivel() != null && c.getProgram().getNivel().toLowerCase().contains("master"));
        int defaultWeeks = isMaster ? 12 : 14;
        dto.setSemester(c.getSemester() != null ? c.getSemester() : "1");
        dto.setDurationWeeks(c.getDurationWeeks() != null ? c.getDurationWeeks() : defaultWeeks);
        
        dto.setWeeklyHours(first.getWeeklyHours() != null ? first.getWeeklyHours() : 2);
        dto.setWeeklyLectureHours(weeklyLectureHours != null ? weeklyLectureHours : 2);
        dto.setWeeklySeminarHours(weeklySeminarHours != null ? weeklySeminarHours : 2);
        dto.setWeeklyLabHours(weeklyLabHours != null ? weeklyLabHours : 1);
        dto.setWeeklyCourseWorkHours(weeklyCourseWorkHours != null ? weeklyCourseWorkHours : 1);
        dto.setWeeklyPracticeHours(weeklyPracticeHours != null ? weeklyPracticeHours : 2);

        dto.setTotalHours(first.getTotalHours() != null ? first.getTotalHours() : (dto.getWeeklyHours() * defaultWeeks));
        dto.setLectureHours(lectureHours != null ? lectureHours : (dto.getWeeklyLectureHours() * defaultWeeks));
        dto.setSeminarHours(seminarHours != null ? seminarHours : (dto.getWeeklySeminarHours() * defaultWeeks));
        dto.setLabHours(labHours != null ? labHours : (dto.getWeeklyLabHours() * defaultWeeks));
        dto.setCourseWorkHours(courseWorkHours != null ? courseWorkHours : (dto.getWeeklyCourseWorkHours() * defaultWeeks));
        dto.setPracticeHours(practiceHours != null ? practiceHours : (dto.getWeeklyPracticeHours() * defaultWeeks));
        dto.setLectureProfessor(lecture);
        dto.setSeminarProfessors(seminars);
        dto.setLabProfessors(labs);
        dto.setCourseWorkProfessors(courseWorks);
        dto.setPracticeProfessors(practices);
        return dto;
    }

    public String formatProfName(Professor p) {
        if (p == null || p.getUser() == null) return "Pedagog";
        String emri = p.getUser().getEmri() != null ? p.getUser().getEmri() : "";
        String mbiemri = p.getUser().getMbiemri() != null ? p.getUser().getMbiemri() : "";
        return (emri + " " + mbiemri).trim();
    }
}
