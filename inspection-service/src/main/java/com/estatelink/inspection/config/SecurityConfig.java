package com.estatelink.inspection.config;

import com.estatelink.inspection.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Stateless JWT security. Role prefixes are stripped (roles come from the
 * token as plain names, e.g. "AGENT"), so path rules use unprefixed names.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/inspection-slots/listing/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/inspection-slots/**").hasRole("AGENT")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/inspection-slots/**").hasRole("AGENT")
                .requestMatchers(HttpMethod.POST, "/api/v1/inspection-requests/**").hasRole("APPLICANT")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/inspection-requests/{id}/accept").hasRole("AGENT")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/inspection-requests/{id}/decline").hasRole("AGENT")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/inspection-requests/**").hasRole("APPLICANT")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
