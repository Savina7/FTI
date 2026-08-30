package Savina.ftiApp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "EXAM_SEASONS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamSeason {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SEASON_ID")
    private Integer seasonId;

    @Column(name = "ACADEMIC_YEAR", nullable = false, length = 20)
    private String academicYear;

    @Column(name = "SEASON_TYPE", nullable = false, length = 20)
    private String seasonType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROGRAM_ID")
    private Program program;

    @Column(name = "START_DATE", nullable = false)
    private LocalDate startDate;

    @Column(name = "END_DATE", nullable = false)
    private LocalDate endDate;
}
