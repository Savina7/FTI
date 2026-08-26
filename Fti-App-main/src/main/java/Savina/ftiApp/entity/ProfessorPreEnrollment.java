package Savina.ftiApp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PROFESSOR_PRE_ENROLLMENT")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProfessorPreEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqProfEnrollment")
    @SequenceGenerator(name = "seqProfEnrollment", sequenceName = "FTIAPP.SEQ_PROF_PRE_ENROLLMENT", allocationSize = 1)
    @Column(name = "PRE_ENROLLMENT_ID")
    private Integer preEnrollmentId;

    @Column(name = "EMRI", length = 50, nullable = false)
    private String emri;

    @Column(name = "MBIEMRI", length = 50, nullable = false)
    private String mbiemri;

    @Column(name = "EMAIL", length = 100, nullable = false, unique = true)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPARTMENT_ID", nullable = false)
    private Department department;
}
