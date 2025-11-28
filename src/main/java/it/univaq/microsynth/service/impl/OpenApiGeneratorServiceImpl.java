package it.univaq.microsynth.service.impl;

import it.univaq.microsynth.domain.*;
import it.univaq.microsynth.domain.dto.BundleGenerationRequestDTO;
import it.univaq.microsynth.domain.dto.DiagramDTO;
import it.univaq.microsynth.domain.dto.OutgoingCallDTO;
import it.univaq.microsynth.service.GeneratorService;
import it.univaq.microsynth.util.TemplateUtils;
import it.univaq.microsynth.util.ZipUtils;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.codegen.DefaultGenerator;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OpenApiGeneratorServiceImpl implements GeneratorService {

    public File generateBundle(List<BundleGenerationRequestDTO> requests) throws Exception {
        Path tempRootDir = Files.createTempDirectory("multi_generator_");
        List<Map<String, String>> services = new ArrayList<>();
        int portOffset = 0;

        for (BundleGenerationRequestDTO request : requests) {
            Path projectDir = tempRootDir.resolve(sanitizeDockerServiceName(request.getProjectName()));
            Files.createDirectories(projectDir);

            // OpenAPI file creation
            Path openapiFile = projectDir.resolve("openapi.json");
            String json = new com.fasterxml.jackson.databind.ObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(request.getApiSpec());
            Files.writeString(openapiFile, json);
            log.info("OpenAPI spec for " + request.getProjectName() + " written to " + openapiFile.toAbsolutePath());
            log.info("json: " + json);

            // Generated output dir
            Path outputDir = projectDir.resolve("generated");
            Files.createDirectories(outputDir);

            // Port
            int externalPort = 8081 + portOffset++;
            String port = String.valueOf(externalPort);

            // Choose generator
            String generatorName = mapGenerator(request.getType());

            log.info("generatorName: " + generatorName);
            // Generate code with OpenAPI Generator
            org.openapitools.codegen.config.CodegenConfigurator configurator =
                    new org.openapitools.codegen.config.CodegenConfigurator()
                            .setInputSpec(openapiFile.toAbsolutePath().toString())
                            .setGeneratorName(generatorName)
                            .setOutputDir(outputDir.toAbsolutePath().toString())
                            .addAdditionalProperty("interfaceOnly", "false")
                            .addAdditionalProperty("generateSupportingFiles", "true")
                            .addAdditionalProperty("useTags", "true");

            new DefaultGenerator()
                    .opts(configurator.toClientOptInput())
                    .generate();

            // Template Dockerfile in the root of single service
            Map<String, Object> values = Map.of(
                    "projectName", request.getProjectName(),
                    "port", port
            );

            String dockerTemplate = switch (request.getType().toLowerCase()) {
                case "python", "flask" -> "docker/Dockerfile-python.template";
                case "java", "spring" -> "docker/Dockerfile-spring.template";
                case "javascript", "node" -> "docker/Dockerfile-node.template";
                default -> throw new IllegalArgumentException("Unsupported type: " + request.getType());
            };

            // write dockerFile in the project directory
            TemplateUtils.writeRenderedTemplate(
                    dockerTemplate,
                    projectDir.resolve("Dockerfile"),
                    values
            );

            // Add to services list for docker-compose
            String serviceName = sanitizeDockerServiceName(request.getProjectName());

            services.add(Map.of(
                    "name", serviceName,
                    "path", "./" + serviceName + "/generated",
                    "port", port
            ));
        } // each node service

        Path locustDir = tempRootDir.resolve("locust");
        Files.createDirectories(locustDir);

        // Map services > port
        Map<String, Object> portByService = services.stream()
                .collect(Collectors.toMap(s -> s.get("name"), s -> s.get("port")));


        // Generate Dockerfile for locust
        TemplateUtils.writeRenderedTemplate(
                "locust/Dockerfile-locust.template",
                locustDir.resolve("Dockerfile"),
                portByService
        );

        // Generate locustfile.py
        TemplateUtils.writeRenderedTemplateAsJson(
                "locust/locust.py.template",
                locustDir.resolve("locust.py"),
                "servicesJson", requests);

        // Docker compose for all services
        Path composeFile = tempRootDir.resolve("docker-compose.yml");
        TemplateUtils.writeRenderedTemplate(
                "docker/docker-compose-multi.yml.template",
                composeFile,
                Map.of("services", services)
        );

        // Final zip
        File zipFile = Files.createTempFile("bundle_", ".zip").toFile();
        ZipUtils.zipFolder(tempRootDir, zipFile);

        return zipFile;
    }


    public List<BundleGenerationRequestDTO> convertGraphToRequestsinit(DiagramDTO diagram) {
        List<BundleGenerationRequestDTO> requests = new ArrayList<>();

        // Map nodes by id to trace back from connection.source to microservice
        Map<String, Node> nodeMap = diagram.getData().getNodes().stream()
                .collect(Collectors.toMap(Node::getId, n -> n));

        for (Node node : diagram.getData().getNodes()) {
            // Build the OpenAPI skeleton of the microservice
            Map<String, Object> openapi = new LinkedHashMap<>();
            openapi.put("openapi", "3.0.0");
            openapi.put("info", Map.of(
                    "title", sanitize(node.getLabel()),
                    "version", "1.0.0",
                    "description", node.getPayload().getDescription()
            ));

            // Find all incoming connections to this node
            List<Connection> incoming = diagram.getData().getConnections().stream()
                    .filter(c -> c.getTarget().equals(node.getId()))
                    .toList();

            Map<String, Map<String, Object>> paths = new LinkedHashMap<>();
            openapi.put("paths", paths);



            for (Connection conn : incoming) {
                ConnectionPayload p = conn.getPayload();
                if (p == null) continue;

                String path = (p.getApiCall().getPath() == null || p.getApiCall().getPath().isEmpty()) ? "/getObject" : p.getApiCall().getPath();
                String method = (p.getApiCall().getMethod() == null ? "get" : p.getApiCall().getMethod().toLowerCase());

                // Check if path already exists with the same method
                Map<String, Object> existingMethods = paths.get(path);
                if (existingMethods != null && existingMethods.containsKey(method)) {
                    continue;
                }

                paths.computeIfAbsent(path, k -> new LinkedHashMap<>())
                        .put(method, Map.of(
                                "summary", "",
                                "operationId", sanitize(conn.getLabel()),
                                "responses", Map.of("200", Map.of("description", "OK"))
                        ));
            }

            // Outgoing → for Locust
            List<Connection> outgoing = diagram.getData().getConnections().stream()
                    .filter(c -> c.getSource().equals(node.getId()))
                    .toList();

            List<OutgoingCallDTO> outgoingCalls = outgoing.stream().map(conn -> {
                ConnectionPayload p = conn.getPayload();
                OutgoingCallDTO oc = new OutgoingCallDTO();
                oc.setTargetService(sanitize(nodeMap.get(conn.getTarget()).getLabel()));
                oc.setHttpMethod(p != null && p.getApiCall().getMethod() != null ? p.getApiCall().getMethod().toUpperCase() : "GET");
                oc.setPath(p != null && p.getApiCall().getPath() != null ? p.getApiCall().getPath() : "/");
                oc.setWeight(p != null && conn.getWeight() > 0 ? conn.getWeight() : 0.5);
                return oc;
            }).toList();

            // Create final DTO
            BundleGenerationRequestDTO dto = new BundleGenerationRequestDTO();
            dto.setType(node.getPayload().getLanguage());
            dto.setProjectName(sanitize(node.getLabel()));
            dto.setApiSpec(openapi);
            dto.setOutgoingCalls(outgoingCalls);

            requests.add(dto);
        }

        return requests;
    }

    private String mapGenerator(String type) throws IllegalArgumentException{
        log.info("lang: " + type);
        return switch (type.toLowerCase()) {
            case "python" -> "python-flask";
            case "java" -> "spring";
            case "javascript" -> "nodejs-express-server";
            default -> throw new IllegalArgumentException("Unsupported type: " + type);
        };
    }

    private String sanitize(String input) {
        // Rimuove slash iniziali
        input = input.replaceAll("^/+", "");
        // Sostituisce tutti i caratteri non alfanumerici con underscore
        input = input.replaceAll("[^A-Za-z0-9]", "_");
        // Aggiunge "get" se il metodo era get (puoi fare camelCase se vuoi)
        return input;
    }

    private String sanitizeDockerServiceName(String s) {
        // solo minuscole, sostituisce tutto ciò che non è alfanumerico con '-'
        return s.toLowerCase().replaceAll("[^a-z0-9]+", "-");
    }

    @Override
    public List<BundleGenerationRequestDTO> convertGraphToRequests(DiagramDTO diagram) {
        List<BundleGenerationRequestDTO> requests = new ArrayList<>();

        // Mappa dei nodi per id
        Map<String, Node> nodeMap = diagram.getData().getNodes().stream()
                .collect(Collectors.toMap(Node::getId, n -> n));

        for (Node node : diagram.getData().getNodes()) {
            NodePayload payload = node.getPayload();

            // Creazione OpenAPI skeleton
            Map<String, Object> openapi = new LinkedHashMap<>();
            openapi.put("openapi", "3.0.0");
            openapi.put("info", Map.of(
                    "title", sanitize(node.getLabel()),
                    "version", "1.0.0",
                    "description", payload.getDescription()
            ));

            Map<String, Map<String, Object>> paths = new LinkedHashMap<>();

            // Aggiungo gli endpoint definiti nel nodo
            for (Endpoint ep : payload.getEndpoints()) {
                String path = ep.getPath() == null || ep.getPath().isEmpty() ? "/" : ep.getPath();
                String method = ep.getMethod() == null || ep.getMethod().isEmpty() ? "get" : ep.getMethod().toLowerCase();

                // Costruzione parametri OpenAPI validi
                List<Map<String, Object>> parameters = ep.getParameters().stream().map(p -> Map.<String,Object>of(
                        "name", p.getName(),
                        "in", "query", // puoi cambiare in "path" se necessario
                        "required", p.isRequired(),
                        "schema", Map.of("type", p.getType().name().toLowerCase())
                )).toList();

                // Costruzione responses OpenAPI validi
                Map<String, Object> responses = ep.getResponses().stream()
                        .collect(Collectors.toMap(
                                r -> String.valueOf(r.getStatus()),
                                r -> Map.of(
                                        "description", r.getDescription(),
                                        "content", Map.of(
                                                r.getType(), Map.of(
                                                        "schema", Map.of(
                                                                "type", "object",
                                                                "example", r.getContent()
                                                        )
                                                )
                                        )
                                )
                        ));

                paths.computeIfAbsent(path, k -> new LinkedHashMap<>())
                        .put(method, Map.of(
                                "summary", ep.getSummary(),
                                "operationId", sanitize(path + "-" + method),
                                "parameters", parameters,
                                "responses", responses
                        ));
            }

            openapi.put("paths", paths);

            // Creo il DTO finale
            BundleGenerationRequestDTO dto = new BundleGenerationRequestDTO();
            dto.setType(payload.getLanguage());
            dto.setProjectName(sanitize(node.getLabel()));
            dto.setApiSpec(openapi);

            // Outgoing calls
            dto.setOutgoingCalls(buildOutgoingCalls(node, nodeMap, diagram.getData().getConnections()));
            requests.add(dto);
        }

        return requests;
    }

    private List<OutgoingCallDTO> buildOutgoingCalls(Node node, Map<String, Node> nodeMap, List<Connection> connections) {
        List<OutgoingCallDTO> outgoing = new ArrayList<>();

        for (Connection c : connections) {
            if (c.getSource().equals(node.getId())) {

                Node targetNode = nodeMap.get(c.getTarget());
                if (targetNode == null) continue;

                ConnectionPayload cp = c.getPayload();
                if (cp == null || cp.getApiCall() == null) continue;

                ApiCall api = cp.getApiCall();

                OutgoingCallDTO call = new OutgoingCallDTO();
                call.setTargetService(sanitizeDockerServiceName(targetNode.getLabel()));
                call.setHttpMethod(api.getMethod());
                call.setPath(api.getPath());
                call.setWeight(c.getWeight());

                Map<String, Object> params = new HashMap<>();
                for (ParameterValue<?> pv : api.getParameterValues()) {
                    params.put(pv.getName(), pv.getValue());
                }
                call.setParameters(params);

                outgoing.add(call);
            }
        }

        return outgoing;
    }

}



