package it.univaq.microsynth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import it.univaq.microsynth.domain.dto.PaginatedRequestDTO;
import it.univaq.microsynth.domain.dto.UserResponseDTO;
import it.univaq.microsynth.service.AccountService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }


    /**
     * Retrieves a paginated list of all users in the system.
     *
     * @param page    The page number to retrieve (0-based index).
     * @param size    The number of users to include in each page.
     * @param sortBy  The field by which to sort the users (e.g., "username", "email").
     * @param sortDir The direction of sorting, either "asc" for ascending or "desc" for descending.
     * @return A ResponseEntity containing a Page of UserResponseDTO objects, which includes the list of users and pagination metadata.
     */
    @Operation(
            summary = "Get all users",
            description = "Return paginated list of users"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/all")
    public ResponseEntity<Page<UserResponseDTO>> getAllUsers(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam String sortBy,
            @RequestParam String sortDir) {

        PaginatedRequestDTO paginatedRequestDTO = new PaginatedRequestDTO(page, size, sortBy, sortDir);
        return accountService.getAllUsers(paginatedRequestDTO);
    }

    /**
     * Retrieves a specific user by their unique identifier.
     *
     * @param id The unique identifier of the user to be retrieved.
     * @return A ResponseEntity containing a UserResponseDTO object if the user is found, or an appropriate error response if not found.
     */
    @Operation(summary = "Get user by id", description = "return user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "404", description = "user not found"),
            @ApiResponse(responseCode = "500", description = "Internal server Error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable String id) {
        return accountService.getUserById(id);
    }

    /**
     * Retrieves the roles associated with a specific user by their unique identifier.
     *
     * @param id The unique identifier of the user whose roles are to be retrieved.
     * @return A List of Role objects representing the roles assigned to the specified user.
     */
    @Operation(summary = "Get user roles", description = "return list of user roles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "404", description = "user not found"),
            @ApiResponse(responseCode = "500", description = "Internal server Error")
    })
    @GetMapping("/{id}/roles")
    public ResponseEntity<List<String>> getUserRoles(@PathVariable String id) {
        return ResponseEntity.ok(accountService.getUserRoles(id).stream()
                .map(role -> role.name())
                .toList());
    }

    /**
     * Creates a new user in the system based on the provided UserResponseDTO object.
     *
     * @param user An object containing the information for the new user to be created.
     * @return A ResponseEntity containing a UserResponseDTO object if the user is successfully created, or an appropriate error response if there is an issue during creation (e.g., username already exists).
     */
    @Operation(summary = "Create user", description = "return created user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "500", description = "Internal server Error")
    })
    @PostMapping("/create")
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody UserResponseDTO user) {
        return accountService.createUser(user);
    }

    /**
     * Creates a new user in the system based on the provided UserResponseDTO object.
     *
     * @param user An object containing the information for the new user to be created.
     * @return A ResponseEntity containing a UserResponseDTO object if the user is successfully created, or an appropriate error response if there is an issue during creation (e.g., username already exists).
     */
    @Operation(summary = "Update user", description = "return updated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "404", description = "user not found"),
            @ApiResponse(responseCode = "500", description = "Internal server Error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable String id, @RequestBody UserResponseDTO user) {
        return accountService.updateUser(id, user);
    }

    /**
     * Deletes a user from the system based on their unique identifier.
     *
     * @param id The unique identifier of the user to be deleted.
     * @return A ResponseEntity containing a String message indicating the result of the deletion operation, or an appropriate error response if the user is not found.
     */
    @Operation(summary = "Delete user", description = "return message")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "404", description = "user not found"),
            @ApiResponse(responseCode = "500", description = "Internal server Error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable String id) {
        return accountService.deleteUser(id);
    }

    /**
     * Promotes a user to a higher role (e.g., from USER to ADMIN) based on their unique identifier.
     *
     * @param id The unique identifier of the user to be promoted.
     * @return A ResponseEntity containing a String message indicating the result of the promotion operation, or an appropriate error response if the user is not found or if the promotion fails.
     */
    @Operation(summary = "Promote user", description = "return message")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "404", description = "user not found"),
            @ApiResponse(responseCode = "500", description = "Internal server Error")
    })
    @PutMapping("/{id}/promote")
    public ResponseEntity<String> promoteUser(@PathVariable String id) {
        return accountService.promoteUser(id);
    }

    /**
     * Demotes a user to a lower role (e.g., from ADMIN to USER) based on their unique identifier.
     *
     * @param id The unique identifier of the user to be demoted.
     * @return A ResponseEntity containing a String message indicating the result of the demotion operation, or an appropriate error response if the user is not found or if the demotion fails.
     */
    @Operation(summary = "Demote user", description = "return message")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "404", description = "user not found"),
            @ApiResponse(responseCode = "500", description = "Internal server Error")
    })
    @PutMapping("/{id}/demote")
    public ResponseEntity<String> demoteUser(@PathVariable String id) {
        return accountService.demoteUser(id);
    }
}
