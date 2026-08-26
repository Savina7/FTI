package Savina.ftiApp.service;

import Savina.ftiApp.dto.responseDTO.TopicDto;
import Savina.ftiApp.entity.Course;
import Savina.ftiApp.entity.TeachingCourse;
import Savina.ftiApp.entity.Topic;
import Savina.ftiApp.mapper.TopicMapper;
import Savina.ftiApp.repository.CourseRepository;
import Savina.ftiApp.repository.TeachingCourseRepository;
import Savina.ftiApp.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepo;
    private final TeachingCourseRepository teachingCourseRepo;
    private final CourseRepository courseRepo;
    private final TopicMapper topicMapper;

    @Transactional(readOnly = true)
    public List<TopicDto> getTopics(Integer courseId, Integer teachingCourseId) {
        List<Topic> topics = new ArrayList<>();

        if (teachingCourseId != null) {
            topics = topicRepo.findByTeachingCourseTeachingCourseIdOrderByWeekNumberAsc(teachingCourseId);
        }
        if (topics.isEmpty() && courseId != null) {
            topics = topicRepo.findByTeachingCourseCourseCourseIdOrderByWeekNumberAsc(courseId);
        }

        return topics.stream().map(topicMapper::toDto).collect(Collectors.toList());
    }

    @Transactional
    public TopicDto createTopic(TopicDto dto) {
        TeachingCourse tc = null;
        if (dto.getTeachingCourseId() != null) {
            tc = teachingCourseRepo.findById(dto.getTeachingCourseId()).orElse(null);
        }
        if (tc == null && dto.getCourseId() != null) {
            List<TeachingCourse> tcs = teachingCourseRepo.findByCourseCourseId(dto.getCourseId());
            if (!tcs.isEmpty()) tc = tcs.get(0);
        }

        Topic topic = topicMapper.toEntity(dto, tc);
        Topic saved = topicRepo.save(topic);
        return topicMapper.toDto(saved);
    }

    @Transactional
    public TopicDto updateTopic(Integer topicId, TopicDto dto) {
        Topic topic = topicRepo.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Tema nuk u gjet me ID: " + topicId));

        if (dto.getWeekNumber() != null) topic.setWeekNumber(dto.getWeekNumber());
        if (dto.getTopicDate() != null) topic.setTopicDate(dto.getTopicDate());
        if (dto.getTitle() != null && !dto.getTitle().isBlank()) topic.setTitle(dto.getTitle());
        if (dto.getDescription() != null) topic.setDescription(dto.getDescription());

        Topic saved = topicRepo.save(topic);
        return topicMapper.toDto(saved);
    }

    @Transactional
    public void deleteTopic(Integer topicId) {
        topicRepo.deleteById(topicId);
    }
}
