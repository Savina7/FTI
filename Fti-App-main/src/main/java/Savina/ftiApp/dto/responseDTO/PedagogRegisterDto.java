package Savina.ftiApp.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedagogRegisterDto {
    private Integer courseId;
    private String courseName;
    private Integer classId;
    private String className;
    private String departmentName;
    private String academicYear;
    private String roleType;
    private List<StudentRowDto> students;
    private List<AttendanceColumnDto> attendanceColumns;
    private RegisterStatsDto stats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentRowDto {
        private Integer studentId;
        private String emri;
        private String nrMatrikulimit;
        private BigDecimal grade;
        private String status;
        private boolean isPermiresim;
        private Map<String, Boolean> attendance;
        private String examAttendanceStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendanceColumnDto {
        private String key;
        private String title;
        private String date;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterStatsDto {
        private Double mesatarja;
        private Double kalueshmeria;
        private String moda;
        private String notaLarteUlet;
        private Double pjesemarrja;
        private List<GradeDistributionDto> distribution;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GradeDistributionDto {
        private Integer nota;
        private Long studentCount;
        private Double perqindja;
    }
}
