package Savina.ftiApp.dto.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VerifyRequest {

    @NotNull
    private Integer userId;

    @NotBlank
    @Size(min = 6, max = 6, message = "Kodi duhet te kete 6 shifra")
    private String code;
}
