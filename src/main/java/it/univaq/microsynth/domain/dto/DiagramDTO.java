package it.univaq.microsynth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import it.univaq.microsynth.domain.DiagramData;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(description = "DTO to represent a diagram")
public class DiagramDTO {

    @Schema(description = "ID", example = "96c1befd426")
    private String id;

    @Schema(description = "Diagram's name", example = "New Diagram")
    private String name;

    @Schema(description = "Diagram's data ")
    private DiagramData data;

}