package Savina.ftiApp.system;

import Savina.ftiApp.repository.DepartmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SystemUiNavigationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Sistemi: Rruga kryesore '/' ben redirect automatik te '/login'")
    void testRootRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("Sistemi: Faqja e Kyçjes '/login' ngarkohet me sukses (Status 200)")
    void testLoginPageLoads() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    @DisplayName("Sistemi: Faqja e Regjistrimit '/register' ngarkohet me sukses bashke me departamentet")
    void testRegisterPageLoads() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("departments"));
    }

    @Test
    @DisplayName("Sistemi: Faqja e Harreses se Fjalekalimit '/forgot-password' ngarkohet me sukses")
    void testForgotPasswordPageLoads() throws Exception {
        mockMvc.perform(get("/forgot-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/forgot-password"));
    }

    @Test
    @DisplayName("Sistemi: Faqja e Rivendosjes se Fjalekalimit '/reset-password' ngarkohet me sukses")
    void testResetPasswordPageLoads() throws Exception {
        mockMvc.perform(get("/reset-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/reset-password"));
    }

    @Test
    @DisplayName("Sistemi: Paneli kryesor i Studentit '/student' ngarkohet me sukses")
    void testStudentPortalLoads() throws Exception {
        mockMvc.perform(get("/student"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/index"));
    }

    @Test
    @DisplayName("Sistemi: Faqja e Notave te Studentit '/nota' ngarkohet me sukses")
    void testStudentGradesPageLoads() throws Exception {
        mockMvc.perform(get("/nota"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/notat"));
    }

    @Test
    @DisplayName("Sistemi: Paneli i Administratorit '/administrator' ngarkohet me sukses")
    void testAdministratorDashboardLoads() throws Exception {
        mockMvc.perform(get("/administrator"))
                .andExpect(status().isOk())
                .andExpect(view().name("administrator/dashboard_reports"));
    }

    @Test
    @DisplayName("Sistemi: Menaxhimi i Orareve '/course_schedule' ngarkohet me sukses")
    void testCourseSchedulePageLoads() throws Exception {
        mockMvc.perform(get("/course_schedule"))
                .andExpect(status().isOk())
                .andExpect(view().name("administrator/course_schedule"));
    }

    @Test
    @DisplayName("Sistemi: Regjistri i Pedagogut '/petagog' ngarkohet me sukses")
    void testPedagogRegisterPageLoads() throws Exception {
        mockMvc.perform(get("/petagog"))
                .andExpect(status().isOk())
                .andExpect(view().name("petagog/rregjistri"));
    }
}
