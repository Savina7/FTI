package Savina.ftiApp.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Savina.ftiApp.dto.responseDTO.StudentAdminDto;
import Savina.ftiApp.dto.requestDTO.StudentPreEnrollmentRequest;
import Savina.ftiApp.entity.Classes;
import Savina.ftiApp.entity.Program;
import Savina.ftiApp.entity.Role;
import Savina.ftiApp.entity.Student;
import Savina.ftiApp.entity.StudentPreEnrollment;
import Savina.ftiApp.entity.User;
import Savina.ftiApp.repository.ClassesRepository;
import Savina.ftiApp.repository.DepartmentRepository;
import Savina.ftiApp.repository.ProgramRepository;
import Savina.ftiApp.repository.RoleRepository;
import Savina.ftiApp.repository.StudentPreEnrollmentRepository;
import Savina.ftiApp.repository.StudentRepository;
import Savina.ftiApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminStudentService {

    private final StudentPreEnrollmentRepository enrollmentRepo;
    private final StudentRepository studentRepo;
    private final ProgramRepository programRepo;
    private final ClassesRepository classesRepo;
    private final DepartmentRepository departmentRepo;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<StudentAdminDto> getAllStudents() {
        List<StudentAdminDto> result = new ArrayList<>();
        java.util.Set<String> seenMatrikulime = new java.util.HashSet<>();

        // 1. Fetch pre-enrollments directly from DB (all students are in STUDENT_PRE_ENROLLMENT)
        List<StudentPreEnrollment> preEnrollments = enrollmentRepo.findAll();
        for (StudentPreEnrollment pe : preEnrollments) {
            String matrikulimi = pe.getNrMatrikulimit() != null ? pe.getNrMatrikulimit().trim() : "";
            if (!matrikulimi.isEmpty()) {
                seenMatrikulime.add(matrikulimi.toLowerCase());
            }

            String progName = (pe.getProgram() != null && pe.getProgram().getNivel() != null) ? pe.getProgram().getNivel() : "";
            String specName = (pe.getProgram() != null && pe.getProgram().getSpecializimi() != null) ? pe.getProgram().getSpecializimi() : "";
            String deptName = (pe.getProgram() != null && pe.getProgram().getDepartment() != null)
                    ? pe.getProgram().getDepartment().getEmerDepartamenti()
                    : "";
            String className = pe.getClasses() != null ? pe.getClasses().getEmriClass() : "";

            String status = pe.getStatus() != null ? pe.getStatus() : "PARAREGJISTRUAR";
            if (!"VERIFIKUAR".equalsIgnoreCase(status) && pe.getEmail() != null) {
                if (userRepo.findByEmail(pe.getEmail().trim().toLowerCase()).map(u -> "Y".equals(u.getVerified())).orElse(false)) {
                    status = "VERIFIKUAR";
                }
            }

            result.add(StudentAdminDto.builder()
                    .id(pe.getEnrollmentId())
                    .emri(pe.getEmri())
                    .mbiemri(pe.getMbiemri())
                    .email(pe.getEmail())
                    .nrMatrikulimit(matrikulimi)
                    .program(progName)
                    .specializimi(specName)
                    .vitStudimit(pe.getVitStudimit() != null ? pe.getVitStudimit() : 1)
                    .grupi(className)
                    .dega(deptName)
                    .status(status)
                    .build());
        }

        // 2. Fetch any registered students that might not be in STUDENT_PRE_ENROLLMENT
        List<Student> registeredStudents = studentRepo.findAll();
        for (Student s : registeredStudents) {
            String matrikulimi = s.getNrMatrikulimit() != null ? s.getNrMatrikulimit().trim() : "";
            if (!matrikulimi.isEmpty() && seenMatrikulime.contains(matrikulimi.toLowerCase())) {
                continue; // Already included from pre-enrollment
            }

            String progName = (s.getProgram() != null && s.getProgram().getNivel() != null) ? s.getProgram().getNivel() : "";
            String specName = (s.getProgram() != null && s.getProgram().getSpecializimi() != null) ? s.getProgram().getSpecializimi() : "";
            String deptName = s.getDepartment() != null ? s.getDepartment().getEmerDepartamenti() : "";
            String className = s.getClasses() != null ? s.getClasses().getEmriClass() : "";
            String emri = s.getUser() != null ? s.getUser().getEmri() : "";
            String mbiemri = s.getUser() != null ? s.getUser().getMbiemri() : "";
            String email = s.getUser() != null ? s.getUser().getEmail() : "";

            result.add(StudentAdminDto.builder()
                    .id(s.getStudentId())
                    .emri(emri)
                    .mbiemri(mbiemri)
                    .email(email)
                    .nrMatrikulimit(matrikulimi)
                    .program(progName)
                    .specializimi(specName)
                    .vitStudimit(s.getVitStudimit() != null ? s.getVitStudimit() : 1)
                    .grupi(className)
                    .dega(deptName)
                    .status("VERIFIKUAR")
                    .build());
        }

        return result;
    }

    @Transactional
    public StudentAdminDto addStudentPreEnrollment(StudentPreEnrollmentRequest req) {
        String cleanEmail = req.getEmail().trim().toLowerCase();
        String cleanMatrikulimi = req.getNrMatrikulimit().trim();

        if (enrollmentRepo.existsByNrMatrikulimitIgnoreCase(cleanMatrikulimi) || studentRepo.existsByNrMatrikulimitIgnoreCase(cleanMatrikulimi)) {
            throw new IllegalArgumentException("Studenti me nr. matrikulimit '" + cleanMatrikulimi + "' ekziston tashme ne sistem.");
        }

        if (enrollmentRepo.existsByEmailIgnoreCase(cleanEmail) || userRepo.existsByEmail(cleanEmail)) {
            throw new IllegalArgumentException("Studenti me email '" + cleanEmail + "' ekziston tashme ne sistem.");
        }

        Program program = null;
        if (req.getProgramId() != null) {
            program = programRepo.findById(req.getProgramId()).orElse(null);
        }
        if (program == null && (req.getProgram() != null || req.getDega() != null)) {
            String nivel = req.getProgram();
            String dega = req.getDega();
            String diplomeDyfishteVal = Boolean.TRUE.equals(req.getDoubleDegree()) ? "PO" : "JO";

            if (nivel != null && dega != null) {
                program = programRepo.findFirstByNivelAndSpecializimiAndDiplomeDyfishte(nivel, dega, diplomeDyfishteVal)
                        .orElseGet(() -> programRepo.findFirstByNivelAndSpecializimi(nivel, dega)
                        .orElseGet(() -> programRepo.findFirstByNivel(nivel).orElse(null)));
            } else if (nivel != null) {
                program = programRepo.findFirstByNivel(nivel).orElse(null);
            }
        }

        Classes classes = null;
        if (req.getClassId() != null) {
            classes = classesRepo.findById(req.getClassId()).orElse(null);
        }
        if (classes == null && program != null) {
            final Program targetProgram = program;
            Integer viti = req.getVitStudimit() != null ? req.getVitStudimit() : 1;
            String emriClass = req.getGrupi() != null ? req.getGrupi() : "";
            classes = classesRepo.findFirstByProgramAndVitStudimit(targetProgram, viti)
                    .orElseGet(() -> classesRepo.save(Classes.builder()
                    .program(targetProgram)
                    .vitStudimit(viti)
                    .emriClass(emriClass)
                    .build()));
        }

        StudentPreEnrollment pe = StudentPreEnrollment.builder()
                .emri(req.getEmri().trim())
                .mbiemri(req.getMbiemri().trim())
                .email(cleanEmail)
                .nrMatrikulimit(cleanMatrikulimi)
                .program(program)
                .vitStudimit(req.getVitStudimit() != null ? req.getVitStudimit() : 1)
                .classes(classes)
                .build();

        pe = enrollmentRepo.save(pe);
        log.info("Admin added new student pre-enrollment: {} {} ({})", pe.getEmri(), pe.getMbiemri(), pe.getNrMatrikulimit());

        String progName = (pe.getProgram() != null && pe.getProgram().getNivel() != null) ? pe.getProgram().getNivel() : "";
        String specName = (pe.getProgram() != null && pe.getProgram().getSpecializimi() != null) ? pe.getProgram().getSpecializimi() : "";
        String deptName = (pe.getProgram() != null && pe.getProgram().getDepartment() != null)
                ? pe.getProgram().getDepartment().getEmerDepartamenti()
                : "";
        String className = pe.getClasses() != null ? pe.getClasses().getEmriClass() : (req.getGrupi() != null ? req.getGrupi() : "");

        return StudentAdminDto.builder()
                .id(pe.getEnrollmentId())
                .emri(pe.getEmri())
                .mbiemri(pe.getMbiemri())
                .email(pe.getEmail())
                .nrMatrikulimit(pe.getNrMatrikulimit())
                .program(progName)
                .specializimi(specName)
                .vitStudimit(pe.getVitStudimit())
                .grupi(className)
                .dega(deptName)
                .status("PARAREGJISTRUAR")
                .build();
    }

    @Transactional
    public void deleteStudentPreEnrollment(Integer id) {
        enrollmentRepo.deleteById(id);
        log.info("Admin deleted student pre-enrollment ID: {}", id);
    }

    @Transactional
    public void deleteStudent(Integer id, String status) {
        if ("VERIFIKUAR".equalsIgnoreCase(status)) {
            studentRepo.findById(id).ifPresent(student -> {
                User user = student.getUser();
                studentRepo.delete(student);
                if (user != null) {
                    userRepo.delete(user);
                }
            });
            log.info("Admin deleted verified student ID: {}", id);
        } else {
            deleteStudentPreEnrollment(id);
        }
    }

    @Transactional
    public StudentAdminDto updateStudent(Integer id, StudentPreEnrollmentRequest req) {
        String cleanEmail = req.getEmail().trim().toLowerCase();
        String cleanMatrikulimi = req.getNrMatrikulimit().trim();
        String status = req.getStatus() != null ? req.getStatus() : "PARAREGJISTRUAR";

        Program program = null;
        if (req.getProgramId() != null) {
            program = programRepo.findById(req.getProgramId()).orElse(null);
        }
        if (program == null && (req.getProgram() != null || req.getDega() != null)) {
            String nivel = req.getProgram();
            String dega = req.getDega();
            String diplomeDyfishteVal = Boolean.TRUE.equals(req.getDoubleDegree()) ? "PO" : "JO";

            if (nivel != null && dega != null) {
                program = programRepo.findFirstByNivelAndSpecializimiAndDiplomeDyfishte(nivel, dega, diplomeDyfishteVal)
                        .orElseGet(() -> programRepo.findFirstByNivelAndSpecializimi(nivel, dega)
                        .orElseGet(() -> programRepo.findFirstByNivel(nivel).orElse(null)));
            } else if (nivel != null) {
                program = programRepo.findFirstByNivel(nivel).orElse(null);
            }
        }

        Classes classes = null;
        if (req.getClassId() != null) {
            classes = classesRepo.findById(req.getClassId()).orElse(null);
        }
        if (classes == null && program != null) {
            final Program targetProgram = program;
            Integer viti = req.getVitStudimit() != null ? req.getVitStudimit() : 1;
            String emriClass = req.getGrupi() != null ? req.getGrupi() : "";
            classes = classesRepo.findFirstByProgramAndVitStudimit(targetProgram, viti)
                    .orElseGet(() -> classesRepo.save(Classes.builder()
                    .program(targetProgram)
                    .vitStudimit(viti)
                    .emriClass(emriClass)
                    .build()));
        }

        if ("VERIFIKUAR".equalsIgnoreCase(status)) {
            Student student = studentRepo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Studenti i verifikuar nuk u gjet."));

            User user = student.getUser();
            if (user != null) {
                user.setEmri(req.getEmri().trim());
                user.setMbiemri(req.getMbiemri().trim());
                user.setEmail(cleanEmail);
                userRepo.save(user);
            }

            student.setNrMatrikulimit(cleanMatrikulimi);
            student.setProgram(program);
            student.setVitStudimit(req.getVitStudimit() != null ? req.getVitStudimit() : 1);
            student.setClasses(classes);
            student = studentRepo.save(student);

            String progName = (student.getProgram() != null && student.getProgram().getNivel() != null) ? student.getProgram().getNivel() : "";
            String specName = (student.getProgram() != null && student.getProgram().getSpecializimi() != null) ? student.getProgram().getSpecializimi() : "";
            String deptName = student.getDepartment() != null ? student.getDepartment().getEmerDepartamenti() : "";
            String className = student.getClasses() != null ? student.getClasses().getEmriClass() : (req.getGrupi() != null ? req.getGrupi() : "");
            String emri = user != null ? user.getEmri() : req.getEmri().trim();
            String mbiemri = user != null ? user.getMbiemri() : req.getMbiemri().trim();
            String email = user != null ? user.getEmail() : cleanEmail;

            return StudentAdminDto.builder()
                    .id(student.getStudentId())
                    .emri(emri)
                    .mbiemri(mbiemri)
                    .email(email)
                    .nrMatrikulimit(student.getNrMatrikulimit())
                    .program(progName)
                    .specializimi(specName)
                    .vitStudimit(student.getVitStudimit())
                    .grupi(className)
                    .dega(deptName)
                    .status("VERIFIKUAR")
                    .build();
        } else {
            StudentPreEnrollment pe = enrollmentRepo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Pararegjistrimi i studentit nuk u gjet."));

            pe.setEmri(req.getEmri().trim());
            pe.setMbiemri(req.getMbiemri().trim());
            pe.setEmail(cleanEmail);
            pe.setNrMatrikulimit(cleanMatrikulimi);
            pe.setProgram(program);
            pe.setVitStudimit(req.getVitStudimit() != null ? req.getVitStudimit() : 1);
            pe.setClasses(classes);

            pe = enrollmentRepo.save(pe);

            String progName = (pe.getProgram() != null && pe.getProgram().getNivel() != null) ? pe.getProgram().getNivel() : "";
            String specName = (pe.getProgram() != null && pe.getProgram().getSpecializimi() != null) ? pe.getProgram().getSpecializimi() : "";
            String deptName = (pe.getProgram() != null && pe.getProgram().getDepartment() != null)
                    ? pe.getProgram().getDepartment().getEmerDepartamenti()
                    : "";
            String className = pe.getClasses() != null ? pe.getClasses().getEmriClass() : (req.getGrupi() != null ? req.getGrupi() : "");

            return StudentAdminDto.builder()
                    .id(pe.getEnrollmentId())
                    .emri(pe.getEmri())
                    .mbiemri(pe.getMbiemri())
                    .email(pe.getEmail())
                    .nrMatrikulimit(pe.getNrMatrikulimit())
                    .program(progName)
                    .specializimi(specName)
                    .vitStudimit(pe.getVitStudimit())
                    .grupi(className)
                    .dega(deptName)
                    .status("PARAREGJISTRUAR")
                    .build();
        }
    }

    @Transactional(readOnly = true)
    public List<String> getAllNivele() {
        List<String> list = programRepo.findDistinctNivele();
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream()
                .filter(s -> s != null && !s.trim().isEmpty())
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getAllSpecializime() {
        List<String> list = programRepo.findDistinctSpecializime();
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream()
                .filter(s -> s != null && !s.trim().isEmpty())
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getAllSpecializimeByNivelOnly(String nivel) {
        List<String> list = programRepo.findSpecializimeByNivelOnly(nivel);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream()
                .filter(s -> s != null && !s.trim().isEmpty())
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getAllSpecializimeFiltered(String nivel, Boolean doubleDegree) {
        boolean isDouble = Boolean.TRUE.equals(doubleDegree);
        List<String> list = programRepo.findSpecializimeByNivelAndDoubleDegree(nivel, isDouble);

        // Fallback: if double degree filter returns nothing, try just the nivel filter
        if (list == null || list.isEmpty()) {
            list = programRepo.findSpecializimeByNivelOnly(nivel);
        }

        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }

        return list.stream()
                .filter(s -> s != null && !s.trim().isEmpty())
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
    }

    @Transactional
    public StudentAdminDto verifyAndRegisterDirectly(Integer id) {
        StudentPreEnrollment pe = enrollmentRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pararegjistrimi i studentit me ID " + id + " nuk u gjet."));

        String cleanEmail = pe.getEmail().trim().toLowerCase();
        String cleanMatrikulimi = pe.getNrMatrikulimit() != null ? pe.getNrMatrikulimit().trim() : "";

        // Check if user already exists
        User user = userRepo.findByEmail(cleanEmail).orElse(null);
        if (user == null) {
            Role studentRole = roleRepo.findByRoleName("STUDENT")
                    .orElseGet(() -> roleRepo.save(Role.builder().roleName("STUDENT").build()));

            user = User.builder()
                    .emri(pe.getEmri())
                    .mbiemri(pe.getMbiemri())
                    .email(cleanEmail)
                    .password(passwordEncoder.encode("Fti12345!"))
                    .createdAt(LocalDate.now())
                    .status("ACTIVE")
                    .verified("Y")
                    .build();
            user.getRoles().add(studentRole);
            user = userRepo.save(user);
        } else {
            user.setVerified("Y");
            user.setStatus("ACTIVE");
            user = userRepo.save(user);
        }

        // Check if Student record exists
        final User finalUser = user;
        Student student = studentRepo.findByUserUserId(finalUser.getUserId()).orElse(null);
        if (student == null) {
            student = Student.builder()
                    .user(finalUser)
                    .program(pe.getProgram())
                    .department(pe.getProgram() != null ? pe.getProgram().getDepartment() : null)
                    .classes(pe.getClasses())
                    .vitStudimit(pe.getVitStudimit() != null ? pe.getVitStudimit() : 1)
                    .nrMatrikulimit(cleanMatrikulimi)
                    .status("AKTIV")
                    .build();
            student = studentRepo.save(student);
        } else {
            student.setProgram(pe.getProgram());
            if (pe.getProgram() != null) {
                student.setDepartment(pe.getProgram().getDepartment());
            }
            student.setClasses(pe.getClasses());
            student.setVitStudimit(pe.getVitStudimit() != null ? pe.getVitStudimit() : 1);
            student.setNrMatrikulimit(cleanMatrikulimi);
            student.setStatus("AKTIV");
            student = studentRepo.save(student);
        }

        // 3. Keep in STUDENT_PRE_ENROLLMENT and update STATUS to VERIFIKUAR
        pe.setStatus("VERIFIKUAR");
        pe = enrollmentRepo.save(pe);

        log.info("Student {} {} ({}) verified and registered directly into USERS & STUDENTS (remains in STUDENT_PRE_ENROLLMENT with status VERIFIKUAR)",
                finalUser.getEmri(), finalUser.getMbiemri(), cleanMatrikulimi);

        String progName = (pe.getProgram() != null && pe.getProgram().getNivel() != null) ? pe.getProgram().getNivel() : "";
        String specName = (pe.getProgram() != null && pe.getProgram().getSpecializimi() != null) ? pe.getProgram().getSpecializimi() : "";
        String deptName = (pe.getProgram() != null && pe.getProgram().getDepartment() != null)
                ? pe.getProgram().getDepartment().getEmerDepartamenti()
                : (student.getDepartment() != null ? student.getDepartment().getEmerDepartamenti() : "");
        String className = pe.getClasses() != null ? pe.getClasses().getEmriClass() : (student.getClasses() != null ? student.getClasses().getEmriClass() : "");

        return StudentAdminDto.builder()
                .id(pe.getEnrollmentId())
                .emri(finalUser.getEmri())
                .mbiemri(finalUser.getMbiemri())
                .email(finalUser.getEmail())
                .nrMatrikulimit(pe.getNrMatrikulimit())
                .program(progName)
                .specializimi(specName)
                .vitStudimit(pe.getVitStudimit())
                .grupi(className)
                .dega(deptName)
                .status("VERIFIKUAR")
                .build();
    }
}
