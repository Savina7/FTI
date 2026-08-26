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
        User user = null;
        if (userId != null) {
            user = userRepo.findById(userId).orElse(null);
        }
        if (user == null && email != null && !email.isBlank()) {
            user = userRepo.findByEmailIgnoreCase(email.trim()).orElse(null);
        }

        Professor professor = null;
        if (user != null) {
            professor = professorRepo.findByUserUserId(user.getUserId()).orElse(null);
        }
        if (professor == null && email != null && !email.isBlank()) {
            professor = professorRepo.findByUserEmailIgnoreCase(email.trim()).orElse(null);
        }
        if (professor == null && email != null && !email.isBlank()) {
            String search = email.trim().toLowerCase();
            List<Professor> profs = professorRepo.findAll();
            for (Professor p : profs) {
                if (p.getUser() != null) {
                    String full = ((p.getUser().getEmri() != null ? p.getUser().getEmri() : "") + " "
                            + (p.getUser().getMbiemri() != null ? p.getUser().getMbiemri() : "")).toLowerCase();
                    if (full.contains(search) || (p.getUser().getEmail() != null && p.getUser().getEmail().toLowerCase().contains(search))) {
                        professor = p;
                        break;
                    }
                }
            }
        }

        String profName = "";
        String profEmail = "";
        if (user != null) {
            profName = ((user.getEmri() != null ? user.getEmri() : "") + " "
                    + (user.getMbiemri() != null ? user.getMbiemri() : "")).trim();
            profEmail = user.getEmail() != null ? user.getEmail() : "";
        }
        if (profName.isEmpty() && professor != null && professor.getUser() != null) {
            profName = ((professor.getUser().getEmri() != null ? professor.getUser().getEmri() : "") + " "
                    + (professor.getUser().getMbiemri() != null ? professor.getUser().getMbiemri() : "")).trim();
            profEmail = professor.getUser().getEmail() != null ? professor.getUser().getEmail() : "";
        }
        // Fallback: nese emri eshte bosh, perdor email-in si identifikues
        if (profName.isEmpty() && !profEmail.isEmpty()) {
            profName = profEmail.contains("@") ? profEmail.substring(0, profEmail.indexOf('@')) : profEmail;
        }
        if (profName.isEmpty() && email != null && !email.isBlank()) {
            profName = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        }
        if (profName.isEmpty()) {
            profName = "Profesor";
        }
        log.info("getPedagogOptions: userId={}, email={}, profName='{}', professor={}", 
                  userId, email, profName, professor != null ? professor.getProfessorId() : "null");

        List<TeachingCourse> teachingCourses = Collections.emptyList();
        if (professor != null) {
            teachingCourses = teachingCourseRepo.findByProfessorProfessorId(professor.getProfessorId());
        }

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

        if (professor != null && !teachingCourses.isEmpty()) {
            for (TeachingCourse tc : teachingCourses) {
                String role = tc.getRoleType() != null ? tc.getRoleType().trim().toUpperCase() : "";
                if ("LEKSION".equals(role) || "LEKTOR".equals(role)) {
                    isLektor = true;
                }
                if ("SEMINAR".equals(role)) {
                    typesSet.add("Seminar");
                    hasRegistry = true;
                }
                if ("LABORATOR".equals(role) || "LAB".equals(role)) {
                    typesSet.add("Laborator");
                    hasRegistry = true;
                }
                if (!tc.getClasses().isEmpty() && typesSet.isEmpty()) {
                    if (role.contains("SEM")) {
                        typesSet.add("Seminar");
                        hasRegistry = true;
                    } else if (role.contains("LAB")) {
                        typesSet.add("Laborator");
                        hasRegistry = true;
                    }
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

                    if (c.getCourseId() != null && !seenCourseIds.contains(c.getCourseId())) {
                        seenCourseIds.add(c.getCourseId());
                        courseOptions.add(pedagogMapper.buildCourseOption(c));
                    }

                    if (tc.getClasses() != null && !tc.getClasses().isEmpty()) {
                        for (Classes cl : tc.getClasses()) {
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
        }

        // If department is null on course, check professor department
        if (departmentOptions.isEmpty() && professor != null && professor.getDepartment() != null) {
            Department d = professor.getDepartment();
            if (d.getDepartmentId() != null && !seenDeptIds.contains(d.getDepartmentId())) {
                seenDeptIds.add(d.getDepartmentId());
                departmentOptions.add(pedagogMapper.buildDepartmentOption(d));
            }
        }

        // GJITHMONE: Nese kurset/departamentet/klasat jane bosh (pedagog pa TeachingCourse ose pa lidhje),
        // ngarkohen te gjitha nga DB qe regjistri dhe dropdown-et te funksionojne 100%
        if (courseOptions.isEmpty()) {
            hasRegistry = true;
            isLektor = true;
            List<Course> allCourses = courseRepo.findAll();
            for (Course c : allCourses) {
                if (c.getCourseId() != null && !seenCourseIds.contains(c.getCourseId())) {
                    seenCourseIds.add(c.getCourseId());
                    courseOptions.add(pedagogMapper.buildCourseOption(c));
                    Department dept = (c.getProgram() != null) ? c.getProgram().getDepartment() : null;
                    if (dept != null && dept.getDepartmentId() != null && !seenDeptIds.contains(dept.getDepartmentId())) {
                        seenDeptIds.add(dept.getDepartmentId());
                        departmentOptions.add(pedagogMapper.buildDepartmentOption(dept));
                    }
                }
            }
        }

        if (departmentOptions.isEmpty()) {
            List<Department> allDepts = departmentRepo.findAll();
            for (Department d : allDepts) {
                if (d.getDepartmentId() != null && !seenDeptIds.contains(d.getDepartmentId())) {
                    seenDeptIds.add(d.getDepartmentId());
                    departmentOptions.add(pedagogMapper.buildDepartmentOption(d));
                }
            }
        }

        if (classOptions.isEmpty()) {
            List<Classes> allClasses = classesRepo.findAll();
            for (Classes cl : allClasses) {
                if (cl.getClassId() != null) {
                    String classKey = cl.getClassId() + "_all";
                    if (!seenClassKeys.contains(classKey)) {
                        seenClassKeys.add(classKey);
                        classOptions.add(pedagogMapper.buildClassOption(cl, null));
                    }
                }
            }
        }

        if (typesSet.isEmpty()) {
            typesSet.add("Seminar");
            typesSet.add("Laborator");
            hasRegistry = true;
        }

        List<String> typesList = new ArrayList<>(typesSet);

        List<PedagogOptionsDto.ProgramOptionDto> programOptions = new ArrayList<>();
        List<Program> allPrograms = programRepo.findAll();
        for (Program p : allPrograms) {
            programOptions.add(pedagogMapper.buildProgramOption(p));
        }

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
            public void saveGrades
            (SaveGradesRequest req
                
            ) {
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
            public void saveAttendance
            (SaveAttendanceRequest req
                
            ) {
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
            public BranchStatsDto getBranchStats
            (Integer courseId, String courseName
            , String academicYear, String dega
            , Integer departmentId
                
            ) {
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

                // Find relevant classes for this course
                List<Classes> relevantClasses = new ArrayList<>();
                if (course != null) {
                    List<TeachingCourse> tcs = teachingCourseRepo.findByCourseCourseId(course.getCourseId());
                    for (TeachingCourse tc : tcs) {
                        if (tc.getClasses() != null) {
                            relevantClasses.addAll(tc.getClasses());
                        }
                    }
                    if (relevantClasses.isEmpty() && course.getProgram() != null && course.getProgram().getProgramId() != null) {
                        List<Classes> progClasses = classesRepo.findByProgram_ProgramId(course.getProgram().getProgramId());
                        if (progClasses != null) {
                            relevantClasses.addAll(progClasses);
                        }
                    }
                }
                if (relevantClasses.isEmpty()) {
                    relevantClasses = classesRepo.findAll();
                }

                // Deduplicate classes
                Map<Integer, Classes> uniqueClasses = new LinkedHashMap<>();
                for (Classes cl : relevantClasses) {
                    if (cl.getClassId() != null) {
                        uniqueClasses.put(cl.getClassId(), cl);
                    }
                }

                // Filter by dega/program (e.g. Inxhinieri Informatike - perfshin te gjitha grupet A, B, C te deges)
                List<Classes> targetClasses = new ArrayList<>();
                if (dega != null && !dega.isBlank()) {
                    String degaLower = dega.trim().toLowerCase();
                    for (Classes cl : uniqueClasses.values()) {
                        boolean matchesProgram = (cl.getProgram() != null && cl.getProgram().getSpecializimi() != null
                                && (cl.getProgram().getSpecializimi().toLowerCase().contains(degaLower) || degaLower.contains(cl.getProgram().getSpecializimi().toLowerCase())));
                        boolean matchesClassName = (cl.getEmriClass() != null
                                && (cl.getEmriClass().toLowerCase().contains(degaLower) || degaLower.contains(cl.getEmriClass().toLowerCase())));
                        if (matchesProgram || matchesClassName) {
                            targetClasses.add(cl);
                        }
                    }
                }
                if (targetClasses.isEmpty()) {
                    targetClasses.addAll(uniqueClasses.values());
                }

                // Fetch students in these classes
                List<Student> students = new ArrayList<>();
                if (!targetClasses.isEmpty()) {
                    Set<Integer> classIds = targetClasses.stream().map(Classes::getClassId).collect(Collectors.toSet());
                    students = studentRepo.findByClasses_ClassIdIn(classIds);
                }

                // Fetch grades for this course
                List<Grade> grades = Collections.emptyList();
                if (course != null) {
                    grades = gradeRepo.findByTeachingCourse_Course_CourseId(course.getCourseId());
                }

                Map<Integer, Grade> gradeByStudentId = new HashMap<>();
                for (Grade g : grades) {
                    if (g.getStudent() != null && g.getStudent().getStudentId() != null) {
                        gradeByStudentId.put(g.getStudent().getStudentId(), g);
                    }
                }

                List<BranchStatsDto.StudentExportItem> studentExportList = new ArrayList<>();
                List<BigDecimal> allGradesList = new ArrayList<>();

                Map<String, List<BigDecimal>> groupGradesMap = new LinkedHashMap<>();
                Map<String, Integer> groupStudentCountMap = new LinkedHashMap<>();

                for (Classes cl : targetClasses) {
                    String grpName = cl.getEmriClass() != null ? cl.getEmriClass() : ("Klasa " + cl.getClassId());
                    groupGradesMap.putIfAbsent(grpName, new ArrayList<>());
                    groupStudentCountMap.putIfAbsent(grpName, 0);
                }

                for (Student s : students) {
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

                // If there is no real student/grade data in the DB yet, return honest
                // zero/empty statistics instead of fabricated sample data.
                if (studentExportList.isEmpty() || allGradesList.isEmpty()) {
                    return pedagogMapper.buildEmptyBranchStats(actualCourseName, deptName, actualDega, actualYear, students.size(), studentExportList, groupGradesMap.keySet());
                }

                int totalStudents = students.size();
                double sum = 0;
                int passing = 0;
                int max = 0;
                int min = 10;
                Map<Integer, Long> countByIntGrade = new HashMap<>();

                for (BigDecimal g : allGradesList) {
                    int intVal = g.intValue();
                    sum += g.doubleValue();
                    if (intVal >= 5) {
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

                double mesatarja = allGradesList.isEmpty() ? 0.0 : BigDecimal.valueOf(sum / allGradesList.size()).setScale(2, RoundingMode.HALF_UP).doubleValue();
                double kalueshmeria = allGradesList.isEmpty() ? 0.0 : BigDecimal.valueOf(((double) passing / allGradesList.size()) * 100).setScale(1, RoundingMode.HALF_UP).doubleValue();
                double pjesemarrja = totalStudents == 0 ? 0.0 : BigDecimal.valueOf(((double) allGradesList.size() / totalStudents) * 100).setScale(1, RoundingMode.HALF_UP).doubleValue();

                int modeGrade = allGradesList.isEmpty() ? 8 : allGradesList.get(0).intValue();
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

                    double grpSum = 0;
                    int grpPassing = 0;
                    for (BigDecimal bg : grpGrades) {
                        grpSum += bg.doubleValue();
                        if (bg.intValue() >= 5) {
                            grpPassing++;
                        }
                    }

                    double grpAvg = grpGrades.isEmpty() ? 0.0 : BigDecimal.valueOf(grpSum / grpGrades.size()).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    double grpPassRate = grpGrades.isEmpty() ? 0.0 : BigDecimal.valueOf(((double) grpPassing / grpGrades.size()) * 100).setScale(1, RoundingMode.HALF_UP).doubleValue();
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
        }
