package Savina.ftiApp.dto.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForceChangePasswordRequest {

    @NotBlank(message = "Email eshte i detyrueshem")
    private String email;

    @NotBlank(message = "Fjalekalimi aktual eshte i detyrueshem")
    private String currentPassword;

    @NotBlank(message = "Fjalekalimi i ri eshte i detyrueshem")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$",
             message = "Fjalekalimi i ri duhet te kete te pakten 8 karaktere, nje shkronje te madhe, nje te vogel, nje numer dhe nje karakter special.")
    private String newPassword;

    @NotBlank(message = "Konfirmimi i fjalekalimit eshte i detyrueshem")
    private String confirmPassword;
}
