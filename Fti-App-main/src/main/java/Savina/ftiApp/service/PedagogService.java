package Savina.ftiApp.service;

import Savina.ftiApp.dto.responseDTO.BranchStatsDto;
import Savina.ftiApp.dto.responseDTO.PedagogOptionsDto;
import Savina.ftiApp.dto.responseDTO.PedagogRegisterDto;
import Savina.ftiApp.dto.requestDTO.SaveAttendanceRequest;
import Savina.ftiApp.dto.requestDTO.SaveGradesRequest;
import Savina.ftiApp.entity.*;
import Savina.ftiApp.mapper.PedagogMapper;
import Savina.ftiApp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PedagogService {

    private final UserRepository userRepo;
    private final ProfessorRepository professorRepo;
    private final TeachingCourseRepository teachingCourseRepo;
    private final CourseRepository courseRepo;
    private final ClassesRepository classesRepo;
    private final DepartmentRepository departmentRepo;
    private final CourseScheduleRepository courseScheduleRepo;
    private final StudentRepository studentRepo;
    private final GradeRepository gradeRepo;
    private final AttendanceRepository attendanceRepo;
    private final TopicRepository topicRepo;
    private final ProgramRepository programRepo;
    private final PedagogMapper pedagogMapper;

    @Transactional(readOnly = true)
    public PedagogOptionsDto getPedagogOptions(Integer userId, String email) {
        log.info("[getPedagogOptions] Filloi kërkimi për userId={}, email='{}'", userId, email);

        // 1. Gjejmë User në DB
        User user = null;
        if (userId != null) {
            user = userRepo.findById(userId).orElse(null);
        }
        if (user == null && email != null && !email.isBlank()) {
            String cleanEmail = email.trim().toLowerCase();
            user = userRepo.findByEmailIgnoreCase(cleanEmail)
                    .orElseGet(() -> userRepo.findByEmail(cleanEmail).orElse(null));
        }

        if (user != null) {
            log.info("[getPedagogOptions] U gjet User: ID={}, Email='{}', Emri='{}', Mbiemri='{}'",
                    user.getUserId(), user.getEmail(), user.getEmri(), user.getMbiemri());
        } else {
            log.warn("[getPedagogOptions] Nuk u gjet asnjë User në tabelën USERS për userId={}, email='{}'", userId, email);
        }

        // 2. Gjejmë Professor në DB
        Professor professor = null;
        if (user != null) {
            final Integer targetUserId = user.getUserId();
            professor = professorRepo.findProfessorByUserId(targetUserId)
                    .orElseGet(() -> professorRepo.findByUserUserId(targetUserId).orElse(null));
        }
        if (professor == null && email != null && !email.isBlank()) {
            final String cleanEmail = email.trim().toLowerCase();
            professor = professorRepo.findProfessorByUserEmail(cleanEmail)
                    .orElseGet(() -> professorRepo.findByUserEmailIgnoreCase(cleanEmail).orElse(null));
        }


        // Kërkim fallback në tabelën PROFESSORS duke krahasuar çdo rekord
        if (professor == null) {
            List<Professor> allProfessors = professorRepo.findAll();
            for (Professor p : allProfessors) {
                if (p.getUser() != null) {
                    if (user != null && p.getUser().getUserId() != null && p.getUser().getUserId().equals(user.getUserId())) {
                        professor = p;
                        break;
                    }
                    if (email != null && p.getUser().getEmail() != null && p.getUser().getEmail().equalsIgnoreCase(email.trim())) {
                        professor = p;
                        break;
                    }
                }
            }
        }
        if (professor == null && user != null) {
            log.info("[getPedagogOptions] Po krijohet automatikisht rekordi në PROFESSORS për User: ID={}, Email='{}'",
                    user.getUserId(), user.getEmail());
            Department defaultDept = departmentRepo.findAll().stream().findFirst().orElse(null);
            professor = Professor.builder()
                    .user(user)
                    .status("A")
                    .department(defaultDept)
                    .build();
            professor = professorRepo.save(professor);
            log.info("[getPedagogOptions] U krijua me sukses rekordi në PROFESSORS me ID={} për User ID={}",
                    professor.getProfessorId(), user.getUserId());
        }

        if (professor != null) {
            log.info("[getPedagogOptions] U gjet/krijua Professor: ID={}, User_ID={}",
                    professor.getProfessorId(), professor.getUser() != null ? professor.getUser().getUserId() : "null");
        } else {
            log.error("[getPedagogOptions] GABIM: Nuk u gjet asnjë User dhe asnjë Professor për email='{}', userId={}", email, userId);
        }

        // 3. Përcaktojmë emrin dhe email-in e pedagogut
        String profName = "";
        String profEmail = "";

        if (user != null) {
            profName = formatProfFullName(user.getEmri(), user.getMbiemri(), user.getEmail());
            profEmail = user.getEmail() != null ? user.getEmail().trim() : "";
        }
        if ((profName.isEmpty() || "Profesor".equals(profName)) && professor != null && professor.getUser() != null) {
            profName = formatProfFullName(professor.getUser().getEmri(), professor.getUser().getMbiemri(), professor.getUser().getEmail());
            profEmail = professor.getUser().getEmail() != null ? professor.getUser().getEmail().trim() : "";
        }
        if ((profName.isEmpty() || "Profesor".equals(profName)) && email != null && !email.isBlank()) {
            profName = formatProfFullName(null, null, email);
        }
        if (profName.isEmpty()) {
            profName = "Profesor";
        }

        log.info("[getPedagogOptions] Emri përfundimtar i pedagogut: '{}' (email: '{}')", profName, profEmail);

        // 4. Marrim VETËM lëndët e caktuara për këtë pedagog nga TEACHING_COURSES
        List<TeachingCourse> teachingCourses = Collections.emptyList();
        if (professor != null) {
            try {
                teachingCourses = teachingCourseRepo.findTeachingCoursesByProfessorId(professor.getProfessorId());
            } catch (Exception ex) {
                teachingCourses = teachingCourseRepo.findByProfessorProfessorId(professor.getProfessorId());
            }
            log.info("[getPedagogOptions] Pedagogu ID={} ka gjithsej {} lëndë/orë në TEACHING_COURSES",
                    professor.getProfessorId(), teachingCourses.size());
        }

        // Vitet akademike
        List<String> academicYears = courseScheduleRepo.findDistinctAcademicYears();
        if (academicYears == null || academicYears.isEmpty()) {
            int currentYear = LocalDate.now().getMonthValue() >= 9
                    ? LocalDate.now().getYear()
                    : LocalDate.now().getYear() - 1;
            academicYears = Arrays.asList(
                    (currentYear) + "-" + (currentYear + 1),
                    (currentYear - 1) + "-" + currentYear,
                    (currentYear + 1) + "-" + (currentYear + 2)
            );
        }

        List<PedagogOptionsDto.DepartmentOptionDto> departmentOptions = new ArrayList<>();
        List<PedagogOptionsDto.CourseOptionDto> courseOptions = new ArrayList<>();
        List<PedagogOptionsDto.ClassOptionDto> classOptions = new ArrayList<>();
        Set<String> typesSet = new LinkedHashSet<>();

        Set<Integer> seenDeptIds = new HashSet<>();
        Set<Integer> seenCourseIds = new HashSet<>();
        Set<String> seenClassKeys = new HashSet<>();

        boolean isLektor = false;
        boolean hasRegistry = false;

        // Mbushim VETËM të dhënat e këtij pedagogu
        Map<Integer, Set<String>> courseBranchesMap = new HashMap<>();
        Map<Integer, Set<Program>> courseProgramsMap = new HashMap<>();

        if (!teachingCourses.isEmpty()) {
            for (TeachingCourse tc : teachingCourses) {
                String role = tc.getRoleType() != null ? tc.getRoleType().trim().toUpperCase() : "";

                if (role.contains("LEK") || role.contains("LEKTOR")) {
                    isLektor = true;
                }
                if (role.contains("SEM")) {
                    typesSet.add("Seminar");
                    hasRegistry = true;
                }
                if (role.contains("LAB")) {
                    typesSet.add("Laborator");
                    hasRegistry = true;
                }

                if (tc.getCourse() != null) {
                    Course c = tc.getCourse();

                    Department dept = (c.getProgram() != null) ? c.getProgram().getDepartment() : null;
                    if (dept == null && c.getDepartments() != null && !c.getDepartments().isEmpty()) {
                        dept = c.getDepartments().iterator().next();
                    }
                    if (dept == null && professor.getDepartment() != null) {
                        dept = professor.getDepartment();
                    }

                    if (dept != null && dept.getDepartmentId() != null && !seenDeptIds.contains(dept.getDepartmentId())) {
                        seenDeptIds.add(dept.getDepartmentId());
                        departmentOptions.add(pedagogMapper.buildDepartmentOption(dept));
                    }

                    if (c.getProgram() != null) {
                        courseProgramsMap.computeIfAbsent(c.getCourseId(), k -> new LinkedHashSet<>()).add(c.getProgram());
                        if (c.getProgram().getSpecializimi() != null) {
                            courseBranchesMap.computeIfAbsent(c.getCourseId(), k -> new LinkedHashSet<>()).add(c.getProgram().getSpecializimi().trim());
                        }
                    }

                    if (c.getCourseId() != null && !seenCourseIds.contains(c.getCourseId())) {
                        seenCourseIds.add(c.getCourseId());
                        PedagogOptionsDto.CourseOptionDto cDto = pedagogMapper.buildCourseOption(c);
                        courseOptions.add(cDto);
                    }

                    if (tc.getClasses() != null && !tc.getClasses().isEmpty()) {
                        for (Classes cl : tc.getClasses()) {
                            if (cl.getProgram() != null) {
                                courseProgramsMap.computeIfAbsent(c.getCourseId(), k -> new LinkedHashSet<>()).add(cl.getProgram());
                                if (cl.getProgram().getSpecializimi() != null) {
                                    courseBranchesMap.computeIfAbsent(c.getCourseId(), k -> new LinkedHashSet<>()).add(cl.getProgram().getSpecializimi().trim());
                                }
                            }
                            if (cl.getClassId() != null) {
                                String classKey = cl.getClassId() + "_" + c.getCourseId() + "_" + role;
                                if (!seenClassKeys.contains(classKey)) {
                                    seenClassKeys.add(classKey);
                                    PedagogOptionsDto.ClassOptionDto clDto = pedagogMapper.buildClassOption(cl, c.getCourseId());
                                    clDto.setRoleType(role);
                                    classOptions.add(clDto);
                                }
                            }
                        }
                    }
                }
            }

            // Vendosim branches reale ne cdo CourseOptionDto
            for (PedagogOptionsDto.CourseOptionDto cDto : courseOptions) {
                Set<String> brs = courseBranchesMap.get(cDto.getId());
                if (brs != null && !brs.isEmpty()) {
                    cDto.setBranches(new ArrayList<>(brs));
                    if (cDto.getProgramName() == null || cDto.getProgramName().isBlank()) {
                        cDto.setProgramName(brs.iterator().next());
                    }
                } else if (cDto.getProgramName() != null && !cDto.getProgramName().isBlank()) {
                    cDto.setBranches(List.of(cDto.getProgramName()));
                }
            }
        } else {
            log.warn("[getPedagogOptions] Pedagogu '{}' nuk ka asnjë lëndë të caktuar në tabelën TEACHING_COURSES.", profName);
        }

        // Nëse departamenti mungon nga kurset por profesori ka departament të caktuar
        if (departmentOptions.isEmpty() && professor != null && professor.getDepartment() != null) {
            Department d = professor.getDepartment();
            if (d.getDepartmentId() != null && !seenDeptIds.contains(d.getDepartmentId())) {
                seenDeptIds.add(d.getDepartmentId());
                departmentOptions.add(pedagogMapper.buildDepartmentOption(d));
            }
        }

        List<String> typesList = new ArrayList<>(typesSet);

        // Programet reale të këtij pedagogu
        List<PedagogOptionsDto.ProgramOptionDto> programOptions = new ArrayList<>();
        Set<Integer> seenProgIds = new HashSet<>();
        for (Set<Program> progs : courseProgramsMap.values()) {
            for (Program p : progs) {
                if (p.getProgramId() != null && !seenProgIds.contains(p.getProgramId())) {
                    seenProgIds.add(p.getProgramId());
                    programOptions.add(pedagogMapper.buildProgramOption(p));
                }
            }
        }
        if (programOptions.isEmpty()) {
            List<Program> allPrograms = programRepo.findAll();
            for (Program p : allPrograms) {
                programOptions.add(pedagogMapper.buildProgramOption(p));
            }
        }

        log.info("[getPedagogOptions] Përfundoi: Kurse={}, Tipa={}, Departamente={}, Klasa={}, isLektor={}, hasRegistry={}",
                courseOptions.size(), typesList, departmentOptions.size(), classOptions.size(), isLektor, hasRegistry);

        return PedagogOptionsDto.builder()
                .professorName(profName)
                .professorEmail(profEmail)
                .academicYears(academicYears)
                .types(typesList)
                .departments(departmentOptions)
                .courses(courseOptions)
                .classes(classOptions)
                .programs(programOptions)
                .isLektor(isLektor)
                .hasRegistry(hasRegistry)
                .build();
    }

    @Transactional(readOnly = true)
    public PedagogRegisterDto getRegisterData(Integer courseId, Integer classId, String academicYear, String roleType) {
        Course course = courseId != null ? courseRepo.findById(courseId).orElse(null) : null;
        Classes classes = classId != null ? classesRepo.findById(classId).orElse(null) : null;

        List<Student> students = new ArrayList<>();
        if (classId != null) {
            students = studentRepo.findByClasses_ClassId(classId);
        }
        if (students.isEmpty() && course != null && course.getProgram() != null) {
            students = studentRepo.findByProgram_ProgramId(course.getProgram().getProgramId());
        }
        if (students.isEmpty()) {
            students = studentRepo.findAll();
            if (students.size() > 15) {
                students = students.subList(0, 15);
            }
        }

        // Attendance columns derived dynamically from the Topics of this course
        List<Topic> courseTopics = (courseId != null)
                ? topicRepo.findByTeachingCourseCourseCourseIdOrderByWeekNumberAsc(courseId)
                : Collections.emptyList();

        List<PedagogRegisterDto.AttendanceColumnDto> columns = new ArrayList<>();
        for (Topic t : courseTopics) {
            String dStr = t.getTopicDate() != null ? t.getTopicDate().toString() : "";
            String title = "Jave " + (t.getWeekNumber() != null ? t.getWeekNumber() : "");
            if (!dStr.isEmpty()) {
                title += " (" + dStr + ")";
            }
            columns.add(PedagogRegisterDto.AttendanceColumnDto.builder()
                    .key("topic_" + t.getTopicId())
                    .title(title)
                    .date(dStr)
                    .build());
        }

        List<Attendance> existingAttendances = (courseId != null)
                ? attendanceRepo.findByTeachingCourse_Course_CourseId(courseId)
                : Collections.emptyList();

        Map<String, Attendance> attMapByKey = new HashMap<>();
        for (Attendance a : existingAttendances) {
            if (a.getStudent() != null && a.getDataAttendance() != null) {
                String key = a.getStudent().getStudentId() + "_" + a.getDataAttendance().toString();
                attMapByKey.put(key, a);
            }
        }

        List<Grade> existingGrades = (courseId != null)
                ? gradeRepo.findByTeachingCourse_Course_CourseId(courseId)
                : Collections.emptyList();

        Map<Integer, Grade> gradeByStudentId = new HashMap<>();
        for (Grade g : existingGrades) {
            if (g.getStudent() != null) {
                gradeByStudentId.put(g.getStudent().getStudentId(), g);
            }
        }

        List<PedagogRegisterDto.StudentRowDto> studentRows = new ArrayList<>();
        List<BigDecimal> allGradeValues = new ArrayList<>();

        for (Student s : students) {
            String fullName = (s.getUser() != null)
                    ? ((s.getUser().getEmri() != null ? s.getUser().getEmri() : "") + " "
                            + (s.getUser().getMbiemri() != null ? s.getUser().getMbiemri() : "")).trim()
                    : "Student #" + s.getStudentId();

            if (fullName.isBlank()) {
                fullName = "Student " + s.getStudentId();
            }

            Grade g = gradeByStudentId.get(s.getStudentId());
            BigDecimal gradeVal = (g != null && g.getGrade() != null) ? g.getGrade() : null;

            if (gradeVal != null) {
                allGradeValues.add(gradeVal);
            }

            String status = "-";
            boolean isPermiresim = (g != null && "IMPROVED".equalsIgnoreCase(g.getStatus()));
            if (isPermiresim) {
                status = "P";
            } else if (gradeVal != null && gradeVal.compareTo(new BigDecimal("5.0")) < 0) {
                status = "N";
            }

            Map<String, Boolean> attMap = new HashMap<>();
            for (Topic t : courseTopics) {
                String colKey = "topic_" + t.getTopicId();
                if (t.getTopicDate() != null) {
                    String matchKey = s.getStudentId() + "_" + t.getTopicDate().toString();
                    Attendance att = attMapByKey.get(matchKey);
                    if (att != null) {
                        attMap.put(colKey, att.getStatus() != null && att.getStatus() == 1);
                    } else {
                        attMap.put(colKey, true); // default present
                    }
                } else {
                    attMap.put(colKey, true);
                }
            }

            studentRows.add(PedagogRegisterDto.StudentRowDto.builder()
                    .studentId(s.getStudentId())
                    .emri(fullName)
                    .nrMatrikulimit(s.getNrMatrikulimit() != null ? s.getNrMatrikulimit() : "IK-" + (3000 + s.getStudentId()))
                    .grade(gradeVal)
                    .status(status)
                    .isPermiresim(isPermiresim)
                    .attendance(attMap)
                    .build());
        }

        // Renditja alfabetike e studenteve
        studentRows.sort(Comparator.comparing(
                PedagogRegisterDto.StudentRowDto::getEmri,
                String.CASE_INSENSITIVE_ORDER
        ));

        // Calculate statistics
        PedagogRegisterDto.RegisterStatsDto stats = pedagogMapper.calculateStats(allGradeValues, students.size());

        String deptName = "";
        if (course != null && course.getProgram() != null && course.getProgram().getDepartment() != null) {
            deptName = course.getProgram().getDepartment().getEmerDepartamenti();
        } else if (classes != null && classes.getProgram() != null && classes.getProgram().getDepartment() != null) {
            deptName = classes.getProgram().getDepartment().getEmerDepartamenti();
        }

        return PedagogRegisterDto.builder()
                .courseId(courseId)
                .courseName(course != null ? course.getEmriCourse() : "Lenda")
                .classId(classId)
                .className(classes != null ? pedagogMapper.formatClassName(classes, classes.getVitStudimit()) : "Grupi A")
                .departmentName(deptName)
                .academicYear(academicYear != null ? academicYear : "2025-2026")
                .roleType(roleType != null ? roleType : "Seminar")
                .students(studentRows)
                .attendanceColumns(columns)
                .stats(stats)
                .build();
    }

    @Transactional
    public void saveGrades(SaveGradesRequest req) {
        if (req == null || req.getGrades() == null) {
            return;
        }

        TeachingCourse tc = null;
        if (req.getCourseId() != null) {
            List<TeachingCourse> tcs = teachingCourseRepo.findByCourseCourseId(req.getCourseId());
            if (!tcs.isEmpty()) {
                tc = tcs.get(0);
            }
        }

        for (SaveGradesRequest.StudentGradeEntry entry : req.getGrades()) {
            if (entry.getStudentId() == null) {
                continue;
            }

            Student student = studentRepo.findById(entry.getStudentId()).orElse(null);
            if (student == null) {
                continue;
            }

            Grade grade = null;
            if (req.getCourseId() != null) {
                grade = gradeRepo.findByStudent_StudentIdAndTeachingCourse_Course_CourseId(entry.getStudentId(), req.getCourseId()).orElse(null);
            }

            if (grade == null) {
                grade = Grade.builder()
                        .student(student)
                        .teachingCourse(tc)
                        .dateGiven(LocalDate.now())
                        .build();
            }

            grade.setGrade(entry.getGrade());
            if ("P".equalsIgnoreCase(entry.getStatus())) {
                grade.setStatus("IMPROVED");
            } else if (entry.getGrade() != null && entry.getGrade().compareTo(new BigDecimal("5.0")) < 0) {
                grade.setStatus("FAILED");
            } else {
                grade.setStatus("PASSED");
            }
            gradeRepo.save(grade);
        }
    }

    @Transactional
    public void saveAttendance(SaveAttendanceRequest req) {
        if (req == null || req.getAttendances() == null) {
            return;
        }

        TeachingCourse tc = null;
        if (req.getCourseId() != null) {
            List<TeachingCourse> tcs = teachingCourseRepo.findByCourseCourseId(req.getCourseId());
            if (!tcs.isEmpty()) {
                tc = tcs.get(0);
            }
        }

        List<Topic> courseTopics = (req.getCourseId() != null)
                ? topicRepo.findByTeachingCourseCourseCourseIdOrderByWeekNumberAsc(req.getCourseId())
                : Collections.emptyList();

        Map<String, LocalDate> topicDateByKey = new HashMap<>();
        for (Topic t : courseTopics) {
            if (t.getTopicDate() != null) {
                topicDateByKey.put("topic_" + t.getTopicId(), t.getTopicDate());
            }
        }

        for (SaveAttendanceRequest.StudentAttendanceEntry entry : req.getAttendances()) {
            if (entry.getStudentId() == null) {
                continue;
            }
            Student student = studentRepo.findById(entry.getStudentId()).orElse(null);
            if (student == null) {
                continue;
            }

            if (entry.getAttendance() != null) {
                for (Map.Entry<String, Boolean> att : entry.getAttendance().entrySet()) {
                    LocalDate attDate = topicDateByKey.get(att.getKey());
                    if (attDate == null) {
                        attDate = LocalDate.now();
                    }

                    Optional<Attendance> existingAtt = (tc != null)
                            ? attendanceRepo.findByStudent_StudentIdAndTeachingCourse_TeachingCourseIdAndDataAttendance(student.getStudentId(), tc.getTeachingCourseId(), attDate)
                            : Optional.empty();

                    final TeachingCourse finalTc = tc;
                    final LocalDate finalAttDate = attDate;
                    Attendance attendance = existingAtt.orElseGet(() -> Attendance.builder()
                            .student(student)
                            .teachingCourse(finalTc)
                            .dataAttendance(finalAttDate)
                            .build());

                    attendance.setStatus(Boolean.TRUE.equals(att.getValue()) ? 1 : 0);
                    attendance.setDataAttendance(attDate);
                    if (tc != null) {
                        attendance.setTeachingCourse(tc);
                    }
                    attendanceRepo.save(attendance);
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public BranchStatsDto getBranchStats(Integer courseId, String courseName, String academicYear, String dega, Integer departmentId) {
        Course course = null;
                if (courseId != null) {
                    course = courseRepo.findById(courseId).orElse(null);
                }
                if (course == null && courseName != null && !courseName.isBlank()) {
                    List<Course> matched = courseRepo.findAll().stream()
                            .filter(c -> c.getEmriCourse() != null && c.getEmriCourse().equalsIgnoreCase(courseName.trim()))
                            .collect(Collectors.toList());
                    if (!matched.isEmpty()) {
                        course = matched.get(0);
                    }
                }
                if (course == null) {
                    List<Course> all = courseRepo.findAll();
                    if (!all.isEmpty()) {
                        course = all.get(0);
                    }
                }

                String actualCourseName = (course != null && course.getEmriCourse() != null)
                        ? course.getEmriCourse()
                        : (courseName != null && !courseName.isBlank() ? courseName : "Programim ne Web");
                String actualYear = (academicYear != null && !academicYear.isBlank()) ? academicYear : "2025-2026";
                String actualDega = (dega != null && !dega.isBlank()) ? dega : "Inxhinieri Informatike (Te gjithe)";

                Department dept = null;
                if (departmentId != null) {
                    dept = departmentRepo.findById(departmentId).orElse(null);
                }
                if (dept == null && course != null && course.getProgram() != null) {
                    dept = course.getProgram().getDepartment();
                }
                String deptName = (dept != null && dept.getEmerDepartamenti() != null)
                        ? dept.getEmerDepartamenti()
                        : "Departamenti i Inxhinierise Kompjuterike";

                // 1. Përcakto Viti i Studimit të lëndës (p.sh. Viti 3 për Sistemet Operative)
                Integer courseStudyYear = (course != null && course.getStudyYear() != null) ? course.getStudyYear() : null;

                // 2. Merr klasat e lidhura me lëndën (TeachingCourse)
                List<TeachingCourse> tcs = (course != null) ? teachingCourseRepo.findByCourseCourseId(course.getCourseId()) : Collections.emptyList();
                Set<Integer> classIds = new HashSet<>();
                for (TeachingCourse tc : tcs) {
                    if (tc.getClasses() != null) {
                        for (Classes cl : tc.getClasses()) {
                            if (cl != null && cl.getClassId() != null) {
                                classIds.add(cl.getClassId());
                                if (courseStudyYear == null && cl.getVitStudimit() != null) {
                                    courseStudyYear = cl.getVitStudimit();
                                }
                            }
                        }
                    }
                }

                // 3. Merr studentët realë të lidhur me këtë lëndë nga DB
                Map<Integer, Student> targetStudentsMap = new LinkedHashMap<>();

                // A) Studentët nga klasat e TeachingCourses të kësaj lënde
                if (!classIds.isEmpty()) {
                    List<Student> studentsInClasses = studentRepo.findByClasses_ClassIdIn(classIds);
                    for (Student s : studentsInClasses) {
                        if (s.getStudentId() != null) {
                            targetStudentsMap.put(s.getStudentId(), s);
                        }
                    }
                }

                // B) Nëse nuk ka klasa të lidhura në teaching_courses, merr studentët e programit
                if (targetStudentsMap.isEmpty() && course != null && course.getProgram() != null && course.getProgram().getProgramId() != null) {
                    List<Student> progStudents = studentRepo.findByProgram_ProgramId(course.getProgram().getProgramId());
                    for (Student s : progStudents) {
                        if (s.getStudentId() != null) {
                            targetStudentsMap.put(s.getStudentId(), s);
                        }
                    }
                }

                // C) Merr notat reale të regjistruara për këtë lëndë
                List<Grade> grades = (course != null) ? gradeRepo.findByTeachingCourse_Course_CourseId(course.getCourseId()) : Collections.emptyList();
                Map<Integer, Grade> gradeByStudentId = new HashMap<>();
                for (Grade g : grades) {
                    if (g.getStudent() != null && g.getStudent().getStudentId() != null) {
                        gradeByStudentId.put(g.getStudent().getStudentId(), g);
                        targetStudentsMap.putIfAbsent(g.getStudent().getStudentId(), g.getStudent());
                    }
                }

                // 4. Filtro studentët sipas Degës DHE Vitit të Studimit të lëndës
                List<Student> finalStudents = new ArrayList<>();
                String degaLower = (dega != null && !dega.isBlank() && !dega.toLowerCase().contains("gjith"))
                        ? dega.trim().toLowerCase()
                        : null;

                for (Student s : targetStudentsMap.values()) {
                    // A) Verifikimi i Degës
                    boolean matchDega = true;
                    if (degaLower != null) {
                        boolean matchProg = (s.getProgram() != null && s.getProgram().getSpecializimi() != null
                                && (s.getProgram().getSpecializimi().toLowerCase().contains(degaLower) || degaLower.contains(s.getProgram().getSpecializimi().toLowerCase())));
                        boolean matchClassProg = (s.getClasses() != null && s.getClasses().getProgram() != null && s.getClasses().getProgram().getSpecializimi() != null
                                && (s.getClasses().getProgram().getSpecializimi().toLowerCase().contains(degaLower) || degaLower.contains(s.getClasses().getProgram().getSpecializimi().toLowerCase())));
                        boolean matchClassName = (s.getClasses() != null && s.getClasses().getEmriClass() != null
                                && (s.getClasses().getEmriClass().toLowerCase().contains(degaLower) || degaLower.contains(s.getClasses().getEmriClass().toLowerCase())));
                        matchDega = (matchProg || matchClassProg || matchClassName);
                    }

                    // B) Verifikimi i Vitit të Studimit (p.sh. Lënda e vitit 3 merr VETËM studentët e vitit 3)
                    boolean matchYear = true;
                    if (courseStudyYear != null) {
                        Integer sYear = s.getVitStudimit();
                        if (sYear == null && s.getClasses() != null) {
                            sYear = s.getClasses().getVitStudimit();
                        }
                        if (sYear != null) {
                            matchYear = sYear.equals(courseStudyYear);
                        }
                    }

                    if (matchDega && matchYear) {
                        finalStudents.add(s);
                    }
                }

                List<BranchStatsDto.StudentExportItem> studentExportList = new ArrayList<>();
                List<BigDecimal> allGradesList = new ArrayList<>();
                Map<String, List<BigDecimal>> groupGradesMap = new LinkedHashMap<>();
                Map<String, Integer> groupStudentCountMap = new LinkedHashMap<>();

                for (Student s : finalStudents) {
                    String sName = (s.getUser() != null)
                            ? ((s.getUser().getEmri() != null ? s.getUser().getEmri() : "") + " " + (s.getUser().getMbiemri() != null ? s.getUser().getMbiemri() : "")).trim()
                            : ("Student " + s.getStudentId());
                    String matrikull = s.getNrMatrikulimit() != null ? s.getNrMatrikulimit() : ("IK-" + (3000 + s.getStudentId()));
                    String grpName = (s.getClasses() != null && s.getClasses().getEmriClass() != null) ? s.getClasses().getEmriClass() : "Grupi A";

                    groupStudentCountMap.put(grpName, groupStudentCountMap.getOrDefault(grpName, 0) + 1);

                    Grade g = gradeByStudentId.get(s.getStudentId());
                    Integer notaVal = null;
                    String status = "Nuk ka hyre";

                    if (g != null && g.getGrade() != null) {
                        notaVal = g.getGrade().intValue();
                        allGradesList.add(g.getGrade());
                        groupGradesMap.computeIfAbsent(grpName, k -> new ArrayList<>()).add(g.getGrade());

                        if (notaVal >= 5) {
                            status = (notaVal >= 9) ? "Permiresim" : "Kaluar";
                        } else {
                            status = "Ngeles";
                        }
                    }

                    studentExportList.add(BranchStatsDto.StudentExportItem.builder()
                            .emri(sName)
                            .matrikulli(matrikull)
                            .grupi(grpName)
                            .nota(notaVal)
                            .statusi(status)
                            .build());
                }

                // Renditja alfabetike e studenteve ne statistikat e deges
                studentExportList.sort(Comparator.comparing(
                        BranchStatsDto.StudentExportItem::getEmri,
                        String.CASE_INSENSITIVE_ORDER
                ));

                int totalStudents = finalStudents.size();

                // If there is no real student/grade data in the DB yet, return honest
                // zero/empty statistics instead of fabricated sample data.
                if (studentExportList.isEmpty() || allGradesList.isEmpty()) {
                    return pedagogMapper.buildEmptyBranchStats(actualCourseName, deptName, actualDega, actualYear, totalStudents, studentExportList, groupGradesMap.keySet());
                }

                double sumPassing = 0.0;
                int passing = 0;
                int max = 0;
                int min = 10;
                Map<Integer, Long> countByIntGrade = new HashMap<>();

                for (BigDecimal g : allGradesList) {
                    int intVal = g.intValue();
                    double dVal = g.doubleValue();
                    if (intVal >= 5) {
                        sumPassing += dVal;
                        passing++;
                    }
                    if (intVal > max) {
                        max = intVal;
                    }
                    if (intVal < min) {
                        min = intVal;
                    }
                    countByIntGrade.put(intVal, countByIntGrade.getOrDefault(intVal, 0L) + 1);
                }

                // Mesatarja reale = Shuma e notave kaluese (>=5) pjesetuar me numrin e studenteve qe kane marre note kaluese
                double mesatarja = (passing > 0)
                        ? BigDecimal.valueOf(sumPassing / passing).setScale(2, RoundingMode.HALF_UP).doubleValue()
                        : (allGradesList.isEmpty() ? 0.0 : BigDecimal.valueOf(allGradesList.stream().mapToDouble(BigDecimal::doubleValue).average().orElse(0.0)).setScale(2, RoundingMode.HALF_UP).doubleValue());

                // Kalueshmëria = % e studentëve që kanë marrë një notë dhe e kanë mbi 4 (>= 5)
                double kalueshmeria = (!allGradesList.isEmpty())
                        ? BigDecimal.valueOf(((double) passing / allGradesList.size()) * 100).setScale(1, RoundingMode.HALF_UP).doubleValue()
                        : 0.0;

                double pjesemarrja = totalStudents == 0 ? 0.0 : BigDecimal.valueOf(((double) allGradesList.size() / totalStudents) * 100).setScale(1, RoundingMode.HALF_UP).doubleValue();

                int modeGrade = allGradesList.isEmpty() ? 0 : allGradesList.get(0).intValue();
                long maxFreq = 0;
                for (Map.Entry<Integer, Long> e : countByIntGrade.entrySet()) {
                    if (e.getValue() > maxFreq) {
                        maxFreq = e.getValue();
                        modeGrade = e.getKey();
                    }
                }

                List<BranchStatsDto.GradeDistributionItem> dist = new ArrayList<>();
                int[] sortedGrades = new int[]{10, 9, 8, 7, 6, 5, 4};
                for (int note : sortedGrades) {
                    long c = countByIntGrade.getOrDefault(note, 0L);
                    double pct = allGradesList.isEmpty() ? 0.0 : BigDecimal.valueOf(((double) c / allGradesList.size()) * 100).setScale(1, RoundingMode.HALF_UP).doubleValue();
                    dist.add(new BranchStatsDto.GradeDistributionItem(note, c, pct));
                }

                List<BranchStatsDto.GroupComparisonItem> groupItems = new ArrayList<>();
                for (Map.Entry<String, List<BigDecimal>> entry : groupGradesMap.entrySet()) {
                    String grp = entry.getKey();
                    List<BigDecimal> grpGrades = entry.getValue();
                    int grpTotal = groupStudentCountMap.getOrDefault(grp, grpGrades.size());
                    if (grpTotal == 0) {
                        grpTotal = grpGrades.size();
                    }

                    double grpSumPassing = 0.0;
                    int grpPassing = 0;
                    for (BigDecimal bg : grpGrades) {
                        if (bg != null && bg.intValue() >= 5) {
                            grpSumPassing += bg.doubleValue();
                            grpPassing++;
                        }
                    }

                    double grpAvg = (grpPassing > 0)
                            ? BigDecimal.valueOf(grpSumPassing / grpPassing).setScale(2, RoundingMode.HALF_UP).doubleValue()
                            : (grpGrades.isEmpty() ? 0.0 : BigDecimal.valueOf(grpGrades.stream().mapToDouble(BigDecimal::doubleValue).average().orElse(0.0)).setScale(2, RoundingMode.HALF_UP).doubleValue());
                    double grpPassRate = (!grpGrades.isEmpty())
                            ? BigDecimal.valueOf(((double) grpPassing / grpGrades.size()) * 100).setScale(1, RoundingMode.HALF_UP).doubleValue()
                            : 0.0;
                    double grpPart = grpTotal == 0 ? 0.0 : BigDecimal.valueOf(((double) grpGrades.size() / grpTotal) * 100).setScale(1, RoundingMode.HALF_UP).doubleValue();

                    groupItems.add(BranchStatsDto.GroupComparisonItem.builder()
                            .groupName(grp)
                            .studentCount(grpTotal)
                            .kalueshmeria(grpPassRate)
                            .pjesemarrja(grpPart)
                            .mesatarja(grpAvg)
                            .build());
                }

                return BranchStatsDto.builder()
                        .courseName(actualCourseName)
                        .departmentName(deptName)
                        .branchName(actualDega)
                        .academicYear(actualYear)
                        .numriStudenteve(totalStudents)
                        .mesatarja(mesatarja)
                        .kalueshmeria(kalueshmeria)
                        .moda(String.valueOf(modeGrade))
                        .notaLarteUlet(max + " / " + min)
                        .pjesemarrja(pjesemarrja)
                        .gradeDistribution(dist)
                        .groups(groupItems)
                        .students(studentExportList)
                        .build();
            }

    private String formatProfFullName(String emri, String mbiemri, String email) {
        String e = emri != null ? emri.trim() : "";
        String m = mbiemri != null ? mbiemri.trim() : "";
        String full = (e + " " + m).trim();
        if (full.isEmpty() && email != null && !email.isBlank()) {
            String clean = email.trim();
            String prefix = clean.contains("@") ? clean.substring(0, clean.indexOf('@')) : clean;
            full = prefix.replace('.', ' ').replace('_', ' ');
        }
        if (full.isEmpty()) {
            return "Profesor";
        }
        String[] words = full.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(Character.toUpperCase(w.charAt(0)));
                if (w.length() > 1) {
                    sb.append(w.substring(1));
                }
            }
        }
        return sb.toString();
    }
}
