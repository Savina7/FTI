package Savina.ftiApp.dto.responseDTO;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSeasonDto {
    private Integer seasonId;
    private String academicYear;
    private String seasonType;
    private Integer programId;
    private String startDate;
    private String endDate;
}
