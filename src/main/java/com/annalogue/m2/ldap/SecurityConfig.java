package com.annalogue.m2.ldap;

import com.annalogue.m2.services.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

        private final CustomUserDetailsService userDetailsService;

        public SecurityConfig(CustomUserDetailsService userDetailsService) {
                this.userDetailsService = userDetailsService;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http)
                        throws Exception {

                http
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/login", "/css/**", "/js/**", "/error").permitAll()
                                                .requestMatchers("/upload").hasAnyRole("ADMIN", "PROF", "RESPONSABLE")
                                                .requestMatchers("/admin").hasRole("ADMIN")
                                                .requestMatchers("/members/**").hasRole("ADMIN")
                                                .requestMatchers("/modules/add").hasRole("RESPONSABLE")
                                                .requestMatchers("/module-enrollments/new").hasRole("RESPONSABLE")
                                                .requestMatchers("/module-enrollments/group").hasRole("RESPONSABLE")
                                                .requestMatchers("/module-enrollments/group/new").hasRole("RESPONSABLE")
                                                .requestMatchers("/module-enrollments")
                                                .hasAnyRole("ADMIN", "RESPONSABLE")
                                                .requestMatchers("/module-enrollments/{id}/delete")
                                                .hasAnyRole("ADMIN", "RESPONSABLE")
                                                .requestMatchers("/module-enrollments/{id}/toggle-editor")
                                                .hasAnyRole("ADMIN", "RESPONSABLE")
                                                .anyRequest().authenticated())
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        response.sendRedirect("/login?required");
                                                }))
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .defaultSuccessUrl("/home", true)
                                                .failureUrl("/login?error")
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutSuccessUrl("/login?logout")
                                                .permitAll())
                                .sessionManagement(session -> session
                                                .sessionFixation().newSession()
                                                .maximumSessions(1));

                return http.build();
        }
}