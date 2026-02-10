package it.univaq.microsynth.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.univaq.microsynth.domain.*;
import it.univaq.microsynth.domain.dto.BundleGenerationRequestDTO;
import it.univaq.microsynth.domain.dto.DiagramDTO;
import it.univaq.microsynth.domain.dto.OutgoingCallDTO;
import it.univaq.microsynth.domain.dto.OutgoingParamDTO;
import it.univaq.microsynth.generator.builder.DelegateImplModelBuilder;
import it.univaq.microsynth.generator.model.DelegateImplModel;
import it.univaq.microsynth.generator.util.GeneratorUtil;
import it.univaq.microsynth.service.GeneratorService;
import it.univaq.microsynth.util.TemplateUtils;
import it.univaq.microsynth.util.ZipUtils;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.codegen.ClientOptInput;
import org.openapitools.codegen.DefaultGenerator;
import org.openapitools.codegen.config.CodegenConfigurator;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;



@Slf4j
@Service
public class OpenApiGeneratorServiceImpl implements GeneratorService {
    // Starting port for generated services, each service will get a different port incrementing this value
    private final static int START_PORT = 8091;

    // Builder for creating the model used to generate the delegate implementation in Spring services
    private final DelegateImplModelBuilder delegateImplModelBuilder;

    public OpenApiGeneratorServiceImpl(DelegateImplModelBuilder delegateImplModelBuilder) {
        this.delegateImplModelBuilder = delegateImplModelBuilder;
    }

    /**
     * {@inheritDoc}
     */
    public File generateBundle(List<BundleGenerationRequestDTO> requests) throws Exception {
        log.info("[GENERATOR] Start bundle generation for n. {} of services ", requests.size());

        Path tempRootDir = Files.createTempDirectory("multi_generator_");
        List<Map<String, String>> services = new ArrayList<>();
        int portOffset = 0;

        for (BundleGenerationRequestDTO request : requests) {
            log.info("[GENERATOR] Generating service {} with type {}", request.getProjectName(), request.getType());
            // Create project directory
            Path projectDir = tempRootDir.resolve(GeneratorUtil.sanitizeDockerServiceName(request.getProjectName()));
            Files.createDirectories(projectDir);

            // OpenAPI file creation
            log.info("[GENERATOR] Start creating OpenAPI spec file");
            Path openapiFile = projectDir.resolve("openapi.json");
            String json = new com.fasterxml.jackson.databind.ObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(request.getApiSpec());
            Files.writeString(openapiFile, json);
            log.info("[GENERATOR] End creating OpenAPI spec file");

            // generate single service from openapi
            log.info("[GENERATOR] Start generating code for single service {} with type {}", request.getProjectName(), request.getType());
            generateSingleService(request, openapiFile, projectDir);
            log.info("[GENERATOR] End generating code for single service {}", request.getProjectName());

            // Port
            int externalPort = START_PORT + portOffset++;
            String port = String.valueOf(externalPort);

            // write dockerFile in the project directory
            log.info("[GENERATOR] Start creating Dockerfile");
            TemplateUtils.writeRenderedTemplate(
                    GeneratorUtil.getDockerTemplate(request.getType()),
                    projectDir.resolve("Dockerfile"),
                    Map.of(
                            "projectName", request.getProjectName(),
                            "port", port
                    )
            );
            log.info("[GENERATOR] End creating Dockerfile");

            // Add to services list for global docker-compose
            String serviceName = GeneratorUtil.sanitizeDockerServiceName(request.getProjectName());

            services.add(Map.of(
                    "name", serviceName,
                    "path", "./" + serviceName + "/generated",
                    "port", port
            ));
        } // each node service

        log.info("[GENERATOR] Start creating Locust Dockerfile");
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
        log.info("[GENERATOR] End creating Locust Dockerfile");

        // Generate locustfile.py
        // only with initiator services, and pass the outgoing calls to log in locust
        log.info("[GENERATOR] Start creating Locust locust.py config file");
        List<BundleGenerationRequestDTO> initiatorsRequests = requests.stream()
                .filter(BundleGenerationRequestDTO::getInitiator)
                .toList();
        TemplateUtils.writeRenderedTemplate(
                "locust/locust.py.template",
                locustDir.resolve("locust.py"),
                Map.of("services", initiatorsRequests)
        );
        log.info("[GENERATOR] End creating Locust locust.py config file");

        // Docker compose for all services
        log.info("[GENERATOR] Start creating docker-compose.yml for all services");
        Path composeFile = tempRootDir.resolve("docker-compose.yml");
        TemplateUtils.writeRenderedTemplate(
                "docker/docker-compose-multi.yml.template",
                composeFile,
                Map.of("services", services)
        );
        log.info("[GENERATOR] End creating docker-compose.yml for all services");

        // Final zip
        log.info("[GENERATOR] Creating final zip file for the bundle");
        File zipFile = Files.createTempFile("bundle_", ".zip").toFile();
        ZipUtils.zipFolder(tempRootDir, zipFile);

        log.info("[GENERATOR] End bundle generation");
        return zipFile;
    }

