package Savina.ftiApp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "LOGIN_HISTORY")
public class LoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "LOGIN_TIME", nullable = false)
    private LocalDateTime loginTime;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;

    @PrePersist
    public void prePersist() {
        if (this.loginTime == null) {
            this.loginTime = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = "SUCCESS";
        }
    }
}
