package Savina.ftiApp.dto.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StudentPreEnrollmentRequest {

    @NotBlank(message = "Emri eshte i detyrueshem.")
    @Size(min = 3, max = 50, message = "Emri duhet te kete te pakten 3 karaktere.")
    private String emri;

    @NotBlank(message = "Mbiemri eshte i detyrueshem.")
    @Size(min = 3, max = 50, message = "Mbiemri duhet te kete te pakten 3 karaktere.")
    private String mbiemri;

    @NotBlank(message = "Email eshte i detyrueshem.")
    @Pattern(regexp = "^[a-zA-Z0-9._%+\\-]+@fti\\.edu\\.al$", message = "Email duhet te jete ne formatin emri.mbiemri@fti.edu.al")
    private String email;

    @NotBlank(message = "Numri i matrikullimit eshte i detyrueshem.")
    @Size(min = 12, max = 12, message = "Nr. Matrikulimit duhet te kete saktesisht 12 karaktere.")
    private String nrMatrikulimit;

    private Integer programId;
    private Integer vitStudimit;
    private Integer classId;

    private String program;
    private String dega;
    private String grupi;
    private Boolean doubleDegree;
    private String status;
}

