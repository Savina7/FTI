package Savina.ftiApp.repository;

import Savina.ftiApp.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {
    Optional<Room> findByRoomNameIgnoreCase(String roomName);
    List<Room> findAllByOrderByRoomNameAsc();
}
