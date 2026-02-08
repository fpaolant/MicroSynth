package it.univaq.microsynth.security;

import it.univaq.microsynth.service.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Extracts the JWT token from the Authorization header, validates it, and if valid, sets the authentication in the SecurityContext.
     *
     * @param request  The incoming HTTP request containing the JWT token in the Authorization header.
     * @param response The HTTP response to be sent back to the client.
     * @param chain    The filter chain to pass the request and response to the next filter in the chain.
     * @throws ServletException if an error occurs during the filtering process.
     * @throws IOException      if an I/O error occurs during the filtering process.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String token = extractTokenFromRequest(request);

        if (token != null && jwtService.validateToken(token)) {
            String username = jwtService.extractUsername(token);
            List<String> roles = jwtService.extractRoles(token);

            var authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        chain.doFilter(request, response);
    }

    /**
     * Extracts the JWT token from the Authorization header of the incoming HTTP request. The expected format of the header is "
     * Bearer <token>". If the header is present and starts with "Bearer ", the method returns the token part of the header. Otherwise, it returns null.
     * @param request The incoming HTTP request from which to extract the JWT token.
     * @return The extracted JWT token if the Authorization header is present and properly formatted, or null if the header is missing or does not start with "Bearer ".
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
