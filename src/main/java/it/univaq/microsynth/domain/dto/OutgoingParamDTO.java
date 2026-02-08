package it.univaq.microsynth.domain.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

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
