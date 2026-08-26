package Savina.ftiApp.dto.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveAttendanceRequest {
    private Integer courseId;
    private Integer classId;
    private List<StudentAttendanceEntry> attendances;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentAttendanceEntry {
        private Integer studentId;
        private Map<String, Boolean> attendance;
    }
}
