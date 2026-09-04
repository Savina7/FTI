package Savina.ftiApp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "EXAMS_SCHEDULE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EXAM_ID")
    private Integer examId;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COURSE_ID", nullable = false)
    private Course course;

    @Column(name = "EXAM_DATE", nullable = false)
    private LocalDateTime examDate;

    @Column(name = "TYPE", length = 20)
    private String type;

    @Column(name = "END_TIME", length = 10)
    private String endTime;

    @Column(name = "MAX_STUDENTS")
    private Integer maxStudents;

    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany
    @JoinTable(
            name = "EXAM_ROOMS",
            joinColumns = @JoinColumn(name = "EXAM_ID"),
            inverseJoinColumns = @JoinColumn(name = "ROOM_ID")
    )
    private Set<Room> rooms = new HashSet<>();
}
