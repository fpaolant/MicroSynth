package it.univaq.microsynth.generator.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OutgoingCallModel {
    private String operationId;
    private String httpMethod;
    private double weight;
    private String url;
    private List<OutgoingParamModel> parameters;
}
