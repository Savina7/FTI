package Savina.ftiApp.integration;

import Savina.ftiApp.dto.requestDTO.LoginRequest;
import Savina.ftiApp.dto.requestDTO.StudentPreEnrollmentRequest;
import Savina.ftiApp.dto.requestDTO.StudentRegisterRequest;
import Savina.ftiApp.dto.requestDTO.VerifyRequest;
import Savina.ftiApp.entity.Program;
import Savina.ftiApp.entity.StudentPreEnrollment;
import Savina.ftiApp.entity.User;
import Savina.ftiApp.repository.ProgramRepository;
import Savina.ftiApp.repository.StudentPreEnrollmentRepository;
import Savina.ftiApp.repository.StudentRepository;
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
public class StudentLifecycleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StudentPreEnrollmentRepository preEnrollmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ProgramRepository programRepository;

    @MockBean
    private EmailService emailService;

    @Test
    @DisplayName("Rrjedha e Plote e Studentit: Admin Pre-Enrollment -> Vet-Regjistrimi -> Verifikimi me OTP -> Login -> Aksesi ne Profil")
    void testCompleteStudentLifecycleFlow() throws Exception {
        // Prevent actual SMTP mail sending during test
        doNothing().when(emailService).sendVerificationCode(anyString(), anyString(), anyString());

        Program program = programRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Nuk u gjet asnje program ne DB per testim."));

        long suffix = System.currentTimeMillis() % 1000000;
        String uniqueMatrikull = String.format("MAT%09d", suffix); // 12 characters exactly
        String testEmail = "arbi.test" + suffix + "@fti.edu.al";
        String testPassword = "Password123!";

        // =========================================================================
        // HAPI 1: Admin regjistron studentin ne Pre-Enrollment
        // =========================================================================
        StudentPreEnrollmentRequest preReq = new StudentPreEnrollmentRequest();
        preReq.setEmri("Arbi");
        preReq.setMbiemri("Hoxha");
        preReq.setEmail(testEmail);
        preReq.setNrMatrikulimit(uniqueMatrikull);
        preReq.setProgramId(program.getProgramId());
        preReq.setVitStudimit(1);

        mockMvc.perform(post("/api/admin/students/pre-enrollment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(preReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(testEmail))
                .andExpect(jsonPath("$.status").value("PARAREGJISTRUAR"));

        StudentPreEnrollment savedPre = preEnrollmentRepository.findByNrMatrikulimit(uniqueMatrikull)
                .orElseThrow(() -> new AssertionError("Studenti duhej te ruhej ne STUDENT_PRE_ENROLLMENT"));
        assertThat(savedPre.getEmail()).isEqualTo(testEmail);
        assertThat(savedPre.getStatus()).isEqualTo("PARAREGJISTRUAR");

        // =========================================================================
        // HAPI 2: Studenti kryen vete-regjistrimin me te dhenat e tij
        // =========================================================================
        StudentRegisterRequest regReq = new StudentRegisterRequest();
        regReq.setEmri("Arbi");
        regReq.setMbiemri("Hoxha");
        regReq.setEmail(testEmail);
        regReq.setNrMatrikulimit(uniqueMatrikull);
        regReq.setPassword(testPassword);

        mockMvc.perform(post("/api/register/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(testEmail))
                .andExpect(jsonPath("$.role").value("STUDENT"));

        User pendingUser = userRepository.findByEmail(testEmail)
                .orElseThrow(() -> new AssertionError("Perdoruesi duhej te krijohej ne tabelen USERS"));
        assertThat(pendingUser.getStatus()).isEqualTo("PENDING");
        assertThat(pendingUser.getVerified()).isEqualTo("N");
        assertThat(pendingUser.getVerificationCode()).isNotNull();

        String otpCode = pendingUser.getVerificationCode();
        Integer userId = pendingUser.getUserId();

        // =========================================================================
        // HAPI 3: Verifikimi i kodit OTP nga studenti
        // =========================================================================
        VerifyRequest verifyReq = new VerifyRequest();
        verifyReq.setUserId(userId);
        verifyReq.setCode(otpCode);

        MvcResult verifyResult = mockMvc.perform(post("/api/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andReturn();

        User verifiedUser = userRepository.findByEmail(testEmail).orElseThrow();
        assertThat(verifiedUser.getStatus()).isEqualTo("ACTIVE");
        assertThat(verifiedUser.getVerified()).isEqualTo("Y");
        assertThat(studentRepository.findByUserUserId(userId)).isPresent();

        // =========================================================================
        // HAPI 4: Login i studentit me Email dhe Password
        // =========================================================================
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail(testEmail);
        loginReq.setPassword(testPassword);

        MvcResult loginResult = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.email").value(testEmail))
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(responseJson);
        String jwtToken = rootNode.get("token").asText();
        assertThat(jwtToken).isNotBlank();

        // =========================================================================
        // HAPI 5: Aksesi ne profilin e studentit me JWT Token te sapo-gjeneruar
        // =========================================================================
        mockMvc.perform(get("/api/student/me")
                        .param("email", testEmail)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emriMbiemri").value("Arbi Hoxha"))
                .andExpect(jsonPath("$.email").value(testEmail))
                .andExpect(jsonPath("$.nrMatrikulimit").value(uniqueMatrikull));
    }
}
