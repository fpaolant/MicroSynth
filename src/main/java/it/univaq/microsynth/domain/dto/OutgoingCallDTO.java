package it.univaq.microsynth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(description = "DTO to represent an outgoing call in generate bundle request")
public class OutgoingCallDTO {
    private String operationId;
    private String targetService;
    private String httpMethod;
    private String path;
    private int port = 8080;
    private double weight; // value between 0.1 and 1.0
    private List<OutgoingParamDTO> parameters;

    public String getEnvVarName() {
        return targetService.toUpperCase().replace("-", "_") + "_BASE_URL";
    }
}
