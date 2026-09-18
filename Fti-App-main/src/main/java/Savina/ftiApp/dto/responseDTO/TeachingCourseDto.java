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
public class TeachingCourseDto {
    private Integer teachingCourseId;
    private Integer courseId;
    private String courseEmri;
    private Integer courseKredite;
    private Integer studyYear;
    private String programName;
    private Integer professorId;
    private String professorName;
    private String professorDepartment;
    private String semester;
    private Integer durationWeeks;
    private Double weeklyHours;
    private Double totalHours;
    private List<String> classNames;
}
