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
public class ExamScheduleRequestDto {
    private Integer examId;
    private Integer courseId;
    private Integer programId;
    private String examDate;
    private String startTime;
    private String endTime;
    private String season;
    private List<Integer> roomIds;
    private List<String> customRoomNames;
}
