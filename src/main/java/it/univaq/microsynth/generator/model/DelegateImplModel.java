package it.univaq.microsynth.generator.model;


import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class DelegateImplModel {
    private String packageName;
    private String className;
    private List<DelegateOperationModel> operations;
    private Set<String> modelImports;
}
