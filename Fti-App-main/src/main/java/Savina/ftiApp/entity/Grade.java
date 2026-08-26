package Savina.ftiApp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "GRADES")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "GRADE_ID")
    private Integer gradeId;

    @ToString.Exclude @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STUDENT_ID", nullable = false)
    private Student student;

    @Column(name = "GRADE", precision = 3, scale = 1)
    private BigDecimal grade;

    @Column(name = "STATUS", length = 20)
    private String status;

    @Column(name = "DATE_GIVEN")
    private LocalDate dateGiven;

    @ToString.Exclude @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEACHING_COURSE_ID")
    private TeachingCourse teachingCourse;
}
