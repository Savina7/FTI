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
    private String examDate;
    private String displayDate;
    private String startTime;
    private String endTime;
    private String timeRange;
    private String roomNames;
    private List<Integer> roomIds;
    private String season;
    private Integer studyYear;
    private String semester;
}
