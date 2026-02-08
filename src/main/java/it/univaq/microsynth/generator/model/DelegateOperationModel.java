package it.univaq.microsynth.generator.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class DelegateOperationModel {
    private String operationId;
    private String httpMethod;
    private String methodSignature;
    private String logLine;
    private List<DelegateParamModel> parameters;
    private DelegateBodyModel body;
    private List<OutgoingCallModel> outgoingCalls;
}
