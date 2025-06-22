package it.univaq.microsynth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(description = "DTO to represent a generic document")
public class DocumentResponseDTO {
    @Schema(description = "ID", example = "96c1befd426")
    private String id;
}
