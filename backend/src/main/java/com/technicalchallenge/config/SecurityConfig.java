package com.technicalchallenge.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOriginPatterns(List.of("http://localhost:*" , "http://localhost"));
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        cors.setAllowedHeaders(List.of("*"));
        cors.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // sets up components for URL pattern matching
        HandlerMappingIntrospector introspector = new HandlerMappingIntrospector();
        MvcRequestMatcher.Builder mvcMatcher = new MvcRequestMatcher.Builder(introspector);

        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())) // for H2 console access
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
                        .requestMatchers(
                                new AntPathRequestMatcher("/api/login/**"),
                                new AntPathRequestMatcher("/swagger-ui/**"),
                                new AntPathRequestMatcher("/swagger-ui.html"),
                                new AntPathRequestMatcher("/v3/api-docs/**"),
                                new AntPathRequestMatcher("/api-docs/**"),
                                new AntPathRequestMatcher("/actuator/**")
                        ).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/users/loginId/{loginId}" , "GET")).authenticated()
                        .requestMatchers(new AntPathRequestMatcher("/api/users" , "GET")).hasAnyRole("SUPERUSER","ADMIN")
                        .requestMatchers(new AntPathRequestMatcher("/api/users" , "POST")).hasAnyRole("SUPERUSER","ADMIN")
                        .requestMatchers(new AntPathRequestMatcher("/api/users/{id}" , "PUT")).hasAnyRole("SUPERUSER","ADMIN")
                        .requestMatchers(new AntPathRequestMatcher("/api/users/loginId" , "POST")).hasAnyRole("SUPERUSER","ADMIN")
                        .requestMatchers(new AntPathRequestMatcher("/api/trades" , "GET")).hasAnyRole("SUPERUSER" , "TRADER_SALES" , "MO" , "SUPPORT")
                        .requestMatchers(new AntPathRequestMatcher("/api/trades/{id}" , "GET")).hasAnyRole("SUPERUSER" , "TRADER_SALES" , "MO" , "SUPPORT")
                        .requestMatchers(new AntPathRequestMatcher("/api/trades" , "POST")).hasAnyRole("SUPERUSER" , "TRADER_SALES")
                        .requestMatchers(new AntPathRequestMatcher("/api/trades/{id}" , "PUT")).hasAnyRole("SUPERUSER" , "TRADER_SALES" , "MO")
                        .requestMatchers(new AntPathRequestMatcher("/api/trades/{id}" , "DELETE")).hasAnyRole("SUPERUSER" , "TRADER_SALES")
                        .requestMatchers(new AntPathRequestMatcher("/api/trades/{id}/terminate" , "DELETE")).hasAnyRole("SUPERUSER" , "TRADER_SALES")
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .securityContext(security -> security.securityContextRepository(new HttpSessionSecurityContextRepository()))

                // Grace: session is created after successful authentication and persists for subsequent requests
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(bCryptPasswordEncoder());
        return provider;
    }
}
