package Savina.ftiApp.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfileDto {
    private Integer studentId;
    private String emriMbiemri;
    private String email;
    private String nrMatrikulimit;
    private String programiStudimit;
    private String vitiAkademik;
    private Integer totalKredite;
    private Double mesatarjaPonderuar;
    private String mesatarjaFormatted;
}
