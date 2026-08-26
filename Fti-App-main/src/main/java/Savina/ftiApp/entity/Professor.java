package Savina.ftiApp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PROFESSORS")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Professor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PROFESSOR_ID")
    private Integer professorId;

    @ToString.Exclude @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    @Column(name = "STATUS", columnDefinition = "CHAR(1)")
    private String status;

    @ToString.Exclude @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPARTMENT_ID")
    private Department department;
}
