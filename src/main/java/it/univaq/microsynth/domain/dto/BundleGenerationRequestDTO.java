package it.univaq.microsynth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(description = "DTO to represent a generate bundle request")
public class BundleGenerationRequestDTO {
    private String type; // es. "python", "spring", "nodejs"
    private Map<String, Object> apiSpec; // OpenAPI specification as a Map
    private String projectName;
    private List<OutgoingCallDTO> outgoingCalls;
    private Boolean initiator; // true if this request is the initiator of the generation process, false otherwise

}
