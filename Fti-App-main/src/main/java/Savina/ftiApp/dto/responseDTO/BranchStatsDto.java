package Savina.ftiApp.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchStatsDto {
    private String courseName;
    private String departmentName;
    private String branchName;
    private String academicYear;

    private Integer numriStudenteve;
    private Double mesatarja;
    private Double kalueshmeria;
    private String moda;
    private String notaLarteUlet;
    private Double pjesemarrja;

    private List<GradeDistributionItem> gradeDistribution;
    private List<GroupComparisonItem> groups;
    private List<StudentExportItem> students;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GradeDistributionItem {
        private Integer nota;
        private Long count;
        private Double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupComparisonItem {
        private String groupName;
        private Integer studentCount;
        private Double kalueshmeria;
        private Double pjesemarrja;
        private Double mesatarja;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentExportItem {
        private String emri;
        private String matrikulli;
        private String grupi;
        private Integer nota;
        private String statusi;
    }
}
