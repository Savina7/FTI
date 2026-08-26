package Savina.ftiApp.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseAdminDto {
    private Integer courseId;
    private String emriCourse;
    private Integer kredite;
    private String status;
    private Integer studyYear;
    private Integer programId;
    private String programEmri;
    private String programNivel;
    private Integer departmentId;
    private String departmentName;
}
