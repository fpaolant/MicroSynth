package it.univaq.microsynth.service;


import it.univaq.microsynth.domain.dto.ChangePasswordDTO;
import it.univaq.microsynth.domain.dto.LoginResponseDTO;
import it.univaq.microsynth.domain.dto.UserCredentialsDTO;
import it.univaq.microsynth.domain.dto.UserDTO;
import org.springframework.http.ResponseEntity;


public interface AuthService {

    /**
     * Registers a new user in the system.
     *
     * @param user The UserDTO containing the user's registration information.
     * @return A ResponseEntity containing a Boolean indicating the success of the registration.
     */
    ResponseEntity<Boolean> register(UserDTO user);

    /**
     * Authenticates a user and generates a JWT token if the credentials are valid.
     *
     * @param credentials The UserCredentialsDTO containing the username and password for authentication.
     * @return A ResponseEntity containing a LoginResponseDTO with the generated token and user information if authentication is successful, or an appropriate error response if authentication fails.
     */
    ResponseEntity<LoginResponseDTO> login(UserCredentialsDTO credentials);

    /**
     * Refreshes an existing JWT token and generates a new one if the provided token is valid.
     *
     * @param token The existing JWT token to be refreshed.
     * @return A ResponseEntity containing a LoginResponseDTO with the new generated token and user information if the refresh is successful, or an appropriate error response if the provided token is invalid or expired.
     */
    ResponseEntity<LoginResponseDTO> refresh(String token);

    /**
     * Changes the password of an authenticated user.
     *
     * @param changePasswordDTO The ChangePasswordDTO containing the current password, new password, and confirmation of the new password.
     * @return A ResponseEntity containing a Boolean indicating the success of the password change operation, or an appropriate error response if the current password is incorrect, if the new password does not meet security requirements, or if the new password and confirmation do not match.
     */
    ResponseEntity<Boolean> changePassword(ChangePasswordDTO changePasswordDTO);

}
