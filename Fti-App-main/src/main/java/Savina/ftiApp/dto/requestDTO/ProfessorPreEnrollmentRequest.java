package Savina.ftiApp.dto.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfessorPreEnrollmentRequest {

    @NotBlank(message = "Emri eshte i detyrueshem.")
    @Size(min = 3, max = 50, message = "Emri duhet te kete te pakten 3 karaktere.")
    private String emri;

    @NotBlank(message = "Mbiemri eshte i detyrueshem.")
    @Size(min = 3, max = 50, message = "Mbiemri duhet te kete te pakten 3 karaktere.")
    private String mbiemri;

    @NotBlank(message = "Email eshte i detyrueshem.")
    @Pattern(regexp = "^[a-zA-Z0-9._%+\\-]+@fti\\.edu\\.al$", message = "Email duhet te jete ne formatin emri.mbiemri@fti.edu.al")
    private String email;

    @NotBlank(message = "Departamenti eshte i detyrueshem.")
    private String department;

    private String status;
}
