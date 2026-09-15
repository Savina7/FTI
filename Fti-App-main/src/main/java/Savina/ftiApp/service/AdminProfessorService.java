package Savina.ftiApp.service;

import Savina.ftiApp.dto.responseDTO.ProfessorAdminDto;
import Savina.ftiApp.dto.requestDTO.ProfessorPreEnrollmentRequest;
import Savina.ftiApp.entity.Department;
import Savina.ftiApp.entity.Professor;
import Savina.ftiApp.entity.ProfessorPreEnrollment;
import Savina.ftiApp.entity.Role;
import Savina.ftiApp.entity.User;
import Savina.ftiApp.mapper.ProfessorMapper;
import Savina.ftiApp.repository.DepartmentRepository;
import Savina.ftiApp.repository.ProfessorPreEnrollmentRepository;
import Savina.ftiApp.repository.ProfessorRepository;
import Savina.ftiApp.repository.RoleRepository;
import Savina.ftiApp.repository.UserRepository;
import Savina.ftiApp.util.PasswordGeneratorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminProfessorService {

    private final ProfessorPreEnrollmentRepository profEnrollmentRepo;
    private final ProfessorRepository profRepo;
    private final DepartmentRepository departmentRepo;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final ProfessorMapper professorMapper;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public List<ProfessorAdminDto> getAllProfessors() {
        List<ProfessorAdminDto> result = new ArrayList<>();
        Set<String> verifiedEmails = new HashSet<>();

        List<Professor> professors = profRepo.findAll();
        for (Professor p : professors) {
            ProfessorAdminDto dto = professorMapper.toDto(p);
            if (dto.getEmail() != null) {
                verifiedEmails.add(dto.getEmail().trim().toLowerCase());
            }
            result.add(dto);
        }

        List<ProfessorPreEnrollment> preEnrollments = profEnrollmentRepo.findAll();
        for (ProfessorPreEnrollment pe : preEnrollments) {
            String email = pe.getEmail() != null ? pe.getEmail().trim().toLowerCase() : "";
            if (!email.isEmpty() && verifiedEmails.contains(email)) {
                continue;
            }
            result.add(professorMapper.toDto(pe));
        }

        return result;
    }

    @Transactional
    public ProfessorAdminDto addProfessorPreEnrollment(ProfessorPreEnrollmentRequest req) {
        String cleanEmail = req.getEmail().trim().toLowerCase();

        if (profEnrollmentRepo.existsByEmailIgnoreCase(cleanEmail) || userRepo.existsByEmail(cleanEmail)) {
            throw new IllegalArgumentException("Pedagogu me email '" + cleanEmail + "' ekziston tashme ne sistem.");
        }

        Department dept = null;
        if (req.getDepartment() != null && !req.getDepartment().trim().isEmpty()) {
            dept = departmentRepo.findByEmerDepartamentiIgnoreCase(req.getDepartment().trim())
                    .orElseGet(() -> departmentRepo.findByEmerDepartamenti(req.getDepartment().trim())
                    .orElse(null));
        }
        if (dept == null) {
            dept = departmentRepo.findAll().stream().findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Nuk u gjet asnje departament ne sistem."));
        }

        ProfessorPreEnrollment pe = professorMapper.toPreEnrollmentEntity(req, dept);
        pe = profEnrollmentRepo.save(pe);
        log.info("Added new professor pre-enrollment ID: {}", pe.getPreEnrollmentId());

        return professorMapper.toDto(pe);
    }

    @Transactional
    public ProfessorAdminDto updateProfessor(Integer id, ProfessorPreEnrollmentRequest req) {
        String cleanEmail = req.getEmail().trim().toLowerCase();
        String status = req.getStatus() != null ? req.getStatus() : "PARAREGJISTRUAR";

        Department dept = null;
        if (req.getDepartment() != null && !req.getDepartment().trim().isEmpty()) {
            dept = departmentRepo.findByEmerDepartamentiIgnoreCase(req.getDepartment().trim())
                    .orElseGet(() -> departmentRepo.findByEmerDepartamenti(req.getDepartment().trim())
                    .orElse(null));
        }

        if ("VERIFIKUAR".equalsIgnoreCase(status)) {
            Professor professor = profRepo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Pedagogu i verifikuar nuk u gjet."));

            User user = professor.getUser();
            if (user != null) {
                user.setEmri(req.getEmri().trim());
                user.setMbiemri(req.getMbiemri().trim());
                user.setEmail(cleanEmail);
                userRepo.save(user);
            }

            if (dept != null) {
                professor.setDepartment(dept);
            }
            professor = profRepo.save(professor);

            return professorMapper.toDto(professor);
        } else {
            ProfessorPreEnrollment pe = profEnrollmentRepo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Pararegjistrimi i pedagogut nuk u gjet."));

            pe.setEmri(req.getEmri().trim());
            pe.setMbiemri(req.getMbiemri().trim());
            pe.setEmail(cleanEmail);
            if (dept != null) {
                pe.setDepartment(dept);
            }

            pe = profEnrollmentRepo.save(pe);

            return professorMapper.toDto(pe);
        }
    }

    @Transactional
    public void deleteProfessor(Integer id, String status) {
        if ("VERIFIKUAR".equalsIgnoreCase(status)) {
            profRepo.findById(id).ifPresent(prof -> {
                User user = prof.getUser();
                profRepo.delete(prof);
                if (user != null) {
                    userRepo.delete(user);
                }
            });
            log.info("Deleted verified professor ID: {}", id);
        } else {
            profEnrollmentRepo.deleteById(id);
            log.info("Deleted professor pre-enrollment ID: {}", id);
        }
    }

    @Transactional(readOnly = true)
    public List<String> getAllDepartments() {
        return departmentRepo.findAll().stream()
                .map(Department::getEmerDepartamenti)
                .filter(d -> d != null && !d.trim().isEmpty())
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
    }

    @Transactional
    public ProfessorAdminDto verifyAndRegisterDirectly(Integer id) {
        ProfessorPreEnrollment pe = profEnrollmentRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pararegjistrimi i pedagogut me ID " + id + " nuk u gjet."));

        String cleanEmail = pe.getEmail().trim().toLowerCase();

        String tempPass = PasswordGeneratorUtil.generateRandomPassword(8);

        User user = userRepo.findByEmail(cleanEmail).orElse(null);
        if (user == null) {
            Role profRole = roleRepo.findByRoleName("PROFESSOR")
                    .orElseGet(() -> roleRepo.save(Role.builder().roleName("PROFESSOR").build()));

            user = User.builder()
                    .emri(pe.getEmri())
                    .mbiemri(pe.getMbiemri())
                    .email(cleanEmail)
                    .password(passwordEncoder.encode(tempPass))
                    .createdAt(LocalDate.now())
                    .status("ACTIVE")
                    .verified("Y")
                    .changePass("YES")
                    .build();
            user.getRoles().add(profRole);
            user = userRepo.save(user);
        } else {
            user.setPassword(passwordEncoder.encode(tempPass));
            user.setVerified("Y");
            user.setStatus("ACTIVE");
            user.setChangePass("YES");
            user = userRepo.save(user);
        }

        emailService.sendTemporaryPasswordEmail(cleanEmail, user.getEmri(), tempPass);

        final User finalUser = user;
        Professor prof = profRepo.findByUserUserId(finalUser.getUserId()).orElse(null);
        if (prof == null) {
            prof = Professor.builder()
                    .user(finalUser)
                    .department(pe.getDepartment())
                    .status("A")
                    .build();
            prof = profRepo.save(prof);
        } else {
            if (pe.getDepartment() != null) {
                prof.setDepartment(pe.getDepartment());
            }
            prof.setStatus("A");
            prof = profRepo.save(prof);
        }

        log.info("Professor {} {} ({}) directly verified & registered into USERS & PROFESSORS with temp pass (changePass=YES)",
                finalUser.getEmri(), finalUser.getMbiemri(), cleanEmail);

        ProfessorAdminDto dto = professorMapper.toDto(prof);
        dto.setTempPassword(tempPass);
        return dto;
    }
}
