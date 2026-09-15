package Savina.ftiApp.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfessorAdminDto {
    private Integer id;
    private String emri;
    private String mbiemri;
    private String email;
    private String department;
    private String status;
    private String tempPassword;
}
