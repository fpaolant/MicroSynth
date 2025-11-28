package it.univaq.microsynth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(description = "DTO to represent an outgoing call in generate bundle request")
public class OutgoingCallDTO {
    private String targetService;
    private String httpMethod;
    private String path;
    private double weight; // value between 0.1 and 1.0
    private Map<String, Object> parameters;
}
