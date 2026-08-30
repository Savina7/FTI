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
public class ExamScheduleDto {
    private Integer examId;
    private Integer courseId;
    private String courseName;
    private Integer programId;
    private String programName;
    private String examDate;      // p.sh. "2025-09-08"
    private String displayDate;   // p.sh. "8 Shtator"
    private String startTime;     // p.sh. "09:00"
    private String endTime;       // p.sh. "12:00"
    private String timeRange;     // p.sh. "09:00-12:00"
    private String roomNames;     // p.sh. "204, 205, 206"
    private List<Integer> roomIds;
    private String season;        // p.sh. "VJESHTE"
    private Integer studyYear;
    private String semester;
}