    /**
     * Generates a single microservice from an OpenAPI specification using OpenAPI Generator.
     * the generator is chosen based on the request type (e.g. "spring" for Java, "python-flask" for Python, etc.)
     * @param request - the bundle generation request containing the OpenAPI spec and other configuration
     * @param openapiFile - the path to the OpenAPI specification file to use for code generation
     * @param projectDir - the directory where the generated code and supporting files should be placed
     * @throws IOException if any error occurs during file I/O operations (e.g. writing generated code, creating directories, etc.)
     */
    private void generateSingleService(BundleGenerationRequestDTO request, Path openapiFile, Path projectDir) throws IOException {
        // Choose generator
        String generatorName = GeneratorUtil.mapGenerator(request.getType());

        // Generated output dir
        Path outputDir = projectDir.resolve("generated");
        Files.createDirectories(outputDir);

        Path templateBaseDir = TemplateUtils.extractTemplates(generatorName);
        Path templateDir = templateBaseDir.resolve(generatorName);

        Files.walk(templateDir).forEach(p ->
                log.info("[TEMPLATE] {}", p)
        );

        // Generate code with OpenAPI Generator
        CodegenConfigurator configurator =
                new CodegenConfigurator()
                        .setInputSpec(openapiFile.toAbsolutePath().toString()) // specifica OpenAPI
                        .setGeneratorName(generatorName) // es. "spring", "python-flask", "nodejs-express"
                        .setTemplateDir(templateDir.toString()) // template directory
                        .setOutputDir
                                (outputDir.toAbsolutePath().toString())
                        .addAdditionalProperty("interfaceOnly", false) // generate only interfaces no implementation
                        .addAdditionalProperty("generateSupportingFiles", true) // generate supporting files (ex. pom.xml, requirements.txt, etc.)
                        .addAdditionalProperty("useTags", "true"); // organize operations in separate class (ex. path1Post → Path1Api, path2Get → Path2Api, etc.)

        log.info("[GENERATOR] Serializing service outgoing calls to JSON");
        ObjectMapper mapper = new ObjectMapper();
        String outgoingCallsJson = mapper.writeValueAsString(request.getOutgoingCalls());

        // Spring specific configuration (delegate pattern)
        if(generatorName.equals("spring")) {
            log.info("[GENERATOR] Spring generator detected, start building delegate implementation model for service {}", request.getProjectName());
            configurator.addAdditionalProperty("outgoingCallsJson", GeneratorUtil.stringEscape(outgoingCallsJson));
            configurator.addAdditionalProperty("useSpringController", true)
                    .addAdditionalProperty("delegatePattern", true)
                    .addAdditionalProperty("useResponseEntity", true);
            DelegateImplModel delegateModel = delegateImplModelBuilder.build(request);

            configurator
                    .addAdditionalProperty("modelImports", delegateModel.getModelImports())
                    .addAdditionalProperty("delegateOperations", delegateModel.getOperations())
                    .addAdditionalProperty("packageName", delegateModel.getPackageName())
                    .addAdditionalProperty("className", delegateModel.getClassName());
            log.info("[GENERATOR] End building delegate implementation");
        } else {
            configurator.addAdditionalProperty("outgoingCallsJson", outgoingCallsJson);
            configurator.addAdditionalProperty("delegatePattern", false); // other generator (es. python-flask) generate in a whole file, without pattern delegate
        }

        ClientOptInput input = configurator.toClientOptInput();
        log.info("[GENERATOR] Start code generation with openapi tools for service {}", request.getProjectName());
        // generate
        new DefaultGenerator().opts(input).generate();
        log.info("[GENERATOR] End code generation with openapi tools for service {}", request.getProjectName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BundleGenerationRequestDTO> convertGraphToRequests(DiagramDTO diagram) {
        log.info("[GENERATOR] Start Converting diagram to bundle generation requests for diagram {}", diagram.getName());
        List<BundleGenerationRequestDTO> requests = new ArrayList<>();

        // Create a map of nodeId -> Node for easy lookup when building outgoing calls
        Map<String, Node> nodeMap = diagram.getData().getNodes().stream()
                .collect(Collectors.toMap(Node::getId, n -> n));

        log.info("[GENERATOR] Start build OpenAPI spec for n. of nodes {}", diagram.getData().getNodes().size());
        for (Node node : diagram.getData().getNodes()) {
            NodePayload payload = node.getPayload();

            log.info("[GENERATOR] build OpenAPI spec for node {} with label {}", node.getId(), node.getLabel());
            // Creazione OpenAPI skeleton
            Map<String, Object> openapi = new LinkedHashMap<>();
            openapi.put("openapi", "3.0.0");
            openapi.put("info", Map.of(
                    "title", GeneratorUtil.sanitize(node.getLabel()),
                    "version", "1.0.0",
                    "description", payload.getDescription()
            ));

            Map<String, Map<String, Object>> paths = new LinkedHashMap<>();

            // Add node endpoints to OpenAPI paths
            for (Endpoint ep : payload.getEndpoints()) {
                String path = ep.getPath() == null || ep.getPath().isEmpty() ? "/" : ep.getPath();
                String method = ep.getMethod() == null || ep.getMethod().isEmpty() ? "get" : ep.getMethod().toLowerCase();

                // Building parameters OpenAPI valid
                List<Map<String, Object>> parameters = ep.getParameters().stream().map(p -> Map.of(
                        "name", p.getName(),
                        "in", "query",
                            "required", p.isRequired(),
                        "schema", Map.of("type", p.getType().name().toLowerCase())
                )).toList();

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

                Map<String, Object> operation = new LinkedHashMap<>();
                operation.put("summary", ep.getSummary());
                operation.put("operationId", GeneratorUtil.toOperationId(path, method));
                operation.put("responses", responses);

                if (method.equals("post") || method.equals("put")
                        || method.equals("patch") || method.equals("delete")) {

                    // ===== JSON BODY =====
                    Map<String, Object> properties = new LinkedHashMap<>();
                    List<String> required = new ArrayList<>();



                    for (Parameter p : ep.getParameters()) {
                        properties.put(
                                p.getName(),
                                Map.of("type", p.getType().name().toLowerCase())
                        );
                        if (p.isRequired()) {
                            required.add(p.getName());
                        }
                    }

                    Map<String, Object> schema = new LinkedHashMap<>();
                    schema.put("title", GeneratorUtil.toOperationId(path, method) + "Request");
                    schema.put("type", "object");
                    schema.put("properties", properties);
                    if (!required.isEmpty()) {
                        schema.put("required", required);
                    }

                    operation.put(
                            "requestBody",
                            Map.of(
                                    "required", true,
                                    "content", Map.of(
                                            "application/json", Map.of(
                                                    "schema", schema
                                            )
                                    )
                            )
                    );

                } else {
                    // ===== QUERY PARAMS (GET) =====
                    operation.put("parameters", parameters);
                }

                paths.computeIfAbsent(path, k -> new LinkedHashMap<>())
                        .put(method, operation);
            }

            openapi.put("paths", paths);
            // Create final DTO for this node
            BundleGenerationRequestDTO dto = new BundleGenerationRequestDTO();
            dto.setType(payload.getLanguage());
            dto.setProjectName(GeneratorUtil.sanitize(node.getLabel()));
            dto.setApiSpec(openapi);

            // Outgoing calls
            dto.setOutgoingCalls(buildOutgoingCalls(node, nodeMap, diagram.getData().getConnections()));
            dto.setInitiator(node.getPayload().getInitiator());
            requests.add(dto);
        }
        log.info("[GENERATOR] End build OpenAPI spec");
        log.info("[GENERATOR] End converting diagram to bundle generation requests for diagram {}", diagram.getName());
        return requests;
    }

    private List<OutgoingCallDTO> buildOutgoingCalls(Node node, Map<String, Node> nodeMap, List<Connection> connections) {
        List<OutgoingCallDTO> outgoing = new ArrayList<>();

        log.info("[GENERATOR] Start building outgoing calls for node {} with label {}", node.getId(), node.getLabel());
        // each connection from this node
        for (Connection c : connections) {
            if (c.getSource().equals(node.getId())) {

                Node targetNode = nodeMap.get(c.getTarget());
                if (targetNode == null) continue;

                ConnectionPayload cp = c.getPayload();
                if (cp == null || cp.getApiCall() == null) continue;

                ApiCall api = cp.getApiCall();

                OutgoingCallDTO call = new OutgoingCallDTO();
                call.setOperationId(
                        GeneratorUtil.sanitize(api.getPath() + "-" + api.getMethod().toLowerCase())
                );
                call.setTargetService(GeneratorUtil.sanitizeDockerServiceName(targetNode.getLabel()));
                call.setHttpMethod(api.getMethod());
                call.setPath(api.getPath());
                call.setWeight(c.getWeight());
                call.setBaseUrl(
                        "http://" + call.getTargetService() + ":" + call.getPort()
                );

                List<OutgoingParamDTO> params = new ArrayList<>();
                for (ParameterValue<?> pv : api.getParameterValues()) {
                    params.add(new OutgoingParamDTO(pv.getName(), pv.getValue()));
                }
                call.setParameters(params);

                outgoing.add(call);
            }
        }
        log.info("[GENERATOR] End building outgoing calls for node {}", node.getId());
        return outgoing;
    }

}


