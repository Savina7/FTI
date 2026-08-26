package Savina.ftiApp.mapper;

import Savina.ftiApp.dto.responseDTO.TopicDto;
import Savina.ftiApp.entity.TeachingCourse;
import Savina.ftiApp.entity.Topic;
import org.springframework.stereotype.Component;

@Component
public class TopicMapper {

    public TopicDto toDto(Topic topic) {
        if (topic == null) return null;

        Integer tcId = topic.getTeachingCourse() != null ? topic.getTeachingCourse().getTeachingCourseId() : null;
        Integer cId = (topic.getTeachingCourse() != null && topic.getTeachingCourse().getCourse() != null)
                ? topic.getTeachingCourse().getCourse().getCourseId()
                : null;

        String fmtDate = "";
        if (topic.getTopicDate() != null) {
            fmtDate = topic.getTopicDate().toString();
        }

        TopicDto dto = new TopicDto();
        dto.setTopicId(topic.getTopicId());
        dto.setWeekNumber(topic.getWeekNumber());
        dto.setTopicDate(topic.getTopicDate());
        dto.setFormattedDate(fmtDate);
        dto.setTitle(topic.getTitle());
        dto.setDescription(topic.getDescription());
        dto.setTeachingCourseId(tcId);
        dto.setCourseId(cId);
        return dto;
    }

    public Topic toEntity(TopicDto dto, TeachingCourse tc) {
        if (dto == null) return null;

        Topic topic = new Topic();
        topic.setWeekNumber(dto.getWeekNumber() != null ? dto.getWeekNumber() : 1);
        topic.setTopicDate(dto.getTopicDate());
        topic.setTitle(dto.getTitle() != null ? dto.getTitle() : "Tema e Re");
        topic.setDescription(dto.getDescription());
        topic.setTeachingCourse(tc);
        return topic;
    }
}
