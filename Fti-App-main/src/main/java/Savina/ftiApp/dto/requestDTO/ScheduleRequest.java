package Savina.ftiApp.dto.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRequest {
    private Integer scheduleId;
    private Integer departmentId;
    private Integer programId;
    private Integer studyYear;
    private Integer courseId;
    private String roleType;
    private Integer professorId;
    private Integer classId;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private Integer roomId;
    private String semester;
    private String academicYear;
}
