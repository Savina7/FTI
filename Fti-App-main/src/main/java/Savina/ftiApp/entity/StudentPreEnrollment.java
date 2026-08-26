package Savina.ftiApp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "STUDENT_PRE_ENROLLMENT")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentPreEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqEnrollment")
    @SequenceGenerator(name = "seqEnrollment", sequenceName = "FTIAPP.SEQ_ENROLLMENT", allocationSize = 1)
    @Column(name = "ENROLLMENT_ID")
    private Integer enrollmentId;

    @Column(name = "EMRI", length = 50, nullable = false)
    private String emri;

    @Column(name = "MBIEMRI", length = 50, nullable = false)
    private String mbiemri;

    @Column(name = "EMAIL", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "NR_MATRIKULIMIT", length = 12, nullable = false, unique = true)
    private String nrMatrikulimit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROGRAM_ID")
    private Program program;

    @Column(name = "VIT_STUDIMIT")
    private Integer vitStudimit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CLASS_ID")
    private Classes classes;

    @Builder.Default
    @Column(name = "STATUS", length = 30)
    private String status = "PARAREGJISTRUAR";
}
