package it.univaq.microsynth.service;


import it.univaq.microsynth.domain.Role;
import it.univaq.microsynth.domain.dto.PaginatedRequestDTO;
import it.univaq.microsynth.domain.dto.UserResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.util.List;


public interface AccountService {
    /**
     * Retrieves a paginated list of all users in the system.
     *
     * @param paginatedRequestDTO An object containing pagination parameters such as page number, page size, sorting field, and sorting direction.
     * @return A ResponseEntity containing a Page of UserResponseDTO objects, which includes the list of users and pagination metadata.
     */
    ResponseEntity<Page<UserResponseDTO>> getAllUsers(PaginatedRequestDTO paginatedRequestDTO);

    /**
     * Retrieves the roles associated with a specific user by their unique identifier.
     *
     * @param id The unique identifier of the user whose roles are to be retrieved.
     * @return A List of Role objects representing the roles assigned to the specified user.
     */
    List<Role> getUserRoles(String id);

    /**
     * Retrieves a specific user by their unique identifier.
     *
     * @param id The unique identifier of the user to be retrieved.
     * @return A ResponseEntity containing a UserResponseDTO object if the user is found, or an appropriate error response if not found.
     */
    ResponseEntity<UserResponseDTO> getUserById(String id);

    /**
     * Updates an existing user's information based on the provided UserResponseDTO object.
     *
     * @param id   The unique identifier of the user to be updated.
     * @param user An object containing the updated information for the user.
     * @return A ResponseEntity containing a UserResponseDTO object if the update is successful, or an appropriate error response if the user is not found.
     */
    ResponseEntity<UserResponseDTO> updateUser(String id, UserResponseDTO user);

    /**
     * Deletes a user from the system based on their unique identifier.
     *
     * @param id The unique identifier of the user to be deleted.
     * @return A ResponseEntity containing a String message indicating the result of the deletion operation, or an appropriate error response if the user is not found.
     */
    ResponseEntity<String> deleteUser(String id);

    /**
     * Promotes a user to a higher role (e.g., from USER to ADMIN) based on their unique identifier.
     *
     * @param id The unique identifier of the user to be promoted.
     * @return A ResponseEntity containing a String message indicating the result of the promotion operation, or an appropriate error response if the user is not found or if the promotion fails.
     */
    ResponseEntity<String> promoteUser(String id);

    /**
     * Demotes a user to a lower role (e.g., from ADMIN to USER) based on their unique identifier.
     *
     * @param id The unique identifier of the user to be demoted.
     * @return A ResponseEntity containing a String message indicating the result of the demotion operation, or an appropriate error response if the user is not found or if the demotion fails.
     */
    ResponseEntity<String> demoteUser(String id);

    /**
     * Creates a new user in the system based on the provided UserResponseDTO object.
     *
     * @param user An object containing the information for the new user to be created.
     * @return A ResponseEntity containing a UserResponseDTO object if the user is successfully created, or an appropriate error response if there is an issue during creation (e.g., username already exists).
     */
    ResponseEntity<UserResponseDTO> createUser(UserResponseDTO user);
}
