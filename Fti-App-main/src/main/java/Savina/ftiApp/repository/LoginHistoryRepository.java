package Savina.ftiApp.repository;

import Savina.ftiApp.entity.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    // Historiku i hyrjeve per nje perdorues te caktuar
    List<LoginHistory> findByUser_UserIdOrderByLoginTimeDesc(Integer userId);

    // Te gjitha hyrjet e fundit ne sistem
    List<LoginHistory> findAllByOrderByLoginTimeDesc();
}
