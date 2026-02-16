package it.univaq.microsynth.service.impl;

import it.univaq.microsynth.Enum.ParameterType;
import it.univaq.microsynth.domain.*;
import it.univaq.microsynth.domain.dto.*;
import it.univaq.microsynth.domain.mapper.DiagramMapper;
import it.univaq.microsynth.domain.mapper.ProjectMapper;
import it.univaq.microsynth.repository.ProjectRepository;
import it.univaq.microsynth.service.ProjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Page<Project>> getUserProjects(String userName, PaginatedRequestDTO paginatedRequestDTO) {
        Sort sort = paginatedRequestDTO.getSortDir().equalsIgnoreCase("asc") ? Sort.by(paginatedRequestDTO.getSortBy()).ascending() : Sort.by(paginatedRequestDTO.getSortBy()).descending();
        PageRequest pageRequest = PageRequest.of(paginatedRequestDTO.getPage(), paginatedRequestDTO.getSize(), sort);
        return ResponseEntity.ok(
                projectRepository.findAllByOwner(userName, pageRequest)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<ProjectDTO> getProjectById(String id, String userName) {
        return projectRepository.findByIdAndOwner(id, userName)
                .map(ProjectMapper.INSTANCE::projectToProjectDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<DiagramDTO> getDiagramById(String projectId, String diagramId) {
        return projectRepository.findById(projectId)
                .flatMap(project -> project.getDiagrams().stream()
                        .filter(diagram -> diagram.getId().equals(diagramId))
                        .findFirst()
                        .map(DiagramMapper.INSTANCE::diagramToDiagramDTO)
                )
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<DocumentResponseDTO> updateProject(String id, ProjectDTO projectDTO) {
        return projectRepository.findById(id)
                .map(existingProject -> {
                    existingProject.setName(projectDTO.getName());
                    projectRepository.save(existingProject);
                    return ResponseEntity.ok(new DocumentResponseDTO(existingProject.getId()));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<DocumentResponseDTO> updateDiagram(String id, DiagramDTO diagramDTO) {
        log.info("Updating project {} with diagram: {}", id, diagramDTO.toString());
        return projectRepository.findById(id)
                .map(project -> {
                    Optional<Diagram> existingDiagramOpt = project.getDiagrams().stream()
                            .filter(diagram -> diagram.getId().equals(diagramDTO.getId()))
                            .findFirst();
                    Diagram diagram;
                    if (existingDiagramOpt.isPresent()) {
                        diagram = existingDiagramOpt.get();
                        diagram.setName(diagramDTO.getName());
                        diagram.setData(diagramDTO.getData());
                        // Update other fields if needed
                    } else {
                        // Create a new diagram and add to the project
                        diagram = new Diagram();
                        diagram.setId(UUID.randomUUID().toString());
                        diagram.setName(diagramDTO.getName());
                        diagram.setData(diagramDTO.getData());
                        // Set other fields from diagramDTO as needed
                        project.getDiagrams().add(diagram);
                    }

                    // Save the updated project if necessary
                    projectRepository.save(project);

                    log.info("Updated project {} with diagram: {}", id, diagramDTO);
                    return ResponseEntity.ok(new DocumentResponseDTO(diagram.getId()));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<DocumentResponseDTO> createProject(ProjectDTO projectDTO, String userName) {
        Project project = ProjectMapper.INSTANCE.projectDTOtoProject(projectDTO);
        project = projectRepository.save(project);
        return ResponseEntity.ok(new DocumentResponseDTO(project.getId()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<?> deleteProject(String id) {
        return projectRepository.findById(id)
                .map(project -> {
                    if (project.getDiagrams() != null && !project.getDiagrams().isEmpty()) {
                        return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body("Project contains diagrams. Cannot be deleted.");
                    }
                    projectRepository.delete(project);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<?> deleteDiagram(String projectId, String diagramId) {
        log.info("Deleting diagram {} from project {}", diagramId, projectId);
        return projectRepository.findById(projectId)
                .map(project -> {
                    Optional<Diagram> diagramToDelete = project.getDiagrams().stream()
                            .filter(diagram -> diagram.getId().equals(diagramId))
                            .findFirst();

                    if (diagramToDelete.isPresent()) {
                        project.getDiagrams().remove(diagramToDelete.get());
                        projectRepository.save(project);
                        log.info("Deleted diagram {} from project {}", diagramId, projectId);
                        return ResponseEntity.ok("Diagram deleted successfully");
                    } else {
                        log.warn("Diagram {} not found in project {}", diagramId, projectId);
                        return ResponseEntity.notFound().build();
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Diagram generate(DiagramGenerationRequestDTO params) {

        int n = params.getNodes();
        int r = params.getRoots();
        double d = params.getDensity();

        if (r > n) {
            throw new IllegalArgumentException("Number of roots cannot be greater than number of nodes.");
        }

        Random rand = new Random();

        List<Node> nodes = new ArrayList<>();
        List<Connection> connections = new ArrayList<>();
        List<String> nodeIds = new ArrayList<>();

        // Nodes creation
        for (int i = 0; i < n; i++) {
            String id = UUID.randomUUID().toString();
            nodeIds.add(id);

            nodes.add(new Node(
                    id,
                    "S " + (i + 1),
                    "circle",
                    new Position(0.0, 0.0),
                    generateRandomNodePayload(
                            "s-" + (i + 1),
                            "/path-" + (i + 1),
                            params.getLanguages()
                    ),
                    0.0
            ));
        }

        // Root choice
        Set<Integer> rootIndices = new HashSet<>();
        while (rootIndices.size() < r) {
            rootIndices.add(rand.nextInt(n));
        }

        // Generate possible acycle edges
        List<int[]> possibleEdges = new ArrayList<>();
        for (int from = 0; from < n - 1; from++) {
            for (int to = from + 1; to < n; to++) {
                if (!rootIndices.contains(to)) {
                    possibleEdges.add(new int[]{from, to});
                }
            }
        }

        int maxConnections = possibleEdges.size();
        int targetConnections = (int) Math.round(d * maxConnections);

        Collections.shuffle(possibleEdges, rand);

        List<int[]> selectedEdges = possibleEdges.subList(
                0,
                Math.min(targetConnections, maxConnections)
        );

        // Group edges per source node
        Map<String, List<int[]>> edgesBySource = new HashMap<>();

        for (int[] edge : selectedEdges) {
            String sourceId = nodeIds.get(edge[0]);
            edgesBySource
                    .computeIfAbsent(sourceId, k -> new ArrayList<>())
                    .add(edge);
        }

        // Generate connections with weight
        for (Map.Entry<String, List<int[]>> entry : edgesBySource.entrySet()) {

            String sourceId = entry.getKey();
            List<int[]> sourceEdges = entry.getValue();

            double remaining = 1.0;

            for (int i = 0; i < sourceEdges.size(); i++) {
                int[] edge = sourceEdges.get(i);
                String targetId = nodeIds.get(edge[1]);
                double weight;

                // if weight sum of probability of outgoing connections must be =1
                if (Boolean.TRUE.equals(params.getOutgoingProbabiltySum())) {
                    int edgesLeft = sourceEdges.size() - i;
                    if (edgesLeft == 1) {
                        // last connection takes the remaining weigth available
                        weight = remaining;
                    } else {
                        // Minimum 0.1 for remaining connection
                        double max = remaining - (0.1 * (edgesLeft - 1));
                        weight = 0.1 + rand.nextDouble() * (max - 0.1);
                    }
                    weight = Math.round(weight * 100.0) / 100.0;
                    remaining -= weight;
                    // Final correction to avoiding floating point
                    if (edgesLeft == 1) {
                        weight += remaining;
                        weight = Math.round(weight * 100.0) / 100.0;
                    }
                } else {
                    weight = 0.1 + rand.nextDouble() * 0.9;
                    weight = Math.round(weight * 100.0) / 100.0;
                }

                Node targetNode = nodes.stream()
                        .filter(n1 -> n1.getId().equals(targetId))
                        .findFirst()
                        .orElse(null);

                if (targetNode == null) continue;

                ConnectionPayload payload = generateRandomConnectionPayload(targetNode);
                targetNode.getPayload().setInitiator(false);

                String action = payload.getApiCall().getMethod() + "_" +
                        payload.getApiCall().getPath().replaceAll("/", "");

                connections.add(new Connection(
                        UUID.randomUUID().toString(),
                        sourceId,
                        targetId,
                        false,
                        weight,
                        action,
                        payload
                ));
            }
        }

        // Build diagram
        Diagram diagram = new Diagram();
        diagram.setId(UUID.randomUUID().toString());
        diagram.setName("Generated System");
        diagram.setData(new DiagramData(
                nodes,
                connections,
                new Viewport(0.0, 0.0, 1.0)
        ));
        return diagram;
    }
    /**
     * Helper method to generate random NodePayload based on the node name and path.
     * This method creates a payload with random language, endpoints, and parameters.
     * @param nodeName The name of the node for which to generate the payload.
     * @param path The path to be used in the generated endpoint.
     * @param languages language request to generate
     * @return A NodePayload object with randomly generated content based on the provided node name and path.
     */
    private NodePayload generateRandomNodePayload(String nodeName, String path, Set<String> languages) throws IllegalArgumentException {
        NodePayload payload = new NodePayload();
        Random rand = new Random();

        if (languages == null || languages.isEmpty()) {
            throw new IllegalArgumentException("Languages to generate cannot be null or empty");
        }

        List<String> languageList = new ArrayList<>(languages);
        String chosenLanguage = languageList.get(rand.nextInt(languageList.size()));

        payload.setLanguage(chosenLanguage);
        payload.setBasePath("/api");
        payload.setType("controller");
        payload.setDescription("Service " + nodeName);

        // Generate random parameters
        List<Parameter> parameters = List.of(
            Parameter.builder()
                    .name("id")
                    .type(ParameterType.STRING)
                    .required(true)
                    .build(),
            Parameter.builder()
                    .name("active")
                    .type(ParameterType.BOOLEAN)
                    .required(false)
                    .build()
        );

        String[] methods = { "GET", "POST" };

        // random integer between 1-30
        Integer complexity = new Random().nextInt(30) + 1;

        // Create an endpoint
        Endpoint e1 = Endpoint.builder()
                .path(path)
                .method(methods[new Random().nextInt(methods.length)])
                .parameters(parameters)
                .responses(List.of(
                        new ApiResponse(200, "Successful response", "application/json","{}")
                ))
                .complexity(complexity)
                .build();

        payload.setEndpoints(List.of(e1));
        payload.setInitiator(true);

        return payload;
    }

    /**
     * Helper method to generate random ConnectionPayload based on the target node's endpoints.
     * This method creates a payload with a random API call using one of the target node's endpoints.
     * @param targetNode The target node for which to generate the connection payload. The method will use the endpoints of this node to create the payload.
     * @return A ConnectionPayload object with a randomly generated API call based on the target node's endpoints. If the target node has no endpoints, it will generate a default API call.
     * @throws IllegalStateException if the target node is null or has no endpoints, as a connection payload cannot be generated without this information.
     */
    private ConnectionPayload generateRandomConnectionPayload(Node targetNode) {
        Random rand = new Random();

        List<Endpoint> endpoints = targetNode.getPayload().getEndpoints();
        String path = "/path-x";
        String method = "GET";
        List<Parameter> parameters = new ArrayList<>();

        Endpoint endpoint = null;
        // Extract casual endpoint, if available
        if (endpoints != null && !endpoints.isEmpty()) {
            endpoint = endpoints.get(rand.nextInt(endpoints.size()));

            if (endpoint.getPath() != null && !endpoint.getPath().isEmpty()) {
                path = endpoint.getPath();
            }
            if (endpoint.getMethod() != null && !endpoint.getMethod().isEmpty()) {
                method = endpoint.getMethod();
            }
            if (endpoint.getParameters() != null && !endpoint.getParameters().isEmpty()) {
                parameters = endpoint.getParameters();
            }
        }

        // Generate parameter values based on type
        List<ParameterValue<?>> parameterValues = new ArrayList<>();
        for (Parameter p : parameters) {
            Object value = generateRandomValueForType(p.getType());
            parameterValues.add(new ParameterValue<>(p.getName(), value));
        }

        // Create API call
        ApiCall apiCall = ApiCall.builder()
                .method(method)
                .path(path)
                .parameterValues(parameterValues)
                .build();

        ConnectionPayload payload = new ConnectionPayload();
        payload.setApiCall(apiCall);
        payload.setEndpoint(endpoint);

        return payload;
    }

    /**
     * Helper method to generate a random value based on the provided ParameterType.
     * This method returns a random value corresponding to the type of the parameter.
     *
     * @param type The ParameterType for which to generate a random value.
     * @return A random value corresponding to the provided ParameterType.
     */
    private Object generateRandomValueForType(ParameterType type) {
        Random rand = new Random();

        return switch (type) {
            case STRING -> "value_" + rand.nextInt(1000);
            case INTEGER -> rand.nextInt(100);
            case FLOAT -> rand.nextDouble() * 100;
            case BOOLEAN -> rand.nextBoolean();
            case ARRAY -> List.of("a", "b", "c"); // oppure genera dinamicamente
            case JSON -> Map.of(
                    "id", rand.nextInt(1000),
                    "active", rand.nextBoolean(),
                    "timestamp", System.currentTimeMillis()
            );
            case OBJECT -> Map.of("key", "value_" + rand.nextInt(1000));
        };
    }

}
