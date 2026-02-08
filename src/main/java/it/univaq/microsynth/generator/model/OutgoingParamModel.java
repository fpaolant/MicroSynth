package it.univaq.microsynth.generator.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OutgoingParamModel {

    private String name;
    private Object jsonValueLiteral;
}
