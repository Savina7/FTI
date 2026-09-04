package Savina.ftiApp.dto.requestDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

    @NotBlank(message = "Email-i institucional eshte i detyrueshem.")
    @Email(message = "Ju lutem vendosni nje email te vlefshem.")
    private String email;

    @NotBlank(message = "Kodi i verifikimit eshte i detyrueshem.")
    @Size(min = 6, max = 6, message = "Kodi i verifikimit duhet te kete 6 shifra.")
    private String code;

    @NotBlank(message = "Fjalekalimi i ri eshte i detyrueshem.")
    @Size(min = 6, message = "Fjalekalimi i ri duhet te kete te pakten 6 karaktere.")
    private String newPassword;
}
