package Savina.ftiApp.mapper;

import Savina.ftiApp.dto.responseDTO.BranchStatsDto;
import Savina.ftiApp.dto.responseDTO.PedagogOptionsDto;
import Savina.ftiApp.dto.responseDTO.PedagogRegisterDto;
import Savina.ftiApp.entity.Classes;
import Savina.ftiApp.entity.Course;
import Savina.ftiApp.entity.Department;
import Savina.ftiApp.entity.Program;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Component
public class PedagogMapper {

    public PedagogOptionsDto.CourseOptionDto buildCourseOption(Course c) {
        if (c == null) return null;

        int cYear = c.getStudyYear() != null ? c.getStudyYear() : 1;
        Department dept = (c.getProgram() != null) ? c.getProgram().getDepartment() : null;
        if (dept == null && c.getDepartments() != null && !c.getDepartments().isEmpty()) {
            dept = c.getDepartments().iterator().next();
        }

        List<Integer> deptIds = new ArrayList<>();
        if (dept != null && dept.getDepartmentId() != null) {
            deptIds.add(dept.getDepartmentId());
        }
        if (c.getDepartments() != null) {
            for (Department d : c.getDepartments()) {
                if (d.getDepartmentId() != null && !deptIds.contains(d.getDepartmentId())) {
                    deptIds.add(d.getDepartmentId());
                }
            }
        }

        String progName = c.getProgram() != null ? c.getProgram().getSpecializimi() : null;

        PedagogOptionsDto.CourseOptionDto dto = new PedagogOptionsDto.CourseOptionDto();
        dto.setId(c.getCourseId());
        dto.setName(c.getEmriCourse());
        dto.setStudyYear(cYear);
        dto.setDepartmentId(dept != null ? dept.getDepartmentId() : null);
        dto.setDepartmentIds(deptIds);
        dto.setProgramName(progName);
        return dto;
    }

    public PedagogOptionsDto.ClassOptionDto buildClassOption(Classes cl, Integer courseId) {
        if (cl == null) return null;

        int clYear = cl.getVitStudimit() != null ? cl.getVitStudimit() : 1;
        String formattedName = formatClassName(cl, clYear);

        Integer deptId = (cl.getProgram() != null && cl.getProgram().getDepartment() != null)
                ? cl.getProgram().getDepartment().getDepartmentId()
                : null;

        Integer progId = cl.getProgram() != null ? cl.getProgram().getProgramId() : null;

        List<Integer> courseIds = new ArrayList<>();
        if (courseId != null) {
            courseIds.add(courseId);
        }

        PedagogOptionsDto.ClassOptionDto dto = new PedagogOptionsDto.ClassOptionDto();
        dto.setId(cl.getClassId());
        dto.setName(formattedName);
        dto.setStudyYear(clYear);
        dto.setCourseId(courseId);
        dto.setCourseIds(courseIds);
        dto.setDepartmentId(deptId);
        dto.setProgramId(progId);
        return dto;
    }

    public PedagogOptionsDto.ProgramOptionDto buildProgramOption(Program p) {
        if (p == null) return null;
        String pName = p.getSpecializimi() != null ? p.getSpecializimi() : ("Programi " + p.getProgramId());
        Integer dId = (p.getDepartment() != null) ? p.getDepartment().getDepartmentId() : null;
        return new PedagogOptionsDto.ProgramOptionDto(p.getProgramId(), pName, dId);
    }

    public PedagogOptionsDto.DepartmentOptionDto buildDepartmentOption(Department d) {
        if (d == null) return null;
        return new PedagogOptionsDto.DepartmentOptionDto(d.getDepartmentId(), d.getEmerDepartamenti());
    }

    public String formatClassName(Classes cl, Integer studyYear) {
        if (cl == null || cl.getEmriClass() == null || cl.getEmriClass().isBlank()) return "-";

        String className = cl.getEmriClass().trim();
        if (className.toLowerCase().startsWith("inxhinieri")) {
            return className;
        }

        String prog = (cl.getProgram() != null && cl.getProgram().getSpecializimi() != null)
                ? cl.getProgram().getSpecializimi()
                : "";

        if (!prog.isBlank()) {
            return (prog + " - " + className).trim();
        }

        return className;
    }

