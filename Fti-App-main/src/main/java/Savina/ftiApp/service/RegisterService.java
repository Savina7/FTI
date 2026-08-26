package Savina.ftiApp.service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Savina.ftiApp.dto.requestDTO.ProfessorRegisterRequest;
import Savina.ftiApp.dto.requestDTO.StudentRegisterRequest;
import Savina.ftiApp.entity.Department;
import Savina.ftiApp.entity.Professor;
import Savina.ftiApp.entity.ProfessorPreEnrollment;
import Savina.ftiApp.entity.Role;
import Savina.ftiApp.entity.Student;
import Savina.ftiApp.entity.StudentPreEnrollment;
import Savina.ftiApp.entity.User;
import Savina.ftiApp.repository.DepartmentRepository;
import Savina.ftiApp.repository.ProfessorPreEnrollmentRepository;
import Savina.ftiApp.repository.ProfessorRepository;
import Savina.ftiApp.repository.RoleRepository;
import Savina.ftiApp.repository.StudentPreEnrollmentRepository;
import Savina.ftiApp.repository.StudentRepository;
import Savina.ftiApp.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterService {

    private final StudentPreEnrollmentRepository enrollmentRepo;
    private final ProfessorPreEnrollmentRepository profEnrollmentRepo;
    private final UserRepository userRepo;
    private final StudentRepository studentRepo;
    private final ProfessorRepository professorRepo;
    private final RoleRepository roleRepo;
    private final DepartmentRepository departmentRepo;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    // ─── REGISTER STUDENT ──────────────────────────────────────────────────────
    @Transactional
    public Integer registerStudent(StudentRegisterRequest req) {
        String cleanEmail = req.getEmail().trim().toLowerCase();

        // 1. Find pre-enrollment record by matrikulimi
        StudentPreEnrollment enrollment = enrollmentRepo
                .findByNrMatrikulimit(req.getNrMatrikulimit())
                .orElseThrow(() -> new IllegalArgumentException(
                "Studenti me kete nr. matrikulimi nuk u gjet ne sistem."));

        // 2. Validate name + email match
        if (!enrollment.getEmri().equalsIgnoreCase(req.getEmri().trim())
                || !enrollment.getMbiemri().equalsIgnoreCase(req.getMbiemri().trim())
                || !enrollment.getEmail().equalsIgnoreCase(cleanEmail)) {
            throw new IllegalArgumentException(
                    "Te dhenat nuk perputhen me rekordin e studentit.");
        }

        // 3. Check if email already exists in USERS table
        Optional<User> existingUserOpt = userRepo.findByEmail(cleanEmail);
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();

            // Branch A: VERIFIED = 'Y' -> Already fully registered
            if ("Y".equals(existingUser.getVerified())) {
                throw new IllegalArgumentException(
                        "Ky email eshte tashme i regjistruar dhe i verifikuar. Ju lutemi kyçuni (login).");
            } else {
                // Branch B: VERIFIED = 'N' -> Incomplete registration. Update BCrypt password, OTP & resend email
                existingUser.setPassword(passwordEncoder.encode(req.getPassword()));

                String newCode = generateCode();
                existingUser.setVerificationCode(newCode);
                existingUser.setCodeCreatedAt(LocalDateTime.now());
                userRepo.save(existingUser);

                emailService.sendVerificationCode(existingUser.getEmail(),
                        existingUser.getEmri() + " " + existingUser.getMbiemri(), newCode);

                log.info("Student re-triggered registration for unverified email: {} (userId={})",
                        existingUser.getEmail(), existingUser.getUserId());
                return existingUser.getUserId();
            }
        }

        // 4. Branch C: New User -> Create User, Student, OTP & send email
        String code = generateCode();

        Role studentRole = roleRepo.findByRoleName("STUDENT")
                .orElseGet(() -> roleRepo.save(Role.builder().roleName("STUDENT").build()));

        User user = User.builder()
                .emri(req.getEmri().trim())
                .mbiemri(req.getMbiemri().trim())
                .email(cleanEmail)
                .password(passwordEncoder.encode(req.getPassword()))
                .createdAt(LocalDate.now())
                .status("PENDING")
                .verified("N")
                .verificationCode(code)
                .codeCreatedAt(LocalDateTime.now())
                .build();
        user.getRoles().add(studentRole);
        user = userRepo.save(user);

        Student student = Student.builder()
                .user(user)
                .program(enrollment.getProgram())
                .department(enrollment.getProgram() != null ? enrollment.getProgram().getDepartment() : null)
                .classes(enrollment.getClasses())
                .vitStudimit(enrollment.getVitStudimit() != null ? enrollment.getVitStudimit() : 1)
                .nrMatrikulimit(req.getNrMatrikulimit())
                .status("AKTIV")
                .build();
        studentRepo.save(student);

        emailService.sendVerificationCode(user.getEmail(),
                user.getEmri() + " " + user.getMbiemri(), code);

        log.info("Student registered: {} (userId={})", user.getEmail(), user.getUserId());
        return user.getUserId();
    }

    // ─── REGISTER PROFESSOR ────────────────────────────────────────────────────
    @Transactional
    public Integer registerProfessor(ProfessorRegisterRequest req) {
        String cleanEmail = req.getEmail().trim().toLowerCase();

        // 1. Validate professor pre-enrollment record if present in PROFESSOR_PRE_ENROLLMENT
        Optional<ProfessorPreEnrollment> profEnrollmentOpt = profEnrollmentRepo.findByEmail(cleanEmail);
        Department dept = null;

        if (profEnrollmentOpt.isPresent()) {
            ProfessorPreEnrollment profEnrollment = profEnrollmentOpt.get();
            if (!profEnrollment.getEmri().equalsIgnoreCase(req.getEmri().trim())
                    || !profEnrollment.getMbiemri().equalsIgnoreCase(req.getMbiemri().trim())) {
                throw new IllegalArgumentException(
                        "Te dhenat (Emri/Mbiemri) nuk perputhen me rekordin e pedagogut ne sistem.");
            }

            // Kontrollojme perputhshmerine e departamentit nese eshte caktuar ne pararegjistrim
            if (profEnrollment.getDepartment() != null) {
                String preDeptName = profEnrollment.getDepartment().getEmerDepartamenti();
                if (req.getDepartment() != null && !preDeptName.equalsIgnoreCase(req.getDepartment().trim())) {
                    throw new IllegalArgumentException(
                            "Departamenti i zgjedhur nuk perputhet me departamentin e pararegjistruar ('" + preDeptName + "').");
                }
                dept = profEnrollment.getDepartment();
            }
        } else if (profEnrollmentRepo.count() > 0) {
            throw new IllegalArgumentException(
                    "Pedagogu me kete email nuk u gjet ne listen e pararegjistrimit te fakultetit.");
        }

        // 2. Check if email already exists in USERS table
        Optional<User> existingUserOpt = userRepo.findByEmail(cleanEmail);
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();

            // Branch A: VERIFIED = 'Y' -> Already fully registered
            if ("Y".equals(existingUser.getVerified())) {
                throw new IllegalArgumentException(
                        "Ky email eshte tashme i regjistruar dhe i verifikuar. Ju lutemi kyçuni (login).");
            } else {
                // Branch B: VERIFIED = 'N' -> Incomplete registration. Update BCrypt password, OTP & resend email
                existingUser.setPassword(passwordEncoder.encode(req.getPassword()));

                String newCode = generateCode();
                existingUser.setVerificationCode(newCode);
                existingUser.setCodeCreatedAt(LocalDateTime.now());
                userRepo.save(existingUser);

                emailService.sendVerificationCode(existingUser.getEmail(),
                        existingUser.getEmri() + " " + existingUser.getMbiemri(), newCode);

                log.info("Professor re-triggered registration for unverified email: {} (userId={})",
                        existingUser.getEmail(), existingUser.getUserId());
                return existingUser.getUserId();
            }
        }

        // 3. Resolve department if not already set from pre-enrollment
        if (dept == null) {
            dept = departmentRepo.findByEmerDepartamenti(req.getDepartment())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Departamenti '" + req.getDepartment() + "' nuk u gjet."));
        }

        // 3. Generate 6-digit code
        String code = generateCode();

        // 4. Create USER
        Role professorRole = roleRepo.findByRoleName("PROFESSOR")
                .orElseGet(() -> roleRepo.save(Role.builder().roleName("PROFESSOR").build()));

        User user = User.builder()
                .emri(req.getEmri().trim())
                .mbiemri(req.getMbiemri().trim())
                .email(cleanEmail)
                .password(passwordEncoder.encode(req.getPassword()))
                .createdAt(LocalDate.now())
                .status("PENDING")
                .verified("N")
                .verificationCode(code)
                .codeCreatedAt(LocalDateTime.now())
                .build();
        user.getRoles().add(professorRole);
        user = userRepo.save(user);

        // 5. Create PROFESSOR record
        Professor professor = Professor.builder()
                .user(user)
                .department(dept)
                .status("A")
                .build();
        professorRepo.save(professor);

        // 6. Send verification email
        emailService.sendVerificationCode(user.getEmail(),
                user.getEmri() + " " + user.getMbiemri(), code);

        log.info("Professor registered: {} (userId={})", user.getEmail(), user.getUserId());
        return user.getUserId();
    }

    // ─── HELPERS ───────────────────────────────────────────────────────────────
    private String generateCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
