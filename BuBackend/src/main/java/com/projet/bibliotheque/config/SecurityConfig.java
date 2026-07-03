package com.projet.bibliotheque.config;

import com.projet.bibliotheque.security.JwtAuthFilter;
import com.projet.bibliotheque.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.projet.bibliotheque.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final JsonMapper jsonMapper;

    public SecurityConfig(JwtService jwtService, UserDetailsService userDetailsService, JsonMapper jsonMapper) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.jsonMapper = jsonMapper;
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter(jwtService, userDetailsService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Consultation publique du catalogue
                        .requestMatchers(HttpMethod.GET, "/api/livres/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auteurs/**").permitAll()
                        // Authentification
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                        // Console H2 (profil dev)
                        .requestMatchers("/h2-console/**").permitAll()
                        // Gestion du catalogue : bibliothécaire ou admin
                        .requestMatchers(HttpMethod.POST, "/api/livres/**", "/api/auteurs/**")
                        .hasAnyRole("BIBLIOTHECAIRE", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/livres/**", "/api/auteurs/**")
                        .hasAnyRole("BIBLIOTHECAIRE", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/livres/**", "/api/auteurs/**")
                        .hasAnyRole("BIBLIOTHECAIRE", "ADMIN")
                        // Espace de gestion (dashboard, retours, pénalités, tous les emprunts…)
                        .requestMatchers("/api/stats/**", "/api/admin/**").hasAnyRole("BIBLIOTHECAIRE", "ADMIN")
                        // Tout le reste nécessite d'être authentifié
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())) // console H2
                .exceptionHandling(ex -> ex
                        // Non authentifié → 401 (et non 403) pour permettre au front de rediriger vers le login
                        .authenticationEntryPoint((request, response, e) ->
                                ecrireErreur(response, HttpServletResponse.SC_UNAUTHORIZED,
                                        "Authentification requise"))
                        // Authentifié mais privilèges insuffisants → 403
                        .accessDeniedHandler((request, response, e) ->
                                ecrireErreur(response, HttpServletResponse.SC_FORBIDDEN,
                                        "Accès refusé : privilèges insuffisants"))
                )
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private void ecrireErreur(HttpServletResponse response, int status, String message) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        jsonMapper.writeValue(response.getWriter(), ErrorResponse.of(status, message));
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
