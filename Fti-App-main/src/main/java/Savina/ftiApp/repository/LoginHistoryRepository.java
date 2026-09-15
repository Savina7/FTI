package Savina.ftiApp.repository;

import Savina.ftiApp.entity.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    List<LoginHistory> findByUser_UserIdOrderByLoginTimeDesc(Integer userId);

    List<LoginHistory> findAllByOrderByLoginTimeDesc();
}
