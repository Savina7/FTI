package Savina.ftiApp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "ATTENDANCE")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ATTENDANCE_ID")
    private Integer attendanceId;

    @ToString.Exclude @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STUDENT_ID", nullable = false)
    private Student student;

    @ToString.Exclude @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEACHING_COURSE_ID", nullable = false)
    private TeachingCourse teachingCourse;

    @Column(name = "DATA_ATTENDANCE", nullable = false)
    private LocalDate dataAttendance;

    @Column(name = "STATUS")
    private Integer status;
}
