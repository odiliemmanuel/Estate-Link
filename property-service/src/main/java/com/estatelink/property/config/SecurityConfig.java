package com.estatelink.property.config;

import com.estatelink.property.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    // anyone can browse active listings
                    .requestMatchers(HttpMethod.GET, "/api/v1/listings").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/listings/{id}").permitAll()
                    // property profiles are public so the browse filters
                    // (location/type/images) work for guests too
                    .requestMatchers(HttpMethod.GET, "/api/v1/properties").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/properties/{id}").permitAll()
                    // uploaded images are static and public; uploads (POST) need a JWT
                    .requestMatchers(HttpMethod.GET, "/api/v1/uploads/**").permitAll()
                    // error forwards (missing static files) are public
                    .requestMatchers("/error").permitAll()
                    // admin approval endpoints
                    .requestMatchers("/api/v1/listings/{id}/approve").hasRole("ADMIN")
                    .requestMatchers("/api/v1/listings/{id}/reject").hasRole("ADMIN")
                    // everything else needs a valid JWT
                    .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
