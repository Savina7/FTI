package Savina.ftiApp.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentGradeDto {
    private Integer gradeId;
    private Integer courseId;
    private String emriLende;
    private Integer kredite;
    private String nota;
    private Double notaValue;
    private Integer studyYear;
    private String status;
    private String dateGiven;
}
