package Savina.ftiApp.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomAvailabilityDto {
    private boolean available;
    private Integer roomId;
    private String roomName;
    private String message;
    private String conflictingCourse;
    private String conflictingProfessor;
    private String conflictingClass;
    private String conflictingTime;

    private boolean roomAvailable;
    private String roomConflictMessage;
    private boolean professorAvailable;
    private String professorConflictMessage;
}
