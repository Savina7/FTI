package Savina.ftiApp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "COURSE_TYPES")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CourseType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TYPE_ID")
    private Integer typeId;

    @Column(name = "EMRI_TYPE", length = 50, nullable = false, unique = true)
    private String emriType;
}
