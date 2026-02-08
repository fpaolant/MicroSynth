package it.univaq.microsynth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;


/**
 * DTO to represent a paginated request
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(description = "DTO to represent a paginated request")
public class PaginatedRequestDTO {
    int page = 0;
    int size = 10;
    String sortBy = "id";
    String sortDir = "asc";
}
