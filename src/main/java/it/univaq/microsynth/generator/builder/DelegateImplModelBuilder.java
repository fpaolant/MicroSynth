package it.univaq.microsynth.generator.builder;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.lang.Strings;
import it.univaq.microsynth.domain.dto.BundleGenerationRequestDTO;
import it.univaq.microsynth.domain.dto.OutgoingCallDTO;
import it.univaq.microsynth.domain.dto.OutgoingParamDTO;
import it.univaq.microsynth.generator.model.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Builder for the model of the delegate implementation class, which will contain the logic of the operations and the outgoing calls
 * NOTE: Used only in Spring generator
 */
@Component
public class DelegateImplModelBuilder {

    /**
     * Build the model for the delegate implementation class, by parsing the OpenAPI specification and the outgoing calls
     */
     @SuppressWarnings("unchecked")
    public DelegateImplModel build(BundleGenerationRequestDTO request) {
        DelegateImplModel model = new DelegateImplModel();

        model.setPackageName("org.openapitools.api.impl");
        model.setClassName("DefaultApiDelegateImpl");

        // import for model classes used in request bodies
        Set<String> modelImports = new HashSet<>();

        // operations
        List<DelegateOperationModel> operations = new ArrayList<>();

        Map<String, Object> paths =
                (Map<String, Object>) request.getApiSpec().get("paths");

        for (var pathEntry : paths.entrySet()) {
            Map<String, Object> methods =
                    (Map<String, Object>) pathEntry.getValue();

            for (var methodEntry : methods.entrySet()) {
                Map<String, Object> op =
                        (Map<String, Object>) methodEntry.getValue();
                // operation
                DelegateOperationModel om = new DelegateOperationModel();
                om.setOperationId((String) op.get("operationId"));
                om.setHttpMethod(methodEntry.getKey().toUpperCase());

                // operation parameters
                List<DelegateParamModel> params = new ArrayList<>();
                List<Map<String,Object>> openapiParams =
                        (List<Map<String,Object>>) op.get("parameters");


                if (openapiParams != null) {
                    for (Map<String,Object> p : openapiParams) {
                        DelegateParamModel pm = new DelegateParamModel();
                        pm.setName((String) p.get("name"));
                        pm.setJavaType(mapType(p));
                        params.add(pm);
                    }
                }
                om.setParameters(params);

                String methodSignature = "";
                String logLine = "log.info(\"[IN] Handling " + om.getOperationId();

                if (!params.isEmpty()) {
                    methodSignature = params.stream()
                            .map(p -> p.getJavaType() + " " + p.getName())
                            .collect(Collectors.joining(", "));

                    String logParams = params.stream()
                            .map(DelegateParamModel::getName)
                            .collect(Collectors.joining(", "));
                    logLine += " with params: \"" + logParams + "\" ";
                }

                // operation request body
                DelegateBodyModel body = mapRequestBody(op);
                om.setBody(body);
                methodSignature += (methodSignature.isEmpty() ? "" : ", ")
                        + body.getJavaType() + " body";
                modelImports.add(body.getJavaType());

                logLine += " with body: " + body.toString() + "\");";
                om.setLogLine(logLine);

                om.setMethodSignature(methodSignature);

                // outgoing calls inside operation
                om.setOutgoingCalls(
                    mapOutgoingCalls(request.getOutgoingCalls(), request.getType())
                );
                // add operation to operations list
                operations.add(om);
            }
        }
        model.setModelImports(modelImports);
        model.setOperations(operations);
        return model;
    }

    /**
     * Map an OpenAPI parameter schema to a Java type
     */
    @SuppressWarnings("unchecked")
    private String mapType(Map<String, Object> p) {
        if (p == null) {
            return "Object";
        }

        Object schemaObj = p.get("schema");
        if (!(schemaObj instanceof Map)) {
            return "Object";
        }

        Map<String, Object> schema = (Map<String, Object>) schemaObj;
        String type = (String) schema.get("type");
        String format = (String) schema.get("format");

        if (type == null) {
            return "Object";
        }

        switch (type) {
            case "string":
                // formati OpenAPI standard
                if ("uuid".equals(format)) {
                    return "UUID";
                }
                if ("date".equals(format)) {
                    return "LocalDate";
                }
                if ("date-time".equals(format)) {
                    return "OffsetDateTime";
                }
                return "String";
            case "integer":
                if ("int64".equals(format)) {
                    return "Long";
                }
                return "Integer";
            case "number":
                if ("float".equals(format)) {
                    return "Float";
                }
                if ("double".equals(format)) {
                    return "Double";
                }
                return "Double";
            case "boolean":
                return "Boolean";
            case "array":
                Object itemsObj = schema.get("items");
                if (itemsObj instanceof Map) {
                    String itemType = mapType(Map.of("schema", itemsObj));
                    return "List<" + itemType + ">";
                }
                return "List<Object>";
            case "object":
                // oggetti inline → Map
                return "Map<String, Object>";
            default:
                return "Object";
        }
    }

