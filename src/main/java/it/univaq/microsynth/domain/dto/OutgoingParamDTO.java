package it.univaq.microsynth.domain.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OutgoingParamDTO {
    private String name;
    private Object value;
}
