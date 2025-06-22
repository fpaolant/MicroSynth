package it.univaq.microsynth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(description = "DTO to represent user credentials")
public class UserCredentialsDTO {

    @Schema(description = "User's username", example = "fabio")
    private String username;

    @Schema(description = "User's password", example = "password")
    private String password;

}