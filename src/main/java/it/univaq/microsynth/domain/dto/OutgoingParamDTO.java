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
    private String jsonValue;

    public OutgoingParamDTO(String name, Object value) {
        this.name = name;
        this.value = value;
        this.jsonValue = serialize(value);
    }

    private static String serialize(Object v) {
        try {
            return new ObjectMapper().writeValueAsString(v);
        } catch (Exception e) {
            return "null";
        }
    }
}
