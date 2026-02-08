package it.univaq.microsynth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;


/**
 * DTO to represent a user
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(description = "DTO to represent a user")
public class UserDTO {
    @Schema(description = "User's first name", example = "Antonio")
    private String firstname;

    @Schema(description = "User's last name", example = "Rossi")
    private String lastname;

    @Schema(description = "User's email", example = "antonio@example.com")
    private String email;

    @Schema(description = "User's username", example = "fabio")
    private String username;

    @Schema(description = "User's password", example = "password")
    private String password;

}