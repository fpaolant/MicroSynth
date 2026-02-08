package it.univaq.microsynth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;


/**
 * DTO to represent a project
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(description = "DTO to represent a project")
public class ProjectDTO {
    @Schema(description = "ID", example = "96c1befd426", nullable = true)
    private String id;

    @Schema(description = "Project's name", example = "New Project")
    @NotNull(message = "Project's name cannot be null")
    private String name;

    private String userName;

    @Schema(description = "Project's diagram")
    private List<DiagramDTO> diagrams = List.of();

}