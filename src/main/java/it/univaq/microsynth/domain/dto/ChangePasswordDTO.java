package it.univaq.microsynth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;


/**
 * DTO to represent a change password request
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(description = "DTO to represent a change password request")
public class ChangePasswordDTO {
    private String username;
    private String oldPassword;
    private String newPassword;
}
