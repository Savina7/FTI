package Savina.ftiApp.dto.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseRequest {
    @NotBlank(message = "Emri i lendes eshte i detyrueshem")
    private String emriCourse;

    @NotNull(message = "Programi eshte i detyrueshem")
    private Integer programId;

    @NotNull(message = "Kredite jane te detyrueshme")
    private Integer kredite;

    private String status;

    private Integer studyYear;
}