    public PedagogRegisterDto.RegisterStatsDto calculateStats(List<BigDecimal> gradeList, int totalStudents) {
        if (gradeList == null || gradeList.isEmpty() || totalStudents == 0) {
            PedagogRegisterDto.RegisterStatsDto dto = new PedagogRegisterDto.RegisterStatsDto();
            dto.setMesatarja(0.0);
            dto.setKalueshmeria(0.0);
            dto.setModa("0");
            dto.setNotaLarteUlet("- / -");
            dto.setPjesemarrja(0.0);
            dto.setDistribution(Collections.emptyList());
            return dto;
        }

        double sum = 0;
        int passing = 0;
        int max = 0;
        int min = 10;
        Map<Integer, Long> countByIntGrade = new HashMap<>();

        for (BigDecimal g : gradeList) {
            int intVal = g.intValue();
            sum += g.doubleValue();
            if (intVal >= 5) passing++;
            if (intVal > max) max = intVal;
            if (intVal < min) min = intVal;
            countByIntGrade.put(intVal, countByIntGrade.getOrDefault(intVal, 0L) + 1);
        }

        double mesatarja = BigDecimal.valueOf(sum / gradeList.size()).setScale(2, RoundingMode.HALF_UP).doubleValue();
        double kalueshmeria = BigDecimal.valueOf(((double) passing / gradeList.size()) * 100).setScale(1, RoundingMode.HALF_UP).doubleValue();
        double pjesemarrja = BigDecimal.valueOf(((double) gradeList.size() / totalStudents) * 100).setScale(1, RoundingMode.HALF_UP).doubleValue();

        int modeGrade = gradeList.get(0).intValue();
        long maxFreq = 0;
        for (Map.Entry<Integer, Long> e : countByIntGrade.entrySet()) {
            if (e.getValue() > maxFreq) {
                maxFreq = e.getValue();
                modeGrade = e.getKey();
            }
        }

        List<PedagogRegisterDto.GradeDistributionDto> dist = new ArrayList<>();
        int[] sortedGrades = new int[]{10, 9, 8, 7, 6, 5, 4};
        for (int note : sortedGrades) {
            long c = countByIntGrade.getOrDefault(note, 0L);
            if (c > 0) {
                double pct = BigDecimal.valueOf(((double) c / gradeList.size()) * 100).setScale(1, RoundingMode.HALF_UP).doubleValue();
                dist.add(new PedagogRegisterDto.GradeDistributionDto(note, c, pct));
            }
        }

        PedagogRegisterDto.RegisterStatsDto dto = new PedagogRegisterDto.RegisterStatsDto();
        dto.setMesatarja(mesatarja);
        dto.setKalueshmeria(kalueshmeria);
        dto.setModa(String.valueOf(modeGrade));
        dto.setNotaLarteUlet(max + " / " + min);
        dto.setPjesemarrja(pjesemarrja);
        dto.setDistribution(dist);
        return dto;
    }

    public BranchStatsDto buildEmptyBranchStats(String courseName, String deptName, String dega, String academicYear,
                                                int totalStudents, List<BranchStatsDto.StudentExportItem> realStudents,
                                                Collection<String> groupNames) {
        List<BranchStatsDto.GradeDistributionItem> dist = new ArrayList<>();
        for (int note : new int[]{10, 9, 8, 7, 6, 5, 4}) {
            dist.add(new BranchStatsDto.GradeDistributionItem(note, 0L, 0.0));
        }

        List<BranchStatsDto.GroupComparisonItem> groups = new ArrayList<>();
        for (String grp : groupNames) {
            groups.add(new BranchStatsDto.GroupComparisonItem(grp, 0, 0.0, 0.0, 0.0));
        }

        BranchStatsDto dto = new BranchStatsDto();
        dto.setCourseName(courseName);
        dto.setDepartmentName(deptName);
        dto.setBranchName(dega);
        dto.setAcademicYear(academicYear);
        dto.setNumriStudenteve(totalStudents);
        dto.setMesatarja(0.0);
        dto.setKalueshmeria(0.0);
        dto.setModa("-");
        dto.setNotaLarteUlet("- / -");
        dto.setPjesemarrja(0.0);
        dto.setGradeDistribution(dist);
        dto.setGroups(groups);
        dto.setStudents(realStudents);
        return dto;
    }
}
