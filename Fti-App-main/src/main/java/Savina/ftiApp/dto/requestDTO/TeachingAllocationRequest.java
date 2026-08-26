package Savina.ftiApp.dto.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeachingAllocationRequest {
    private Integer courseId;
    private String semester;
    private Integer durationWeeks;
    private Integer totalHours;

    private Integer lectureProfessorId;
    private List<Integer> lectureClassIds;

    private List<SeminarAssignmentReq> seminars;
    private Boolean hasLab;
    private List<LabAssignmentReq> labs;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SeminarAssignmentReq {
        private Integer professorId;
        private String classGroup;
        private List<Integer> classIds;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LabAssignmentReq {
        private Integer professorId;
        private String classGroup;
        private List<Integer> classIds;
    }
}
