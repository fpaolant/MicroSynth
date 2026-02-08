package it.univaq.microsynth.generator.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString
public class DelegateBodyModel {
    private String javaType;              // es: Path1PostRequest
    private Map<String, String> fields;   // id -> String, active -> Boolean
}