    /**
     * Map the list of outgoing calls to a list of OutgoingCallModel, which will be used to generate the code for the outgoing calls
     */
    private List<OutgoingCallModel> mapOutgoingCalls(
            List<OutgoingCallDTO> outgoingCalls,
            String type
    ) {
        List<OutgoingCallModel> result = new ArrayList<>();

        if (outgoingCalls == null) {
            return result;
        }

        for (OutgoingCallDTO dto : outgoingCalls) {

            OutgoingCallModel model = new OutgoingCallModel();

            // ================= METADATA =================
            model.setOperationId(dto.getOperationId());
            model.setHttpMethod(dto.getHttpMethod().toUpperCase());
            model.setWeight(dto.getWeight());

            // URL completa (baseUrl + path)
            model.setUrl(dto.getBaseUrl() + dto.getPath());

            // ================= PARAMETRI =================
            List<OutgoingParamModel> paramModels = new ArrayList<>();

            if (dto.getParameters() != null) {
                for (OutgoingParamDTO p : dto.getParameters()) {
                    paramModels.add(
                            new OutgoingParamModel(
                                    p.getName(),
                                    serialize(p.getValue())
                            )
                    );
                }
            }
            model.setParameters(paramModels);
            result.add(model);
        }

        return result;
    }

    private static String serialize(Object v) {
        try {
            return new ObjectMapper().writeValueAsString(v);
        } catch (Exception e) {
            return "null";
        }
    }

    /**
     * Map the request body schema of an operation to a DelegateBodyModel, which will be used to generate the code for the request body
     */
    @SuppressWarnings("unchecked")
    private DelegateBodyModel mapRequestBody(Map<String, Object> operation) {
        DelegateBodyModel defaultModel = new DelegateBodyModel();
        defaultModel.setJavaType("");
        defaultModel.setFields(Map.of());

        if (operation == null) {
            return defaultModel;
        }

        Object requestBodyObj = operation.get("requestBody");
        if (!(requestBodyObj instanceof Map)) {
            return defaultModel;
        }

        Map<String, Object> requestBody =
                (Map<String, Object>) requestBodyObj;

        Object contentObj = requestBody.get("content");
        if (!(contentObj instanceof Map)) {
            return defaultModel;
        }

        Map<String, Object> content =
                (Map<String, Object>) contentObj;

        Object appJsonObj = content.get("application/json");
        if (!(appJsonObj instanceof Map)) {
            return defaultModel;
        }

        Map<String, Object> appJson =
                (Map<String, Object>) appJsonObj;

        Object schemaObj = appJson.get("schema");
        if (!(schemaObj instanceof Map)) {
            return defaultModel;
        }

        Map<String, Object> schema =
                (Map<String, Object>) schemaObj;

        DelegateBodyModel bodyModel = new DelegateBodyModel();

        // Nome modello coerente con openapi-generator
        String operationId = (String) operation.get("operationId");
        bodyModel.setJavaType(Strings.capitalize(operationId) + "Request");

        Map<String, String> fields = new LinkedHashMap<>();

        Object propsObj = schema.get("properties");
        if (propsObj instanceof Map) {

            Map<String, Object> properties =
                    (Map<String, Object>) propsObj;

            for (Map.Entry<String, Object> e : properties.entrySet()) {

                String fieldName = e.getKey();
                Object fieldSchemaObj = e.getValue();

                if (fieldSchemaObj instanceof Map) {
                    String javaType = mapType(
                            Map.of("schema", fieldSchemaObj)
                    );
                    fields.put(fieldName, javaType);
                }
            }
        }

        bodyModel.setFields(fields);
        return bodyModel;
    }

}

