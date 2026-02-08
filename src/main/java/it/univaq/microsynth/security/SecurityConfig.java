package it.univaq.microsynth.security;

import it.univaq.microsynth.service.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtService jwtService;

    public SecurityConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Configures the security filter chain for the application, defining which endpoints are publicly accessible and which require authentication. It also sets up JWT authentication and disables CSRF protection since the application is stateless.
     *
     * @param http The HttpSecurity object used to configure the security settings for the application.
     * @return A SecurityFilterChain object that defines the security configuration for the application.
     * @throws Exception if an error occurs while configuring the security settings.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/check-token",
                                "/api/auth/change-password",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers("/api/account/all").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Defines a PasswordEncoder bean that uses BCrypt for hashing passwords. This encoder will be used to securely store user passwords in the database and to verify password matches during authentication.
     *
     * @return A PasswordEncoder instance that uses BCrypt for password hashing.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Defines a JwtAuthenticationFilter bean that will be used to intercept incoming HTTP requests and validate the JWT token included in the Authorization header. If the token is valid, the filter will set the authentication in the SecurityContext.
     *
     * @return A JwtAuthenticationFilter instance that uses the JwtService for token validation and extraction of user information.
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtService);
    }

    /**
     * Defines an AuthenticationManager bean that is used to handle authentication requests. This bean is required for the authentication process and is obtained from the AuthenticationConfiguration.
     *
     * @param authConfig The AuthenticationConfiguration object that provides access to the AuthenticationManager.
     * @return An AuthenticationManager instance that can be used to authenticate user credentials.
     * @throws Exception if an error occurs while retrieving the AuthenticationManager from the AuthenticationConfiguration.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}