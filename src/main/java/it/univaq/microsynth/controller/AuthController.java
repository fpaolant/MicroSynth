package it.univaq.microsynth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import it.univaq.microsynth.domain.dto.*;
import it.univaq.microsynth.service.AuthService;
import it.univaq.microsynth.service.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @Operation(summary = "Register user", description = "return message")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "409", description = "username already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server Error")
    })
    @PostMapping("/register")
    public ResponseEntity<Boolean> register(@RequestBody UserDTO user) {
       return this.authService.register(user);
    }

    @Operation(summary = "Login user", description = "return authentication token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "401", description = "username or password incorrect"),
            @ApiResponse(responseCode = "500", description = "Internal server Error")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody UserCredentialsDTO credentials) {
        log.info("credentials: {}", credentials);

        return this.authService.login(credentials);
    }

    @Operation(summary = "Refresh token", description = "return new authentication token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "401", description = "token expired"),
            @ApiResponse(responseCode = "500", description = "Internal server Error")
    })
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refresh(@RequestBody String token) {
        return this.authService.refresh(token);
    }

    @Operation(summary = "Check token", description = "return true if token is valid")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "401", description = "token expired"),
            @ApiResponse(responseCode = "500", description = "Internal server Error")
    })
    @PostMapping("/check-token")
    public ResponseEntity<Boolean> checkToken(@RequestBody TokenDTO tokenDTO) {
        log.info("token: {}", tokenDTO.getToken());
        return ResponseEntity.ok(this.jwtService.validateToken(tokenDTO.getToken()));
    }


}
