package Savina.ftiApp.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromotionResultDto {
    private int totalProcessed;
    private int promotedYear1To2;
    private int promotedYear2To3;
    private int graduated;
    private int repeatingYear1;
    private int repeatingYear2;
    private int repeatingYear3;
    private String message;

    @Builder.Default
    private List<StudentPromotionDetailDto> details = new ArrayList<>();
}
