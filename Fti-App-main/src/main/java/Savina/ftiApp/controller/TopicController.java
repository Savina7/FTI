package Savina.ftiApp.controller;

import Savina.ftiApp.dto.responseDTO.TopicDto;
import Savina.ftiApp.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @GetMapping
    public ResponseEntity<List<TopicDto>> getTopics(
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) Integer teachingCourseId) {
        return ResponseEntity.ok(topicService.getTopics(courseId, teachingCourseId));
    }

    @PostMapping
    public ResponseEntity<TopicDto> createTopic(@RequestBody TopicDto dto) {
        return ResponseEntity.ok(topicService.createTopic(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TopicDto> updateTopic(@PathVariable Integer id, @RequestBody TopicDto dto) {
        return ResponseEntity.ok(topicService.updateTopic(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTopic(@PathVariable Integer id) {
        topicService.deleteTopic(id);
        return ResponseEntity.ok().build();
    }
}
