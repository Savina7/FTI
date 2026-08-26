package Savina.ftiApp.repository;

import Savina.ftiApp.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Integer> {

    List<Topic> findByTeachingCourseTeachingCourseIdOrderByWeekNumberAsc(Integer teachingCourseId);

    List<Topic> findByTeachingCourseCourseCourseIdOrderByWeekNumberAsc(Integer courseId);
}
