package Savina.ftiApp.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicDto {
    private Integer topicId;
    private Integer weekNumber;
    private LocalDate topicDate;
    private String formattedDate;
    private String title;
    private String description;
    private Integer teachingCourseId;
    private Integer courseId;
}
