package Savina.ftiApp.dto.requestDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordRequest {

    @NotBlank(message = "Email-i institucional eshte i detyrueshem.")
    @Email(message = "Ju lutem vendosni nje email te vlefshem.")
    private String email;
}
