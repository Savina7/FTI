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
    private Double totalHours;
    private Double lectureHours;
    private Double seminarHours;
    private Double labHours;
    private Double courseWorkHours;
    private Double practiceHours;

    private Double weeklyHours;
    private Double weeklyLectureHours;
    private Double weeklySeminarHours;
    private Double weeklyLabHours;
    private Double weeklyCourseWorkHours;
    private Double weeklyPracticeHours;

    private Integer lectureProfessorId;
    private List<Integer> lectureClassIds;

    private List<SeminarAssignmentReq> seminars;
    private Boolean hasLab;
    private List<LabAssignmentReq> labs;
    private Boolean hasCourseWork;
    private List<CourseWorkAssignmentReq> courseWorks;
    private Boolean hasPractice;
    private List<PracticeAssignmentReq> practices;

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

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CourseWorkAssignmentReq {
        private Integer professorId;
        private String classGroup;
        private List<Integer> classIds;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PracticeAssignmentReq {
        private Integer professorId;
        private String classGroup;
        private List<Integer> classIds;
    }
}
