package it.univaq.microsynth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import it.univaq.microsynth.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Set;


@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
@Schema(description = "DTO to represent a user")
public class UserResponseDTO {
    @Schema(description = "User ID", example = "96c1befd426")
    private String id;

    @Schema(description = "User's username", example = "fabio")
    private String username;

    @Schema(description = "User's first name", example = "Antonio")
    private String firstname;

    @Schema(description = "User's last name", example = "Rossi")
    private String lastname;

    @Schema(description = "User's email", example = "antonio@example.com")
    private String email;

    @Schema(description = "User's roles", example = "[\"ADMIN\", \"USER\"]")
    private Set<Role> roles;

    @Schema(description = "User's creation date", example = "2023-10-01T12:00:00Z")
    private String createdAt;

    @Schema(description = "User's last update date", example = "2023-10-01T12:00:00Z")
    private String updatedAt;
}