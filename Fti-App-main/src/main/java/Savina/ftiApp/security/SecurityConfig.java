package Savina.ftiApp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationProvider qe lidh CustomUserDetailsService + BCrypt.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * AuthenticationManager — per authenticate() manual brenda LoginService.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Entry point per API-te: Kur nje request ne /api/** nuk ka Bearer token te
     * vlefshem → kthen 401 JSON.
     */
    private AuthenticationEntryPoint apiEntryPoint() {
        return (HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"Nuk jeni te autentifikuar. Ju lutem kyçuni perseri.\"}");
        };
    }

    /**
     * Rregullat e sigurise (Arkitektura me LocalStorage + Bearer Token): 1.
     * Endpoint-et publike te API-se (login, register, verify) → permitAll() 2.
     * Te gjitha API-te e tjera (/api/**) → authenticated() 3. Faqet HTML dhe
     * resourcet statike (css, js, images) → permitAll() (mbrohen me guard ne
     * frontend dhe nga API-te)
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                // 1. API publike
                .requestMatchers(
                        "/api/login",
                        "/api/register",
                        "/api/register/**",
                        "/api/verify/**",
                        "/api/pedagog/**",
                        "/api/student/**",
                        "/api/admin/**"
                ).permitAll()
                // 2. Te gjitha API-te e tjera kerkojne JWT (Bearer Token)
                .requestMatchers("/api/**").authenticated()
                // 3. Faqet HTML dhe skedaret statike sherbehen lirisht
                .anyRequest().permitAll()
                )
                .exceptionHandling(ex -> ex
                .authenticationEntryPoint(apiEntryPoint())
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
