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

    /**
     * Registers a new user in the system.
     *
     * @param user The UserDTO object containing the user's registration details (e.g., username, password, email).
     * @return A ResponseEntity containing a Boolean value indicating whether the registration was successful or not.
     *         - Returns HTTP 200 OK with true if registration is successful.
     *         - Returns HTTP 409 Conflict if the username already exists.
     *         - Returns HTTP 500 Internal Server Error for any other errors during registration.
     */
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

    /**
     * Authenticates a user and generates a JWT token if the provided credentials are valid.
     *
     * @param credentials The UserCredentialsDTO object containing the username and password for authentication.
     * @return A ResponseEntity containing a LoginResponseDTO object with the generated JWT token and user information if authentication is successful, or an appropriate error response if authentication fails.
     *         - Returns HTTP 200 OK with LoginResponseDTO if authentication is successful.
     *         - Returns HTTP 401 Unauthorized if the username or password is incorrect.
     *         - Returns HTTP 500 Internal Server Error for any other errors during authentication.
     */
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

    /**
     * Refreshes an existing JWT token and generates a new one if the provided token is valid.
     *
     * @param token The existing JWT token to be refreshed.
     * @return A ResponseEntity containing a LoginResponseDTO with the new generated token and user information if the refresh is successful, or an appropriate error response if the provided token is invalid or expired.
     *         - Returns HTTP 200 OK with LoginResponseDTO if the token is successfully refreshed.
     *         - Returns HTTP 401 Unauthorized if the token is expired or invalid.
     *         - Returns HTTP 500 Internal Server Error for any other errors during token refresh.
     */
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

    /**
     * Validates a JWT token and returns true if the token is valid, or false if the token is invalid or expired.
     *
     * @param tokenDTO The TokenDTO object containing the JWT token to be validated.
     * @return A ResponseEntity containing a Boolean value indicating whether the token is valid or not.
     *         - Returns HTTP 200 OK with true if the token is valid.
     *         - Returns HTTP 401 Unauthorized if the token is expired or invalid.
     *         - Returns HTTP 500 Internal Server Error for any other errors during token validation.
     */
    @Operation(summary = "Check token", description = "return true if token is valid")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "401", description = "token expired"),
            @ApiResponse(responseCode = "500", description = "Internal server Error")
    })
    @PostMapping("/check-token")
    public ResponseEntity<Boolean> checkToken(@RequestBody TokenDTO tokenDTO) {
        return ResponseEntity.ok(this.jwtService.validateToken(tokenDTO.getToken()));
    }

    /**
     * Changes the password of an authenticated user based on the provided ChangePasswordDTO object.
     *
     * @param changePasswordDTO The ChangePasswordDTO object containing the current password, new password, and confirmation of the new password.
     * @return A ResponseEntity containing a Boolean value indicating whether the password change was successful or not.
     *         - Returns HTTP 200 OK with true if the password was changed successfully.
     *         - Returns HTTP 401 Unauthorized if the old password is incorrect.
     *         - Returns HTTP 404 Not Found if the user is not found.
     *         - Returns HTTP 500 Internal Server Error for any other errors during password change.
     */
    @Operation(summary = "Change password", description = "Change user password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "401", description = "Old password incorrect"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server Error")
    })
    @PostMapping("/change-password")
    public ResponseEntity<Boolean> changePassword(@RequestBody ChangePasswordDTO changePasswordDTO) {
        log.info("change password: {}", changePasswordDTO.toString());
        return this.authService.changePassword(changePasswordDTO);
    }

}
