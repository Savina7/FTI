package Savina.ftiApp.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentAdminDto {
    private Integer id;
    private String emri;
    private String mbiemri;
    private String email;
    private String nrMatrikulimit;
    private String program;
    private String specializimi;
    private Integer vitStudimit;
    private String grupi;
    private String dega;
    private String status; // "PARAREGJISTRUAR" or "VERIFIKUAR"
}
