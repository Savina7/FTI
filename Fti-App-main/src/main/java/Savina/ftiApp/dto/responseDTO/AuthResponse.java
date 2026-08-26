package Savina.ftiApp.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private Integer id;
    private String email;
    private String name;
    private String role;
    private String message;

    public AuthResponse(String token, Integer id, String email, String role, String message) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.role = role;
        this.message = message;
    }

    public AuthResponse(Integer id, String message) {
        this.id = id;
        this.message = message;
    }

    public AuthResponse(String message) {
        this.message = message;
    }
}
