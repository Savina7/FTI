package Savina.ftiApp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CLASSES")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Classes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CLASS_ID")
    private Integer classId;

    @ToString.Exclude @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROGRAM_ID", nullable = false)
    private Program program;

    @Column(name = "VIT_STUDIMIT", nullable = false)
    private Integer vitStudimit;

    @Column(name = "EMRI_CLASS", length = 50, nullable = false)
    private String emriClass;
}
