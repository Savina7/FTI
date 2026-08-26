package Savina.ftiApp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ROOMS")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ROOM_ID")
    private Integer roomId;

    @Column(name = "ROOM_NAME", length = 50)
    private String roomName;

    @Column(name = "CAPACITY")
    private Integer capacity;
}
