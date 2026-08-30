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
    private String examDate;    // "YYYY-MM-DD"
    private String startTime;   // "09:00"
    private String endTime;     // "12:00"
    private String season;      // "VJESHTE", "VERE", "DIMER"
    private List<Integer> roomIds;
    private List<String> customRoomNames; // Nese vendosen salla me dore
}
