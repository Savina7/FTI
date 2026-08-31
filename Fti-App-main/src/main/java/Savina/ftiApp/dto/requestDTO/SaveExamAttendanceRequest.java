package Savina.ftiApp.dto.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveExamAttendanceRequest {
    private Integer examId;
    private Integer courseId;
    private Integer classId;
    private List<StudentStatusEntry> entries;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentStatusEntry {
        private Integer studentId;
        private String status; // "present" ose "absent"
    }
}
