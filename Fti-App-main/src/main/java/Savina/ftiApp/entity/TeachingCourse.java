package Savina.ftiApp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "TEACHING_COURSES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeachingCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TEACHING_COURSE_ID")
    private Integer teachingCourseId;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COURSE_ID", nullable = false)
    private Course course;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROFESSOR_ID", nullable = false)
    private Professor professor;

    @Column(name = "ROLE_TYPE", length = 20)
    private String roleType;

    @Column(name = "ACADEMIC_YEAR", length = 20)
    private String academicYear;

    @Column(name = "WEEKLY_HOURS")
    private Double weeklyHours;

    @Column(name = "TOTAL_HOURS")
    private Double totalHours;

    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany
    @JoinTable(
            name = "TEACHING_COURSES_CLASSES",
            joinColumns = @JoinColumn(name = "TEACHING_COURSE_ID"),
            inverseJoinColumns = @JoinColumn(name = "CLASS_ID")
    )
    private Set<Classes> classes = new HashSet<>();
}
