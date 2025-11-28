package it.univaq.microsynth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(description = "DTO to represent a generate diagram request")
public class DiagramGenerationRequestDTO {
    @Min(value = 1, message = "Number of nodes must be at least 1")
    private int nodes;

    @Min(value = 0, message = "Number of roots cannot be negative")
    private int roots;

    @DecimalMin(value = "0.0", inclusive = true, message = "Density must be >= 0.0")
    @DecimalMax(value = "1.0", inclusive = true, message = "Density must be <= 1.0")
    private double density;
}
