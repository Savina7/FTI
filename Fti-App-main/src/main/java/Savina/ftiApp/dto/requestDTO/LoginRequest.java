package Savina.ftiApp.dto.requestDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Email-i eshte i detyrueshem.")
    private String email;

    @NotBlank(message = "Fjalekalimi eshte i detyrueshem.")
    private String password;
}
