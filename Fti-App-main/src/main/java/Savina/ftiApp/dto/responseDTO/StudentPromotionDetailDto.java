package Savina.ftiApp.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentPromotionDetailDto {
    private Integer studentId;
    private String emri;
    private String mbiemri;
    private String nrMatrikulimit;
    private Integer oldYear;
    private Integer newYear;
    private Integer creditsYear1;
    private Integer creditsYear2;
    private Integer totalCredits;
    private String previousStatus;
    private String newStatus;
    private boolean promoted;
    private String reason;
}
