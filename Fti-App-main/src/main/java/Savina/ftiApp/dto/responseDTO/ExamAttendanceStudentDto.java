package Savina.ftiApp.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamAttendanceStudentDto {
    private Integer studentId;
    private String studentName;
    private String matrikulli;
    private String status;
}
