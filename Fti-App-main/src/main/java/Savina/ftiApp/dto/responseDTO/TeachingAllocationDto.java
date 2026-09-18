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
