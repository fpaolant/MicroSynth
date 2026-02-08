package it.univaq.microsynth.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import it.univaq.microsynth.domain.User;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private Key secretKey;

    @PostConstruct
    public void init() {
        byte[] decodedKey = Base64.getDecoder().decode(secret);
        if (decodedKey.length < 32) {
            throw new IllegalArgumentException("The secret key must be at least 256 bits long.");
        }
        this.secretKey = Keys.hmacShaKeyFor(decodedKey);
    }

    /**
     * Generates a JWT token for the given user, including their username, user ID, and roles as claims.
     *
     * @param user The user for whom the token is being generated.
     * @return A JWT token as a String.
     */
    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getId())
                .claim("roles", user.getRoles())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates the provided JWT token by checking its signature and expiration.
     *
     * @param token The JWT token to be validated.
     * @return true if the token is valid, false otherwise.
     */
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException e) {
            log.warn("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extracts the username (subject) from the provided JWT token.
     *
     * @param token The JWT token from which to extract the username.
     * @return The username contained in the token, or null if the token is invalid.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the roles from the provided JWT token.
     *
     * @param token The JWT token from which to extract the roles.
     * @return A List of roles contained in the token, or null if the token is invalid.
     */
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("roles", List.class);
    }

    /**
     * Extracts the user ID from the provided JWT token.
     *
     * @param token The JWT token from which to extract the user ID.
     * @return The user ID contained in the token, or null if the token is invalid.
     */
    public String extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userId", String.class);
    }

    /**
     * Extracts a specific claim from the provided JWT token using a claims resolver function.
     *
     * @param token          The JWT token from which to extract the claim.
     * @param claimsResolver A function that takes Claims as input and returns the desired claim.
     * @param <T>            The type of the claim to be extracted.
     * @return The extracted claim of type T, or null if the token is invalid.
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from the provided JWT token by parsing it with the secret key.
     *
     * @param token The JWT token from which to extract the claims.
     * @return A Claims object containing all the claims extracted from the token.
     * @throws JwtException If the token is invalid or cannot be parsed.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
    }

}
