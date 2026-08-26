package Savina.ftiApp.dto.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveGradesRequest {
    private Integer courseId;
    private Integer classId;
    private List<StudentGradeEntry> grades;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentGradeEntry {
        private Integer studentId;
        private BigDecimal grade;
        private String status;
    }
}
