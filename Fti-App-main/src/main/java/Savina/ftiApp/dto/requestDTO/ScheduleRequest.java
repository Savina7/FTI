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
    private String roleType; // LEKSION, SEMINAR, LABORATOR
    private Integer professorId;
    private Integer classId;
    private String dayOfWeek; // E Hene, E Marte, ...
    private String startTime; // "08:00"
    private String endTime;   // "10:00"
    private Integer roomId;
    private String semester;
    private String academicYear;
}
