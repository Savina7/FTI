package Savina.ftiApp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "FAILED_COURSES")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FailedCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FAILED_ID")
    private Integer failedId;

    @ToString.Exclude @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STUDENT_ID", nullable = false)
    private Student student;

    @ToString.Exclude @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEACHING_COURSE_ID", nullable = false)
    private TeachingCourse teachingCourse;

    @ToString.Exclude @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GRADE_ID", nullable = false)
    private Grade grade;

    @Column(name = "DATE_RECORDED")
    private LocalDate dateRecorded;
}
