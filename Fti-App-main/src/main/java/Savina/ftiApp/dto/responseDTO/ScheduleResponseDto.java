package Savina.ftiApp.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponseDto {
    private Integer scheduleId;
    private Integer courseId;
    private String courseName;
    private Integer professorId;
    private String professorName;
    private String roleType;
    private Integer classId;
    private String className;
    private String programName;
    private Integer studyYear;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private Integer roomId;
    private String roomName;
    private String semester;
    private String academicYear;
}
