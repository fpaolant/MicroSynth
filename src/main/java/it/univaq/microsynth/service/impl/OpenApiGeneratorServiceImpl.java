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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;



@Slf4j

@Service
public class OpenApiGeneratorServiceImpl implements GeneratorService {


    private final DelegateImplModelBuilder delegateImplModelBuilder;

    public OpenApiGeneratorServiceImpl(DelegateImplModelBuilder delegateImplModelBuilder) {
        this.delegateImplModelBuilder = delegateImplModelBuilder;
    }

    /**
     * Generates a bundle (zip file) containing multiple microservices generated from OpenAPI specifications.
     * Each microservice is generated in a separate directory with its own OpenAPI spec, Dockerfile, and supporting files.
     * The method also generates a docker-compose.yml file to orchestrate all the services together, and a locust configuration for load testing.
     *
     * @param requests
     * @return File - the generated zip file containing all microservices and configuration
     * @throws Exception
     */
    public File generateBundle(List<BundleGenerationRequestDTO> requests) throws Exception {
        Path tempRootDir = Files.createTempDirectory("multi_generator_");
        List<Map<String, String>> services = new ArrayList<>();
        int portOffset = 0;

        for (BundleGenerationRequestDTO request : requests) {
            // Create project directory
            Path projectDir = tempRootDir.resolve(GeneratorUtil.sanitizeDockerServiceName(request.getProjectName()));
            Files.createDirectories(projectDir);

            // OpenAPI file creation
            Path openapiFile = projectDir.resolve("openapi.json");
            String json = new com.fasterxml.jackson.databind.ObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(request.getApiSpec());
            Files.writeString(openapiFile, json);

            // generate single service from openapi
            generateSingleService(request, openapiFile, projectDir);

            // Port
            int externalPort = 8081 + portOffset++;
            String port = String.valueOf(externalPort);

            // write dockerFile in the project directory
            TemplateUtils.writeRenderedTemplate(
                    GeneratorUtil.getDockerTemplate(request.getType()),
                    projectDir.resolve("Dockerfile"),
                    Map.of(
                            "projectName", request.getProjectName(),
                            "port", port
                    )
            );

            // Add to services list for docker-compose
            String serviceName = GeneratorUtil.sanitizeDockerServiceName(request.getProjectName());

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
        // only with initiator services, and pass the outgoing calls to log in locust
        List<BundleGenerationRequestDTO> initiatorsRequests = requests.stream()
                .filter(BundleGenerationRequestDTO::getInitiator)
                .collect(Collectors.toList());
        TemplateUtils.writeRenderedTemplate(
                "locust/locust.py.template",
                locustDir.resolve("locust.py"),
                Map.of("services", initiatorsRequests)
        );

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

    /**
     * Generates a single microservice from an OpenAPI specification using OpenAPI Generator.
     * the generator is chosen based on the request type (e.g. "spring" for Java, "python-flask" for Python, etc.)
     * @param request
     * @param openapiFile
     * @param projectDir
     * @throws IOException
     */
    private void generateSingleService(BundleGenerationRequestDTO request, Path openapiFile, Path projectDir) throws IOException {
        // Choose generator
        String generatorName = GeneratorUtil.mapGenerator(request.getType());

        // Generated output dir
        Path outputDir = projectDir.resolve("generated");
        Files.createDirectories(outputDir);

        // Generate code with OpenAPI Generator
        CodegenConfigurator configurator =
                new CodegenConfigurator()
                        .setInputSpec(openapiFile.toAbsolutePath().toString()) // specifica OpenAPI
                        .setGeneratorName(generatorName) // es. "spring", "python-flask", "nodejs-express"
                        .setTemplateDir("src/main/resources/templates/openapi/custom/" + generatorName) // directory dei template personalizzati
                        .setOutputDir
                                (outputDir.toAbsolutePath().toString())
                        .addAdditionalProperty("interfaceOnly", false) // genera solo le interfacce, non le implementazioni
                        .addAdditionalProperty("generateSupportingFiles", true) // genera i supporting files (es. pom.xml, requirements.txt, ecc.)
                        .addAdditionalProperty("useTags", "true"); // organizza le operazioni in classi separate per tag (es. path1Post → Path1Api, path2Get → Path2Api, ecc.)


        //configurator.addAdditionalProperty("outgoingCalls", request.getOutgoingCalls()); // per i template, contiene la lista delle chiamate in uscita da loggare
        ObjectMapper mapper = new ObjectMapper();

        String outgoingCallsJson = mapper.writeValueAsString(request.getOutgoingCalls());

        // Spring specific configuration (delegate pattern)
        if(generatorName.equals("spring")) {
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
        } else {
            configurator.addAdditionalProperty("outgoingCallsJson", outgoingCallsJson);
            configurator.addAdditionalProperty("delegatePattern", false); // per gli altri generatori (es. python-flask) generiamo tutto in un unico file, senza pattern delegate
        }

        ClientOptInput input = configurator.toClientOptInput();
        // generate
        new DefaultGenerator().opts(input).generate();
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
                    "title", GeneratorUtil.sanitize(node.getLabel()),
                    "version", "1.0.0",
                    "description", payload.getDescription()
            ));

            Map<String, Map<String, Object>> paths = new LinkedHashMap<>();

            // Aggiungo gli endpoint definiti nel nodo
            for (Endpoint ep : payload.getEndpoints()) {
                String path = ep.getPath() == null || ep.getPath().isEmpty() ? "/" : ep.getPath();
                String method = ep.getMethod() == null || ep.getMethod().isEmpty() ? "get" : ep.getMethod().toLowerCase();

                // Costruzione parametri OpenAPI validi
                List<Map<String, Object>> parameters = ep.getParameters().stream().map(p -> Map.of(
                        "name", p.getName(),
                        "in", "query",
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

            // Creo il DTO finale
            BundleGenerationRequestDTO dto = new BundleGenerationRequestDTO();
            dto.setType(payload.getLanguage());
            dto.setProjectName(GeneratorUtil.sanitize(node.getLabel()));
            dto.setApiSpec(openapi);

            // Outgoing calls
            dto.setOutgoingCalls(buildOutgoingCalls(node, nodeMap, diagram.getData().getConnections()));
            dto.setInitiator(node.getPayload().getInitiator());
            requests.add(dto);
        }

        return requests;
    }

    private List<OutgoingCallDTO> buildOutgoingCalls(Node node, Map<String, Node> nodeMap, List<Connection> connections) {
        List<OutgoingCallDTO> outgoing = new ArrayList<>();

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

        return outgoing;
    }

}


