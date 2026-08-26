package Savina.ftiApp.dto.requestDTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeachingCourseRequest {
    @NotNull(message = "Lenda eshte e detyrueshme")
    private Integer courseId;

    @NotNull(message = "Pedagogu eshte i detyrueshem")
    private Integer professorId;

    private String semester;
    private Integer durationWeeks;
    private List<Integer> classIds;
}
