package Savina.ftiApp.service;

import Savina.ftiApp.dto.requestDTO.ExamScheduleRequestDto;
import Savina.ftiApp.dto.responseDTO.ExamScheduleDto;
import Savina.ftiApp.entity.*;
import Savina.ftiApp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminExamScheduleService {

    private final ExamScheduleRepository examScheduleRepo;
    private final CourseRepository courseRepo;
    private final ProgramRepository programRepo;
    private final RoomRepository roomRepo;
    private final TeachingCourseRepository teachingCourseRepo;
    private final ExamSeasonRepository examSeasonRepo;

    private static final String[] MUAJT_SHQIP = {
        "", "Janar", "Shkurt", "Mars", "Prill", "Maj", "Qershor",
        "Korrik", "Gusht", "Shtator", "Tetor", "Nentor", "Dhjetor"
    };

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getCoursesForProgramAndSemester(Integer programId, String semester) {
        List<Course> allCourses = courseRepo.findByProgramProgramId(programId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Course c : allCourses) {
            String sem = determineCourseSemester(c);

            Map<String, Object> map = new HashMap<>();
            map.put("courseId", c.getCourseId());
            map.put("emriCourse", c.getEmriCourse());
            map.put("studyYear", c.getStudyYear() != null ? c.getStudyYear() : 1);
            map.put("semester", sem);
            map.put("kredite", c.getKredite());
            result.add(map);
        }

        result.sort((a, b) -> {
            Integer y1 = (Integer) a.get("studyYear");
            Integer y2 = (Integer) b.get("studyYear");
            int cmp = Integer.compare(y1, y2);
            if (cmp != 0) {
                return cmp;
            }
            return ((String) a.get("emriCourse")).compareToIgnoreCase((String) b.get("emriCourse"));
        });

        return result;
    }

    public String determineCourseSemester(Course c) {
        if (c == null) return "1";
        if (c.getSemester() != null && !c.getSemester().isBlank()) {
            return c.getSemester().trim();
        }

        String name = c.getEmriCourse() != null ? c.getEmriCourse().toLowerCase() : "";

        if (name.contains("analize matematike 2") || name.contains("analizë matematike 2")
                || name.contains("fizike 2") || name.contains("fizikë 2")
                || name.contains("elektroteknik")
                || name.contains("analize matematike 3") || name.contains("analizë matematike 3")
                || name.contains("teknikat dhe gjuhet") || name.contains("teknikat dhe gjuhët")
                || name.contains("probabilitet")
                || name.contains("gjuhe e huaj 2") || name.contains("gjuhë e huaj 2")
                || name.contains("sistemet elektronike") || name.contains("sisteme elektronike")
                || name.contains("perpunimi numerik") || name.contains("përpunimi numerik")
                || name.contains("arkitekture") || name.contains("arkitekturë")
                || name.contains("strukture te dhenash") || name.contains("strukturë të dhënash")
                || name.contains("ekonomi dhe menaxhim")
                || name.contains("rrjetat e kompjuterave")
                || name.contains("python")
                || name.contains("shperndara") || name.contains("shpërndara")
                || name.contains("legjislacion")
                || name.contains("praktik")
                || name.contains("diplom")
                || name.contains("rrjetet optike")
                || name.contains("sensoret")
                || name.contains("elektronika per telekom")) {
            return "2";
        }

        if (name.contains("analize matematike 1") || name.contains("analizë matematike 1")
                || name.contains("fizike 1") || name.contains("fizikë 1")
                || name.contains("elementet e informatikes") || name.contains("elementet e informatikës")
                || name.contains("algjeber") || name.contains("algjebër")
                || name.contains("gjuhe e huaj 1") || name.contains("gjuhë e huaj 1")
                || name.contains("shkrim dhe prezantim")
                || name.contains("analize numerike") || name.contains("analizë numerike")
                || name.contains("orientuar nga objekti")
                || name.contains("teoria e sinjaleve")
                || name.contains("bazat e te dhenave") || name.contains("baza e te dhenave")
                || name.contains("automatizim")
                || name.contains("teknologjite elektronike")
                || name.contains("algoritmik")
                || name.contains("web")
                || name.contains("inxhinieri softi")
                || name.contains("sistemet operative")
                || name.contains("kibernetike")
                || name.contains("fuqie")
                || name.contains("analoge")
                || name.contains("radiofrekuenc")) {
            return "1";
        }

        return "1";
    }

    @Transactional(readOnly = true)
    public List<ExamScheduleDto> getExamsByProgramAndSeason(Integer programId, String season) {
        List<ExamSchedule> exams;
        if (season != null && !season.isBlank() && !season.equalsIgnoreCase("ALL")) {
            exams = examScheduleRepo.findByCourse_Program_ProgramIdAndType(programId, season.toUpperCase().trim());
        } else {
            exams = examScheduleRepo.findByCourse_Program_ProgramId(programId);
        }

        return exams.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional
    public ExamScheduleDto saveExam(ExamScheduleRequestDto req) {
        Course course = courseRepo.findById(req.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Lenda nuk u gjet: " + req.getCourseId()));

        Program program = req.getProgramId() != null
                ? programRepo.findById(req.getProgramId()).orElse(course.getProgram())
                : course.getProgram();

        LocalDate date = LocalDate.parse(req.getExamDate());
        LocalTime startTime = (req.getStartTime() != null && !req.getStartTime().isBlank())
                ? LocalTime.parse(req.getStartTime())
                : LocalTime.of(9, 0);

        LocalDateTime examDateTime = LocalDateTime.of(date, startTime);

        LocalTime endTime;
        if (req.getEndTime() != null && !req.getEndTime().isBlank()) {
            endTime = LocalTime.parse(req.getEndTime());
        } else {
            endTime = startTime.plusHours(3);
        }

        if (req.getRoomIds() != null && !req.getRoomIds().isEmpty()) {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);
            List<ExamSchedule> sameDayExams = examScheduleRepo.findByExamDateBetween(startOfDay, endOfDay);

            for (ExamSchedule existing : sameDayExams) {
                if (req.getExamId() != null && req.getExamId().equals(existing.getExamId())) {
                    continue;
                }

                LocalTime exStart = existing.getExamDate().toLocalTime();
                LocalTime exEnd;
                if (existing.getEndTime() != null && !existing.getEndTime().isBlank()) {
                    try {
                        exEnd = LocalTime.parse(existing.getEndTime().trim());
                    } catch (Exception ex) {
                        exEnd = exStart.plusHours(3);
                    }
                } else {
                    exEnd = exStart.plusHours(3);
                }

                boolean overlaps = startTime.isBefore(exEnd) && endTime.isAfter(exStart);
                if (overlaps) {
                    for (Room existingRoom : existing.getRooms()) {
                        if (req.getRoomIds().contains(existingRoom.getRoomId())) {
                            throw new IllegalArgumentException("Salla '" + existingRoom.getRoomName() + "' është e zënë!");
                        }
                    }
                }
            }
        }

        ExamSchedule exam;
        if (req.getExamId() != null && req.getExamId() > 0) {
            exam = examScheduleRepo.findById(req.getExamId())
                    .orElse(new ExamSchedule());
        } else {
            exam = new ExamSchedule();
        }

        exam.setCourse(course);
        exam.setExamDate(examDateTime);
        exam.setEndTime(endTime.format(DateTimeFormatter.ofPattern("HH:mm")));
        exam.setType(req.getSeason() != null ? req.getSeason().toUpperCase().trim() : "VJESHTE");

        Set<Room> rooms = new HashSet<>();
        if (req.getRoomIds() != null && !req.getRoomIds().isEmpty()) {
            rooms.addAll(roomRepo.findAllById(req.getRoomIds()));
        }
        exam.setRooms(rooms);

        ExamSchedule saved = examScheduleRepo.save(exam);
        return mapToDto(saved);
    }

    @Transactional
    public void deleteExam(Integer examId) {
        examScheduleRepo.deleteById(examId);
    }

    private ExamScheduleDto mapToDto(ExamSchedule exam) {
        LocalDateTime dt = exam.getExamDate();
        String dateStr = dt.toLocalDate().toString();
        String displayDate = dt.getDayOfMonth() + " " + MUAJT_SHQIP[dt.getMonthValue()];
        String startTime = dt.format(DateTimeFormatter.ofPattern("HH:mm"));
        String endTime = (exam.getEndTime() != null && !exam.getEndTime().isBlank())
                ? exam.getEndTime()
                : dt.plusHours(3).format(DateTimeFormatter.ofPattern("HH:mm"));

        String roomNames = exam.getRooms().stream()
                .map(Room::getRoomName)
                .sorted()
                .collect(Collectors.joining(", "));

        List<Integer> roomIds = exam.getRooms().stream()
                .map(Room::getRoomId)
                .collect(Collectors.toList());

        Program prog = (exam.getCourse() != null && exam.getCourse().getProgram() != null)
                ? exam.getCourse().getProgram() : null;
        String progName = "";
        Integer progId = null;
        if (prog != null) {
            progId = prog.getProgramId();
            progName = (prog.getNivel() != null ? prog.getNivel() + " " : "")
                    + (prog.getSpecializimi() != null ? prog.getSpecializimi() : "");
        }

        return ExamScheduleDto.builder()
                .examId(exam.getExamId())
                .courseId(exam.getCourse() != null ? exam.getCourse().getCourseId() : null)
                .courseName(exam.getCourse() != null ? exam.getCourse().getEmriCourse() : "")
                .programId(progId)
                .programName(progName.trim())
                .examDate(dateStr)
                .displayDate(displayDate)
                .startTime(startTime)
                .endTime(endTime)
                .timeRange(startTime + "-" + endTime)
                .roomNames(roomNames)
                .roomIds(roomIds)
                .season(exam.getType())
                .studyYear(exam.getCourse() != null ? exam.getCourse().getStudyYear() : 1)
                .build();
    }

    public Savina.ftiApp.dto.responseDTO.ExamSeasonDto getSeasonDates(String academicYear, String seasonType, Integer programId) {
        String year = (academicYear != null && !academicYear.isBlank()) ? academicYear.trim() : "2025-2026";
        String type = (seasonType != null && !seasonType.isBlank()) ? seasonType.toUpperCase().trim() : "VJESHTE";

        Optional<ExamSeason> opt = Optional.empty();
        if (programId != null) {
            opt = examSeasonRepo.findByAcademicYearAndSeasonTypeAndProgram_ProgramId(year, type, programId);
        }
        if (opt.isEmpty()) {
            opt = examSeasonRepo.findByAcademicYearAndSeasonTypeAndProgramIsNull(year, type);
        }

        if (opt.isPresent()) {
            ExamSeason es = opt.get();
            return Savina.ftiApp.dto.responseDTO.ExamSeasonDto.builder()
                    .seasonId(es.getSeasonId())
                    .academicYear(es.getAcademicYear())
                    .seasonType(es.getSeasonType())
                    .programId(es.getProgram() != null ? es.getProgram().getProgramId() : null)
                    .startDate(es.getStartDate().toString())
                    .endDate(es.getEndDate().toString())
                    .build();
        }

        String defStart = "2025-09-08";
        String defEnd = "2025-09-18";
        if ("VERE".equalsIgnoreCase(type)) {
            defStart = "2026-06-08";
            defEnd = "2026-06-25";
        } else if ("DIMER".equalsIgnoreCase(type)) {
            defStart = "2026-01-26";
            defEnd = "2026-02-15";
        }

        return Savina.ftiApp.dto.responseDTO.ExamSeasonDto.builder()
                .academicYear(year)
                .seasonType(type)
                .programId(programId)
                .startDate(defStart)
                .endDate(defEnd)
                .build();
    }

    @Transactional
    public Savina.ftiApp.dto.responseDTO.ExamSeasonDto saveSeasonDates(Savina.ftiApp.dto.responseDTO.ExamSeasonDto dto) {
        if (dto.getStartDate() == null || dto.getEndDate() == null) {
            throw new IllegalArgumentException("Datat e fillimit dhe mbarimit janë të detyrueshme!");
        }

        LocalDate start = LocalDate.parse(dto.getStartDate());
        LocalDate end = LocalDate.parse(dto.getEndDate());
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Data e fillimit nuk mund të jetë pas datës së mbarimit!");
        }

        String year = (dto.getAcademicYear() != null && !dto.getAcademicYear().isBlank()) ? dto.getAcademicYear().trim() : "2025-2026";
        String type = (dto.getSeasonType() != null && !dto.getSeasonType().isBlank()) ? dto.getSeasonType().toUpperCase().trim() : "VJESHTE";

        ExamSeason es;
        if (dto.getProgramId() != null) {
            es = examSeasonRepo.findByAcademicYearAndSeasonTypeAndProgram_ProgramId(year, type, dto.getProgramId())
                    .orElse(new ExamSeason());
            if (es.getProgram() == null) {
                es.setProgram(programRepo.findById(dto.getProgramId()).orElse(null));
            }
        } else {
            es = examSeasonRepo.findByAcademicYearAndSeasonTypeAndProgramIsNull(year, type)
                    .orElse(new ExamSeason());
        }

        es.setAcademicYear(year);
        es.setSeasonType(type);
        es.setStartDate(start);
        es.setEndDate(end);

        ExamSeason saved = examSeasonRepo.save(es);
        return Savina.ftiApp.dto.responseDTO.ExamSeasonDto.builder()
                .seasonId(saved.getSeasonId())
                .academicYear(saved.getAcademicYear())
                .seasonType(saved.getSeasonType())
                .programId(saved.getProgram() != null ? saved.getProgram().getProgramId() : null)
                .startDate(saved.getStartDate().toString())
                .endDate(saved.getEndDate().toString())
                .build();
    }
}
