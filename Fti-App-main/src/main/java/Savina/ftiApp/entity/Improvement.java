package Savina.ftiApp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "IMPROVEMENTS")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Improvement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IMPROVEMENT_ID")
    private Integer improvementId;

    @ToString.Exclude @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STUDENT_ID", nullable = false)
    private Student student;

    @ToString.Exclude @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEACHING_COURSE_ID", nullable = false)
    private TeachingCourse teachingCourse;

    @Column(name = "DATE_APPLIED")
    private LocalDate dateApplied;

    @Column(name = "STATUS", length = 20)
    private String status;

    @ToString.Exclude @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GRADE_ID")
    private Grade grade;
}
