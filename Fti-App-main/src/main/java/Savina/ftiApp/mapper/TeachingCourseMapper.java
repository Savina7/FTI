package Savina.ftiApp.mapper;

import Savina.ftiApp.dto.responseDTO.TeachingAllocationDto;
import Savina.ftiApp.entity.Classes;
import Savina.ftiApp.entity.Course;
import Savina.ftiApp.entity.Professor;
import Savina.ftiApp.entity.TeachingCourse;
import Savina.ftiApp.service.AcademicYearService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TeachingCourseMapper {

    private final AcademicYearService academicYearService;

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
        int durationWeeks = c.getDurationWeeks() != null ? c.getDurationWeeks() : defaultWeeks;
        dto.setSemester(c.getSemester() != null ? c.getSemester() : "1");
        dto.setAcademicYear(academicYearService.getCurrentAcademicYear());
        dto.setDurationWeeks(durationWeeks);

        double kL = c.getKrediteLeksion() != null ? c.getKrediteLeksion().doubleValue() : (c.getKredite() != null && c.getKredite() >= 6 ? 3.0 : 2.0);
        double kS = c.getKrediteSeminar() != null ? c.getKrediteSeminar().doubleValue() : 1.5;
        double kLb = c.getKrediteLaborator() != null ? c.getKrediteLaborator().doubleValue() : 1.0;
        double kDk = c.getKrediteDetyreKursi() != null ? c.getKrediteDetyreKursi().doubleValue() : 0.5;
        double kPr = c.getKreditePraktike() != null ? c.getKreditePraktike().doubleValue() : 0.0;

        // Bachelor: 1 cr Leksion = 12h, Seminar = 14h, Lab = 20h, DK = 5h
        // Master:   1 cr Leksion = 10h, Seminar = 12h, Lab = 20h, DK = 5h
        double factorLec = isMaster ? 10.0 : 12.0;
        double factorSem = isMaster ? 12.0 : 14.0;
        double factorLab = 20.0;
        double factorCw = 5.0;

        double totalLec = (c.getKrediteLeksion() != null && c.getKrediteLeksion().doubleValue() == 0.0) ? 0.0 : kL * factorLec;
        double totalSem = (c.getKrediteSeminar() != null && c.getKrediteSeminar().doubleValue() == 0.0) ? 0.0 : kS * factorSem;
        double totalLab = (c.getKrediteLaborator() != null && c.getKrediteLaborator().doubleValue() == 0.0) ? 0.0 : kLb * factorLab;
        double totalCw = (c.getKrediteDetyreKursi() != null && c.getKrediteDetyreKursi().doubleValue() == 0.0) ? 0.0 : kDk * factorCw;
        double totalPr = (c.getKreditePraktike() != null && c.getKreditePraktike().doubleValue() == 0.0) ? 0.0 : kPr * 20.0;

        dto.setWeeklyHours(2.0);
        dto.setWeeklyLectureHours(2.0);
        dto.setWeeklySeminarHours(2.0);
        dto.setWeeklyLabHours(1.0);
        dto.setWeeklyCourseWorkHours(1.0);
        dto.setWeeklyPracticeHours(2.0);

        dto.setTotalHours(totalLec);
        dto.setLectureHours(totalLec);
        dto.setSeminarHours(totalSem);
        dto.setLabHours(totalLab);
        dto.setCourseWorkHours(totalCw);
        dto.setPracticeHours(totalPr);
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
        Double lectureHours = null;
        Double seminarHours = null;
        Double labHours = null;
        Double courseWorkHours = null;
        Double practiceHours = null;

        Double weeklyLectureHours = null;
        Double weeklySeminarHours = null;
        Double weeklyLabHours = null;
        Double weeklyCourseWorkHours = null;
        Double weeklyPracticeHours = null;

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
        int durationWeeks = c.getDurationWeeks() != null ? c.getDurationWeeks() : defaultWeeks;
        dto.setSemester(c.getSemester() != null ? c.getSemester() : "1");
        dto.setAcademicYear(first.getAcademicYear() != null && !first.getAcademicYear().isBlank() ? first.getAcademicYear() : academicYearService.getCurrentAcademicYear());
        dto.setDurationWeeks(durationWeeks);
        
        double kL = c.getKrediteLeksion() != null ? c.getKrediteLeksion().doubleValue() : (c.getKredite() != null && c.getKredite() >= 6 ? 3.0 : 2.0);
        double kS = c.getKrediteSeminar() != null ? c.getKrediteSeminar().doubleValue() : 1.5;
        double kLb = c.getKrediteLaborator() != null ? c.getKrediteLaborator().doubleValue() : 1.0;
        double kDk = c.getKrediteDetyreKursi() != null ? c.getKrediteDetyreKursi().doubleValue() : 0.5;
        double kPr = c.getKreditePraktike() != null ? c.getKreditePraktike().doubleValue() : 0.0;

        double factorLec = isMaster ? 10.0 : 12.0;
        double factorSem = isMaster ? 12.0 : 14.0;
        double factorLab = 20.0;
        double factorCw = 5.0;

        double autoTotLec = (c.getKrediteLeksion() != null && c.getKrediteLeksion().doubleValue() == 0.0) ? 0.0 : kL * factorLec;
        double autoTotSem = (c.getKrediteSeminar() != null && c.getKrediteSeminar().doubleValue() == 0.0) ? 0.0 : kS * factorSem;
        double autoTotLab = (c.getKrediteLaborator() != null && c.getKrediteLaborator().doubleValue() == 0.0) ? 0.0 : kLb * factorLab;
        double autoTotCw = (c.getKrediteDetyreKursi() != null && c.getKrediteDetyreKursi().doubleValue() == 0.0) ? 0.0 : kDk * factorCw;
        double autoTotPr = (c.getKreditePraktike() != null && c.getKreditePraktike().doubleValue() == 0.0) ? 0.0 : kPr * 20.0;

        dto.setWeeklyLectureHours(weeklyLectureHours != null ? weeklyLectureHours : 2.0);
        dto.setWeeklySeminarHours(weeklySeminarHours != null ? weeklySeminarHours : 2.0);
        dto.setWeeklyLabHours(weeklyLabHours != null ? weeklyLabHours : 1.0);
        dto.setWeeklyCourseWorkHours(weeklyCourseWorkHours != null ? weeklyCourseWorkHours : 1.0);
        dto.setWeeklyPracticeHours(weeklyPracticeHours != null ? weeklyPracticeHours : 2.0);
        dto.setWeeklyHours(first.getWeeklyHours() != null ? first.getWeeklyHours() : dto.getWeeklyLectureHours());

        dto.setLectureHours(lectureHours != null ? lectureHours : autoTotLec);
        dto.setSeminarHours(seminarHours != null ? seminarHours : autoTotSem);
        dto.setLabHours(labHours != null ? labHours : autoTotLab);
        dto.setCourseWorkHours(courseWorkHours != null ? courseWorkHours : autoTotCw);
        dto.setPracticeHours(practiceHours != null ? practiceHours : autoTotPr);
        dto.setTotalHours(first.getTotalHours() != null ? first.getTotalHours() : dto.getLectureHours());
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
