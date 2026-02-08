package it.univaq.microsynth.domain.dto;

import lombok.*;

/**
 * DTO to represent an outgoing parameter
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class OutgoingParamDTO {
    private String name;
    private Object value;

    public OutgoingParamDTO(String name, Object value) {
        this.name = name;
        this.value = value;
    }
}
