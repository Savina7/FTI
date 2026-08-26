package Savina.ftiApp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PROGRAMS")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Program {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PROGRAM_ID")
    private Integer programId;

    @ToString.Exclude @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPARTMENT_ID")
    private Department department;

    @Column(name = "NIVEL", length = 20, nullable = false)
    private String nivel;

    @Column(name = "LLOJI", length = 20, nullable = false)
    private String lloji;

    @Column(name = "DIPLOME_DYFISHTE", length = 10)
    private String diplomeDyfishte;

    @Column(name = "SPECIALIZIMI", length = 100, nullable = false)
    private String specializimi;
}
