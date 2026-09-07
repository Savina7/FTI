package Savina.ftiApp.integration;

import Savina.ftiApp.dto.requestDTO.LoginRequest;
import Savina.ftiApp.dto.requestDTO.ProfessorPreEnrollmentRequest;
import Savina.ftiApp.dto.requestDTO.ProfessorRegisterRequest;
import Savina.ftiApp.dto.requestDTO.VerifyRequest;
import Savina.ftiApp.entity.Department;
import Savina.ftiApp.entity.ProfessorPreEnrollment;
import Savina.ftiApp.entity.User;
import Savina.ftiApp.repository.DepartmentRepository;
import Savina.ftiApp.repository.ProfessorPreEnrollmentRepository;
import Savina.ftiApp.repository.ProfessorRepository;
import Savina.ftiApp.repository.UserRepository;
import Savina.ftiApp.service.EmailService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ProfessorLifecycleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProfessorPreEnrollmentRepository profPreEnrollmentRepo;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @MockBean
    private EmailService emailService;

    @Test
    @DisplayName("Rrjedha e Plote e Pedagogut: Admin Pre-Enrollment -> Vet-Regjistrimi -> Verifikimi me OTP -> Login -> Aksesi ne Portal")
    void testCompleteProfessorLifecycleFlow() throws Exception {
        doNothing().when(emailService).sendVerificationCode(anyString(), anyString(), anyString());

        Department department = departmentRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Nuk u gjet asnje departament ne DB per testim."));

        long suffix = System.currentTimeMillis() % 1000000;
        String testEmail = "prof.test" + suffix + "@fti.edu.al";
        String testPassword = "Password123!";

        // =========================================================================
        // HAPI 1: Admin regjistron pedagogun ne Pre-Enrollment
        // =========================================================================
        ProfessorPreEnrollmentRequest preReq = new ProfessorPreEnrollmentRequest();
        preReq.setEmri("Dritan");
        preReq.setMbiemri("Balla");
        preReq.setEmail(testEmail);
        preReq.setDepartment(department.getEmerDepartamenti());

        mockMvc.perform(post("/api/admin/professors/pre-enrollment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(preReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(testEmail))
                .andExpect(jsonPath("$.status").value("PARAREGJISTRUAR"));

        ProfessorPreEnrollment savedPre = profPreEnrollmentRepo.findByEmail(testEmail)
                .orElseThrow(() -> new AssertionError("Pedagogu duhej te ruhej ne PROFESSOR_PRE_ENROLLMENT"));
        assertThat(savedPre.getEmri()).isEqualTo("Dritan");

        // =========================================================================
        // HAPI 2: Pedagogu kryen vete-regjistrimin me te dhenat e tij
        // =========================================================================
        ProfessorRegisterRequest regReq = new ProfessorRegisterRequest();
        regReq.setEmri("Dritan");
        regReq.setMbiemri("Balla");
        regReq.setEmail(testEmail);
        regReq.setDepartment(department.getEmerDepartamenti());
        regReq.setPassword(testPassword);

        mockMvc.perform(post("/api/register/professor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(testEmail))
                .andExpect(jsonPath("$.role").value("PROFESSOR"));

        User pendingUser = userRepository.findByEmail(testEmail)
                .orElseThrow(() -> new AssertionError("Perdoruesi duhej te krijohej ne tabelen USERS"));
        assertThat(pendingUser.getStatus()).isEqualTo("PENDING");
        assertThat(pendingUser.getVerified()).isEqualTo("N");
        assertThat(pendingUser.getVerificationCode()).isNotNull();

        String otpCode = pendingUser.getVerificationCode();
        Integer userId = pendingUser.getUserId();

        // =========================================================================
        // HAPI 3: Verifikimi i kodit OTP nga pedagogu
        // =========================================================================
        VerifyRequest verifyReq = new VerifyRequest();
        verifyReq.setUserId(userId);
        verifyReq.setCode(otpCode);

        mockMvc.perform(post("/api/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("PROFESSOR"));

        User verifiedUser = userRepository.findByEmail(testEmail).orElseThrow();
        assertThat(verifiedUser.getStatus()).isEqualTo("ACTIVE");
        assertThat(verifiedUser.getVerified()).isEqualTo("Y");
        assertThat(professorRepository.findByUserUserId(userId)).isPresent();

        // =========================================================================
        // HAPI 4: Login i pedagogut me Email dhe Password
        // =========================================================================
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail(testEmail);
        loginReq.setPassword(testPassword);

        MvcResult loginResult = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("PROFESSOR"))
                .andExpect(jsonPath("$.email").value(testEmail))
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(responseJson);
        String jwtToken = rootNode.get("token").asText();
        assertThat(jwtToken).isNotBlank();

        // =========================================================================
        // HAPI 5: Aksesi ne portalin e pedagogut me JWT Token te sapo-gjeneruar
        // =========================================================================
        mockMvc.perform(get("/api/pedagog/options")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courses").isArray());
    }
}
