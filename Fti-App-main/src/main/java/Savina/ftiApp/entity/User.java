package Savina.ftiApp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "USERS")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    private Integer userId;

    @Column(name = "EMRI", length = 50)
    private String emri;

    @Column(name = "MBIEMRI", length = 50)
    private String mbiemri;

    @Column(name = "EMAIL", length = 50)
    private String email;

    @Column(name = "PASSWORD", length = 255)
    private String password;

    @Column(name = "CREATED_AT")
    private LocalDate createdAt;

    @Column(name = "STATUS", length = 50)
    private String status;

    @Builder.Default
    @Column(name = "VERIFIED", columnDefinition = "CHAR(1) DEFAULT 'N'", nullable = false)
    private String verified = "Y";

    @Column(name = "VERIFICATION_CODE", length = 6)
    private String verificationCode;

    @Column(name = "CODE_CREATED_AT")
    private LocalDateTime codeCreatedAt;

    @Builder.Default
    @Column(name = "CHANGE_PASS", length = 3)
    private String changePass = "NO";

    @Builder.Default
    @ToString.Exclude @EqualsAndHashCode.Exclude
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "USER_ROLES",
        joinColumns = @JoinColumn(name = "USER_ID"),
        inverseJoinColumns = @JoinColumn(name = "ROLE_ID")
    )
    private Set<Role> roles = new HashSet<>();
}
