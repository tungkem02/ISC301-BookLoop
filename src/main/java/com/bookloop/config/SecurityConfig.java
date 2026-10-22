package com.bookloop.config;

import com.bookloop.service.BookloopUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Spring Security configuration for BookLoop.
 * Uses modern lambda-style configuration (not the deprecated WebSecurityConfigurerAdapter).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final BookloopUserDetailsService userDetailsService;

    public SecurityConfig(BookloopUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * BCrypt password encoder bean.
     * Used throughout the application to hash and verify passwords.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Defines the security filter chain — access rules, login, and logout.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public pages: static assets, home, listings, login, registration
                .requestMatchers(
                        "/",
                        "/books",
                        "/books/**",
                        "/register",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/webjars/**",
                        "/error"
                ).permitAll()
                // Everything else requires authentication
                .anyRequest().authenticated()
            )
            // Form login configuration
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            // Logout configuration
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            // CSRF is enabled by default — we do NOT disable it
            // Thymeleaf automatically includes the CSRF token in forms via th:action
            .userDetailsService(userDetailsService);

        return http.build();
    }
}
