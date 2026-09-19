package Savina.ftiApp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "COURSE_SCHEDULE")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CourseSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SCHEDULE_ID")
    private Integer scheduleId;

    @ToString.Exclude @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEACHING_COURSE_ID", nullable = false)
    private TeachingCourse teachingCourse;

    @ToString.Exclude @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CLASS_ID", nullable = false)
    private Classes classes;

    @Column(name = "DAY_OF_WEEK", length = 10, nullable = false)
    private String dayOfWeek;

    @Column(name = "START_TIME", length = 10, nullable = false)
    private String startTime;

    @Column(name = "END_TIME", length = 10, nullable = false)
    private String endTime;

    @ToString.Exclude @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ROOM_ID")
    private Room room;

    public String getAcademicYear() {
        return (teachingCourse != null && teachingCourse.getAcademicYear() != null)
                ? teachingCourse.getAcademicYear() : null;
    }
}
