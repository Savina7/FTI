package Savina.ftiApp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "EXAM_ATTENDANCE")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExamAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EXAM_ATTENDANCE_ID")
    private Integer examAttendanceId;

    @ToString.Exclude @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXAM_ID", nullable = false)
    private ExamSchedule examSchedule;

    @ToString.Exclude @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STUDENT_ID", nullable = false)
    private Student student;

    @Column(name = "STATUS", length = 20)
    @Builder.Default
    private String status = "present";
}
