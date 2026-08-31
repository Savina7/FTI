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
public class ExamAttendancePageDto {
    private Integer examId;
    private Integer courseId;
    private String courseName;
    private String examDate;
    private String timeRange;
    private String roomNames;
    private boolean hasExam;
    private List<ExamAttendanceStudentDto> students;
}
