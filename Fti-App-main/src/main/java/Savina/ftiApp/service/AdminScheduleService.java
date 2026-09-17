package Savina.ftiApp.service;

import Savina.ftiApp.dto.requestDTO.ScheduleRequest;
import Savina.ftiApp.dto.responseDTO.RoomAvailabilityDto;
import Savina.ftiApp.dto.responseDTO.ScheduleResponseDto;
import Savina.ftiApp.entity.*;
import Savina.ftiApp.repository.*;
import lombok.RequiredArgsConstructor;
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
public class AdminScheduleService {

    private final DepartmentRepository departmentRepository;
    private final ProgramRepository programRepository;
    private final CourseRepository courseRepository;
    private final TeachingCourseRepository teachingCourseRepository;
    private final ProfessorRepository professorRepository;
    private final ClassesRepository classesRepository;
    private final RoomRepository roomRepository;
    private final CourseTypeRepository courseTypeRepository;
    private final CourseScheduleRepository courseScheduleRepository;

    private static final LocalDate BASE_DATE = LocalDate.of(2026, 1, 1);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDepartments() {
        return departmentRepository.findAll().stream().map(d -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", d.getDepartmentId());
            map.put("name", d.getEmerDepartamenti());
            return map;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getLevels(Integer departmentId) {
        if (departmentId != null) {
            List<String> list = programRepository.findDistinctNiveleByDepartment(departmentId);
            if (!list.isEmpty()) return list;
        }
        return programRepository.findDistinctNivele();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getBranches(Integer departmentId, String nivel) {
        List<Program> progs;
        if (departmentId != null && nivel != null && !nivel.isBlank()) {
            progs = programRepository.findByDepartmentDepartmentIdAndNivelIgnoreCase(departmentId, nivel);
            if (progs.isEmpty()) {
                progs = programRepository.findByDepartmentDepartmentId(departmentId);
            }
        } else if (departmentId != null) {
            progs = programRepository.findByDepartmentDepartmentId(departmentId);
        } else if (nivel != null && !nivel.isBlank()) {
            progs = programRepository.findAll().stream()
                    .filter(p -> p.getNivel() != null && p.getNivel().equalsIgnoreCase(nivel))
                    .collect(Collectors.toList());
        } else {
            progs = programRepository.findAll();
        }

        return progs.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("programId", p.getProgramId());
            map.put("name", p.getSpecializimi());
            map.put("nivel", p.getNivel());
            map.put("diplomeDyfishte", p.getDiplomeDyfishte());
            return map;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Integer> getYears(Integer programId) {
        if (programId != null) {
            Optional<Program> pOpt = programRepository.findById(programId);
            if (pOpt.isPresent()) {
                String nivel = pOpt.get().getNivel();
                if (nivel != null && nivel.toLowerCase().contains("master")) {
                    return List.of(1, 2);
                }
                return List.of(1, 2, 3);
            }
        }
        return List.of(1, 2, 3);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getCourses(Integer programId, Integer studyYear) {
        List<Course> courses;
        if (programId != null && studyYear != null) {
            courses = courseRepository.findByProgramProgramIdAndStudyYear(programId, studyYear);
            if (courses.isEmpty()) {
                courses = courseRepository.findByProgramProgramId(programId);
            }
        } else if (programId != null) {
            courses = courseRepository.findByProgramProgramId(programId);
        } else {
            courses = courseRepository.findAll();
        }

        return courses.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("courseId", c.getCourseId());
            map.put("name", c.getEmriCourse());
            map.put("credits", c.getKredite());
            map.put("studyYear", c.getStudyYear());
            return map;
        }).collect(Collectors.toList());
    }

    public String determineCourseSemester(Course c) {
        if (c == null) return "1";
        if (c.getSemester() != null && !c.getSemester().isBlank()) {
            return c.getSemester().trim();
        }

        String name = c.getEmriCourse() != null ? c.getEmriCourse().toLowerCase() : "";

        if (name.contains("analize matematike 2") || name.contains("analize matematike 2") ||
            name.contains("fizike 2") || name.contains("fizike 2") ||
            name.contains("elektroteknik") ||
            name.contains("analize matematike 3") || name.contains("analize matematike 3") ||
            name.contains("teknikat dhe gjuhet") || name.contains("teknikat dhe gjuhet") ||
            name.contains("probabilitet") ||
            name.contains("gjuhe e huaj") || name.contains("gjuhe e huaj") ||
            name.contains("sistemet elektronike") || name.contains("sisteme elektronike") ||
            name.contains("perpunimi numerik") || name.contains("perpunimi numerik") || name.contains("perpunim numerik") ||
            name.contains("arkitekture") || name.contains("arkitekture") ||
            name.contains("strukture te dhenash") || name.contains("strukture te dhenash") ||
            name.contains("ekonomi dhe menaxhim") ||
            name.contains("rrjetat e kompjuterave") ||
            name.contains("python") ||
            name.contains("shperndara") || name.contains("shperndara") ||
            name.contains("legjislacion") ||
            name.contains("praktik") ||
            name.contains("diplom") ||
            name.contains("rrjetet optike") ||
            name.contains("sensoret") || name.contains("sensoret") ||
            name.contains("elektronika per telekom") || name.contains("elektronika per telekom")) {
            return "2";
        }

        if (name.contains("analize matematike 1") || name.contains("analize matematike 1") ||
            name.contains("fizike 1") || name.contains("fizike 1") ||
            name.contains("elementet e informatikes") || name.contains("elementet e informatikes") ||
            name.contains("algjeber") || name.contains("algjeber") ||
            name.contains("shkrim dhe prezantim") ||
            name.contains("analize numerike") || name.contains("analize numerike") ||
            name.contains("orientuar nga objekti") ||
            name.contains("teoria e sinjaleve") ||
            name.contains("bazat e te dhenave") || name.contains("bazat e te dhenave") ||
            name.contains("automatizim") ||
            name.contains("teknologjite elektronike") || name.contains("teknologjite elektronike") ||
            name.contains("algoritmik") ||
            name.contains("web") ||
            name.contains("inxhinieri softi") ||
            name.contains("sistemet operative") ||
            name.contains("kibernetike") ||
            name.contains("fuqie") ||
            name.contains("analoge") ||
            name.contains("radiofrekuenc")) {
            return "1";
        }

        return "1";
    }

    @Transactional(readOnly = true)
    public List<String> getCourseTypes() {
        List<String> types = new ArrayList<>();
        types.add("LEKSION");
        types.add("SEMINAR");
        types.add("LABORATOR");
        return types;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getProfessorsForCourseAndType(Integer courseId, String roleType) {
        Map<String, Object> response = new HashMap<>();
        if (courseId == null) {
            response.put("defaultProfessor", null);
            response.put("professors", Collections.emptyList());
            return response;
        }

        List<TeachingCourse> allocations = teachingCourseRepository.findByCourseCourseId(courseId);
        List<Map<String, Object>> profList = new ArrayList<>();
        Map<String, Object> defaultProf = null;

        String targetRole = (roleType != null && !roleType.isBlank()) ? roleType.trim().toUpperCase() : "LEKSION";

        for (TeachingCourse tc : allocations) {
            Professor prof = tc.getProfessor();
            if (prof != null) {
                Map<String, Object> pMap = new HashMap<>();
                pMap.put("professorId", prof.getProfessorId());
                pMap.put("name", getProfessorFullName(prof));
                pMap.put("roleType", tc.getRoleType());

                profList.add(pMap);

                if (defaultProf == null && tc.getRoleType() != null && tc.getRoleType().equalsIgnoreCase(targetRole)) {
                    defaultProf = pMap;
                }
            }
        }

        if (defaultProf == null && !profList.isEmpty()) {
            defaultProf = profList.get(0);
        }

        response.put("defaultProfessor", defaultProf);
        response.put("professors", profList);
        return response;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getClasses(Integer programId, Integer studyYear, Integer courseId) {
        List<Classes> list = new ArrayList<>();

        if (programId != null) {
            if (studyYear != null) {
                list = classesRepository.findByProgram_ProgramIdAndVitStudimit(programId, studyYear);
            }
            if (list == null || list.isEmpty()) {
                list = classesRepository.findByProgram_ProgramId(programId);
                if (studyYear != null && list != null && !list.isEmpty()) {
                    list = list.stream()
                            .filter(c -> c.getVitStudimit() == null || c.getVitStudimit().equals(studyYear))
                            .collect(Collectors.toList());
                }
            }

            if (list == null || list.isEmpty()) {
                return Collections.emptyList();
            }
        } else if (courseId != null) {
            Optional<Course> cOpt = courseRepository.findById(courseId);
            if (cOpt.isPresent() && cOpt.get().getProgram() != null) {
                return getClasses(cOpt.get().getProgram().getProgramId(), cOpt.get().getStudyYear(), null);
            } else {
                return Collections.emptyList();
            }
        } else {
            list = classesRepository.findAll();
        }

        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Classes> unique = new LinkedHashMap<>();
        for (Classes c : list) {
            if (c != null && c.getEmriClass() != null && !c.getEmriClass().isBlank()) {
                unique.putIfAbsent(c.getEmriClass().trim(), c);
            }
        }

        return unique.values().stream()
                .sorted(Comparator.comparing(c -> c.getEmriClass() != null ? c.getEmriClass() : ""))
                .map(c -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("classId", c.getClassId());
                    map.put("name", c.getEmriClass());
                    map.put("studyYear", c.getVitStudimit());
                    return map;
                }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRooms() {
        return roomRepository.findAllByOrderByRoomNameAsc().stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("roomId", r.getRoomId());
            map.put("roomName", r.getRoomName());
            map.put("capacity", r.getCapacity());
            return map;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RoomAvailabilityDto checkRoomAvailability(Integer roomId, Integer professorId, String dayOfWeek,
                                                     String startTimeStr, String endTimeStr,
                                                     Integer excludeScheduleId) {
        if (dayOfWeek == null || startTimeStr == null || endTimeStr == null ||
                dayOfWeek.isBlank() || startTimeStr.isBlank() || endTimeStr.isBlank()) {
            return RoomAvailabilityDto.builder()
                    .available(true)
                    .message("")
                    .build();
        }

        String sTime = startTimeStr.trim();
        String eTime = endTimeStr.trim();

        if (eTime.compareTo(sTime) <= 0) {
            return RoomAvailabilityDto.builder()
                    .available(false)
                    .message("Ora e mbarimit duhet te jete pas ores se fillimit.")
                    .build();
        }

        boolean roomAvailable = true;
        String roomConflictMsg = null;
        boolean professorAvailable = true;
        String professorConflictMsg = null;

        String dayKey = extractDayKey(dayOfWeek);

        if (roomId != null) {
            Optional<Room> rOpt = roomRepository.findById(roomId);
            if (rOpt.isPresent()) {
                Room room = rOpt.get();
                List<CourseSchedule> overlaps = courseScheduleRepository.findOverlappingRoomSchedules(
                        roomId, dayOfWeek.trim(), dayKey, sTime, eTime, excludeScheduleId
                );

                if (!overlaps.isEmpty()) {
                    roomAvailable = false;
                    CourseSchedule conflict = overlaps.get(0);
                    String conflictTime = conflict.getStartTime() + " - " + conflict.getEndTime();
                    String targetInfo = "";
                    if (conflict.getClasses() != null) {
                        String prog = (conflict.getClasses().getProgram() != null) ? conflict.getClasses().getProgram().getSpecializimi() : "";
                        Integer vit = conflict.getClasses().getVitStudimit();
                        String emriCls = conflict.getClasses().getEmriClass();
                        if (prog != null && !prog.isBlank()) {
                            targetInfo = prog + (vit != null ? " Viti " + vit : "") + (emriCls != null && !emriCls.isBlank() ? " (" + emriCls + ")" : "");
                        } else {
                            targetInfo = (emriCls != null) ? emriCls : "";
                        }
                    }
                    roomConflictMsg = "Salla " + room.getRoomName() + " eshte e zene ne kete orar (" + conflictTime + ")"
                            + (targetInfo.isEmpty() ? "" : " nga " + targetInfo);
                }
            }
        }

        if (professorId != null) {
            Optional<Professor> pOpt = professorRepository.findById(professorId);
            if (pOpt.isPresent()) {
                List<CourseSchedule> profOverlaps = courseScheduleRepository.findOverlappingProfessorSchedules(
                        professorId, dayOfWeek.trim(), dayKey, sTime, eTime, excludeScheduleId
                );

                if (!profOverlaps.isEmpty()) {
                    professorAvailable = false;
                    CourseSchedule conflict = profOverlaps.get(0);
                    String conflictTime = conflict.getStartTime() + " - " + conflict.getEndTime();
                    String targetInfo = "";
                    if (conflict.getClasses() != null) {
                        String prog = (conflict.getClasses().getProgram() != null) ? conflict.getClasses().getProgram().getSpecializimi() : "";
                        Integer vit = conflict.getClasses().getVitStudimit();
                        String emriCls = conflict.getClasses().getEmriClass();
                        if (prog != null && !prog.isBlank()) {
                            targetInfo = prog + (vit != null ? " Viti " + vit : "") + (emriCls != null && !emriCls.isBlank() ? " (" + emriCls + ")" : "");
                        } else {
                            targetInfo = (emriCls != null) ? emriCls : "";
                        }
                    }
                    professorConflictMsg = "Mesim ne kete orar (" + conflictTime + ")"
                            + (targetInfo.isEmpty() ? "" : " me " + targetInfo);
                }
            }
        }

        boolean overallAvailable = roomAvailable && professorAvailable;
        String combinedMsg = (!professorAvailable) ? professorConflictMsg : ((!roomAvailable) ? roomConflictMsg : "");

        return RoomAvailabilityDto.builder()
                .available(overallAvailable)
                .roomId(roomId)
                .roomAvailable(roomAvailable)
                .roomConflictMessage(roomConflictMsg)
                .professorAvailable(professorAvailable)
                .professorConflictMessage(professorConflictMsg)
                .message(combinedMsg)
                .build();
    }

    @Transactional
    public ScheduleResponseDto saveSchedule(ScheduleRequest req) {
        if (req.getCourseId() == null) throw new RuntimeException("Lenda eshte e detyrueshme.");
        if (req.getRoomId() == null) throw new RuntimeException("Salla/Klasa eshte e detyrueshme.");
        if (req.getDayOfWeek() == null || req.getDayOfWeek().isBlank()) throw new RuntimeException("Dita eshte e detyrueshme.");
        String sTime = req.getStartTime().trim();
        String eTime = req.getEndTime().trim();

        if (eTime.compareTo(sTime) <= 0) {
            throw new RuntimeException("Ora e mbarimit duhet te jete me e madhe se ora e fillimit.");
        }

        String dayKey = extractDayKey(req.getDayOfWeek());

        List<CourseSchedule> overlaps = courseScheduleRepository.findOverlappingRoomSchedules(
                req.getRoomId(), req.getDayOfWeek().trim(), dayKey, sTime, eTime, req.getScheduleId()
        );
        if (!overlaps.isEmpty()) {
            Room r = roomRepository.findById(req.getRoomId()).orElse(null);
            String rName = r != null ? r.getRoomName() : "Salla";
            throw new RuntimeException(rName + " eshte e zene ne kete orar! Ju lutem zgjidhni nje salle tjeter ose ndryshoni orarin.");
        }

        if (req.getProfessorId() != null) {
            List<CourseSchedule> profOverlaps = courseScheduleRepository.findOverlappingProfessorSchedules(
                    req.getProfessorId(), req.getDayOfWeek().trim(), dayKey, sTime, eTime, req.getScheduleId()
            );
            if (!profOverlaps.isEmpty()) {
                CourseSchedule c = profOverlaps.get(0);
                String cName = c.getClasses() != null ? c.getClasses().getEmriClass() : "";
                throw new RuntimeException("Pedagogu ka mesim ne kete orar me klasen " + cName + "!");
            }
        }

        Course course = courseRepository.findById(req.getCourseId())
                .orElseThrow(() -> new RuntimeException("Lenda nuk u gjet."));

        TeachingCourse tc = null;
        if (req.getProfessorId() != null) {
            Professor prof = professorRepository.findById(req.getProfessorId()).orElse(null);
            if (prof != null) {
                List<TeachingCourse> existing = teachingCourseRepository.findByCourseCourseId(course.getCourseId());
                for (TeachingCourse t : existing) {
                    if (t.getProfessor() != null && t.getProfessor().getProfessorId().equals(prof.getProfessorId())) {
                        tc = t;
                        break;
                    }
                }
                if (tc == null) {
                    tc = TeachingCourse.builder()
                            .course(course)
                            .professor(prof)
                            .roleType(req.getRoleType() != null ? req.getRoleType() : "LEKSION")
                            .build();
                    tc = teachingCourseRepository.save(tc);
                }
            }
        }

        if (tc == null) {
            List<TeachingCourse> existing = teachingCourseRepository.findByCourseCourseId(course.getCourseId());
            if (!existing.isEmpty()) {
                tc = existing.get(0);
            } else {
                Professor fallbackProf = professorRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> new RuntimeException("Nuk ka asnje pedagog te regjistruar ne sistem."));
                tc = TeachingCourse.builder()
                        .course(course)
                        .professor(fallbackProf)
                        .roleType(req.getRoleType() != null ? req.getRoleType() : "LEKSION")
                        .build();
                tc = teachingCourseRepository.save(tc);
            }
        }

        Classes cls = null;
        if (req.getClassId() != null) {
            cls = classesRepository.findById(req.getClassId()).orElse(null);
        }
        if (cls == null && course.getProgram() != null) {
            List<Classes> progClasses = classesRepository.findByProgram_ProgramId(course.getProgram().getProgramId());
            if (!progClasses.isEmpty()) {
                cls = progClasses.get(0);
            } else {
                cls = classesRepository.save(Classes.builder()
                        .program(course.getProgram())
                        .vitStudimit(course.getStudyYear() != null ? course.getStudyYear() : 1)
                        .emriClass("Grupi A")
                        .build());
            }
        }

        Room room = roomRepository.findById(req.getRoomId())
                .orElseThrow(() -> new RuntimeException("Salla nuk u gjet."));

        CourseSchedule schedule;
        if (req.getScheduleId() != null) {
            schedule = courseScheduleRepository.findById(req.getScheduleId())
                    .orElseThrow(() -> new RuntimeException("Orari nuk u gjet per perditesim."));
        } else {
            schedule = new CourseSchedule();
        }

        schedule.setTeachingCourse(tc);
        schedule.setClasses(cls);
        schedule.setDayOfWeek(req.getDayOfWeek().trim());
        schedule.setStartTime(sTime);
        schedule.setEndTime(eTime);
        schedule.setRoom(room);
        schedule.setAcademicYear(req.getAcademicYear() != null && !req.getAcademicYear().isBlank() ? req.getAcademicYear().trim() : "2025-2026");

        CourseSchedule saved = courseScheduleRepository.save(schedule);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponseDto> getSchedules(Integer programId, Integer studyYear) {
        List<CourseSchedule> list;
        if (programId != null) {
            list = courseScheduleRepository.findByProgramAndStudyYear(programId, studyYear);
        } else {
            list = courseScheduleRepository.findAll();
        }
        return list.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional
    public void deleteSchedule(Integer scheduleId) {
        if (!courseScheduleRepository.existsById(scheduleId)) {
            throw new RuntimeException("Orari nuk ekziston.");
        }
        courseScheduleRepository.deleteById(scheduleId);
    }

    private ScheduleResponseDto mapToDto(CourseSchedule cs) {
        String courseName = (cs.getTeachingCourse() != null && cs.getTeachingCourse().getCourse() != null)
                ? cs.getTeachingCourse().getCourse().getEmriCourse() : "";
        Integer courseId = (cs.getTeachingCourse() != null && cs.getTeachingCourse().getCourse() != null)
                ? cs.getTeachingCourse().getCourse().getCourseId() : null;

        String profName = "";
        Integer profId = null;
        if (cs.getTeachingCourse() != null && cs.getTeachingCourse().getProfessor() != null) {
            profId = cs.getTeachingCourse().getProfessor().getProfessorId();
            profName = getProfessorFullName(cs.getTeachingCourse().getProfessor());
        }

        String roleType = (cs.getTeachingCourse() != null && cs.getTeachingCourse().getRoleType() != null)
                ? cs.getTeachingCourse().getRoleType() : "LEKSION";

        String className = cs.getClasses() != null ? cs.getClasses().getEmriClass() : "";
        Integer classId = cs.getClasses() != null ? cs.getClasses().getClassId() : null;
        Integer studyYear = cs.getClasses() != null ? cs.getClasses().getVitStudimit() : null;
        String progName = (cs.getClasses() != null && cs.getClasses().getProgram() != null)
                ? cs.getClasses().getProgram().getSpecializimi() : "";

        String roomName = cs.getRoom() != null ? cs.getRoom().getRoomName() : "";
        Integer roomId = cs.getRoom() != null ? cs.getRoom().getRoomId() : null;

        String sTime = cs.getStartTime() != null ? cs.getStartTime() : "";
        String eTime = cs.getEndTime() != null ? cs.getEndTime() : "";

        String semesterVal = (cs.getTeachingCourse() != null && cs.getTeachingCourse().getCourse() != null && cs.getTeachingCourse().getCourse().getSemester() != null)
                ? cs.getTeachingCourse().getCourse().getSemester() : "";

        return ScheduleResponseDto.builder()
                .scheduleId(cs.getScheduleId())
                .courseId(courseId)
                .courseName(courseName)
                .professorId(profId)
                .professorName(profName.trim())
                .roleType(roleType)
                .classId(classId)
                .className(className)
                .programName(progName)
                .studyYear(studyYear)
                .dayOfWeek(cs.getDayOfWeek())
                .startTime(sTime)
                .endTime(eTime)
                .roomId(roomId)
                .roomName(roomName)
                .semester(semesterVal)
                .academicYear(cs.getAcademicYear())
                .build();
    }

    private String getProfessorFullName(Professor prof) {
        if (prof == null) return "Pa pedagog";
        if (prof.getUser() != null) {
            String e = prof.getUser().getEmri() != null ? prof.getUser().getEmri() : "";
            String m = prof.getUser().getMbiemri() != null ? prof.getUser().getMbiemri() : "";
            String full = (e + " " + m).trim();
            if (!full.isEmpty()) return full;
        }
        return "Pedagog #" + prof.getProfessorId();
    }

    private String extractDayKey(String day) {
        if (day == null) return "";
        String d = day.trim().toLowerCase();
        if (d.contains("hen") || d.contains("hën")) return "hen";
        if (d.contains("mart")) return "mart";
        if (d.contains("merkur") || d.contains("mërkur")) return "merkur";
        if (d.contains("enjt")) return "enjt";
        if (d.contains("premt")) return "premt";
        if (d.contains("shtun")) return "shtun";
        if (d.contains("djel")) return "djel";
        return d;
    }
}
