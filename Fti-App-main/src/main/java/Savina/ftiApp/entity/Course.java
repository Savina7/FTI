package Savina.ftiApp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "COURSES")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COURSE_ID")
    private Integer courseId;

    @ToString.Exclude @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROGRAM_ID")
    private Program program;

    @Column(name = "EMRI_COURSE", length = 50, nullable = false)
    private String emriCourse;

    @Column(name = "KREDITE", nullable = false)
    private Integer kredite;

    @Column(name = "STATUS", length = 50)
    private String status;

    @Column(name = "STUDY_YEAR")
    private Integer studyYear;

    @Builder.Default
    @ToString.Exclude @EqualsAndHashCode.Exclude
    @ManyToMany
    @JoinTable(
        name = "COURSE_DEPARTMENTS",
        joinColumns = @JoinColumn(name = "COURSE_ID"),
        inverseJoinColumns = @JoinColumn(name = "DEPARTMENT_ID")
    )
    private Set<Department> departments = new HashSet<>();

    @Builder.Default
    @ToString.Exclude @EqualsAndHashCode.Exclude
    @ManyToMany
    @JoinTable(
        name = "COURSE_COURSETYPES",
        joinColumns = @JoinColumn(name = "COURSE_ID"),
        inverseJoinColumns = @JoinColumn(name = "TYPE_ID")
    )
    private Set<CourseType> courseTypes = new HashSet<>();
}
