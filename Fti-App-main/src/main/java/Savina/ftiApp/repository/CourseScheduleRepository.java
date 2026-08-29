package Savina.ftiApp.repository;

import Savina.ftiApp.entity.CourseSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CourseScheduleRepository extends JpaRepository<CourseSchedule, Integer> {

    @Query("SELECT DISTINCT cs.academicYear FROM CourseSchedule cs WHERE cs.academicYear IS NOT NULL AND cs.academicYear <> ''")
    List<String> findDistinctAcademicYears();

    List<CourseSchedule> findByRoom_RoomId(Integer roomId);

    @Query("SELECT cs FROM CourseSchedule cs " +
           "WHERE cs.classes.program.programId = :programId " +
           "AND (:studyYear IS NULL OR cs.classes.vitStudimit = :studyYear) " +
           "ORDER BY cs.dayOfWeek, cs.startTime")
    List<CourseSchedule> findByProgramAndStudyYear(@Param("programId") Integer programId,
                                                  @Param("studyYear") Integer studyYear);

    @Query("SELECT cs FROM CourseSchedule cs " +
           "WHERE cs.room.roomId = :roomId " +
           "AND (LOWER(TRIM(cs.dayOfWeek)) = LOWER(TRIM(:dayOfWeek)) OR LOWER(cs.dayOfWeek) LIKE CONCAT('%', :dayKey, '%')) " +
           "AND (cs.startTime < :newEndTime AND cs.endTime > :newStartTime) " +
           "AND (:excludeScheduleId IS NULL OR cs.scheduleId <> :excludeScheduleId)")
    List<CourseSchedule> findOverlappingRoomSchedules(
            @Param("roomId") Integer roomId,
            @Param("dayOfWeek") String dayOfWeek,
            @Param("dayKey") String dayKey,
            @Param("newStartTime") String newStartTime,
            @Param("newEndTime") String newEndTime,
            @Param("excludeScheduleId") Integer excludeScheduleId
    );

    @Query("SELECT cs FROM CourseSchedule cs " +
           "WHERE cs.classes.classId = :classId " +
           "AND (LOWER(TRIM(cs.dayOfWeek)) = LOWER(TRIM(:dayOfWeek)) OR LOWER(cs.dayOfWeek) LIKE CONCAT('%', :dayKey, '%')) " +
           "AND (cs.startTime < :newEndTime AND cs.endTime > :newStartTime) " +
           "AND (:excludeScheduleId IS NULL OR cs.scheduleId <> :excludeScheduleId)")
    List<CourseSchedule> findOverlappingClassSchedules(
            @Param("classId") Integer classId,
            @Param("dayOfWeek") String dayOfWeek,
            @Param("dayKey") String dayKey,
            @Param("newStartTime") String newStartTime,
            @Param("newEndTime") String newEndTime,
            @Param("excludeScheduleId") Integer excludeScheduleId
    );

    @Query("SELECT cs FROM CourseSchedule cs " +
           "WHERE cs.teachingCourse.professor.professorId = :professorId " +
           "AND (LOWER(TRIM(cs.dayOfWeek)) = LOWER(TRIM(:dayOfWeek)) OR LOWER(cs.dayOfWeek) LIKE CONCAT('%', :dayKey, '%')) " +
           "AND (cs.startTime < :newEndTime AND cs.endTime > :newStartTime) " +
           "AND (:excludeScheduleId IS NULL OR cs.scheduleId <> :excludeScheduleId)")
    List<CourseSchedule> findOverlappingProfessorSchedules(
            @Param("professorId") Integer professorId,
            @Param("dayOfWeek") String dayOfWeek,
            @Param("dayKey") String dayKey,
            @Param("newStartTime") String newStartTime,
            @Param("newEndTime") String newEndTime,
            @Param("excludeScheduleId") Integer excludeScheduleId
    );
}
