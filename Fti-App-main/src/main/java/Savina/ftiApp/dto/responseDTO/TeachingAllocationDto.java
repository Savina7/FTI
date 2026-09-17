package Savina.ftiApp.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeachingAllocationDto {
    private Integer courseId;
    private String courseEmri;
    private Integer courseKredite;
    private Integer studyYear;
    private Integer departmentId;
    private String departmentName;
    private Integer programId;
    private String programName;
    private String programNivel;
    private String semester;
    private Integer durationWeeks;
    private Integer totalHours;
    private Integer lectureHours;
    private Integer seminarHours;
    private Integer labHours;
    private Integer courseWorkHours;
    private Integer practiceHours;

    private Integer weeklyHours;
    private Integer weeklyLectureHours;
    private Integer weeklySeminarHours;
    private Integer weeklyLabHours;
    private Integer weeklyCourseWorkHours;
    private Integer weeklyPracticeHours;

    private Double krediteLeksion;
    private Double krediteSeminar;
    private Double krediteLaborator;
    private Double krediteDetyreKursi;
    private Double kreditePraktike;

    private ProfessorAssignment lectureProfessor;
    private List<ProfessorAssignment> seminarProfessors;
    private List<ProfessorAssignment> labProfessors;
    private List<ProfessorAssignment> courseWorkProfessors;
    private List<ProfessorAssignment> practiceProfessors;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProfessorAssignment {
        private Integer teachingCourseId;
        private Integer professorId;
        private String professorName;
        private String classGroup;
        private List<Integer> classIds;
        private List<String> classNames;
    }
}
