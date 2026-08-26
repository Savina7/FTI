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
public class StudentAttendanceDto {
    private Integer courseId;
    private String emriLende;

    private int seminareAbsencesCount;
    private int seminareTotalHours;
    private String seminareStatus;
    private List<String> seminareDates;

    private int laboratoreAbsencesCount;
    private int laboratoreTotalHours;
    private String laboratoreStatus;
    private List<String> laboratoreDates;
}
