package Savina.ftiApp.dto.requestDTO;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProfessorRegisterRequest {

    @NotBlank
    @Size(min = 3, max = 50)
    private String emri;

    @NotBlank
    @Size(min = 3, max = 50)
    private String mbiemri;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9._%+\\-]+@fti\\.edu\\.al$",
             message = "Email duhet te jete ne formatin emri.mbiemri@fti.edu.al")
    private String email;

    @NotBlank
    private String department;

    @NotBlank
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$",
             message = "Fjalekalimi duhet te kete te pakten 8 karaktere, nje shkronje te madhe, nje te vogel, nje numer dhe nje karakter special.")
    private String password;
}
