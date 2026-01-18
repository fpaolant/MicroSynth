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

    @Override
    public ResponseEntity<Page<Project>> getUserProjects(String userName, PaginatedRequestDTO paginatedRequestDTO) {
        Sort sort = paginatedRequestDTO.getSortDir().equalsIgnoreCase("asc") ? Sort.by(paginatedRequestDTO.getSortBy()).ascending() : Sort.by(paginatedRequestDTO.getSortBy()).descending();
        PageRequest pageRequest = PageRequest.of(paginatedRequestDTO.getPage(), paginatedRequestDTO.getSize(), sort);
        return ResponseEntity.ok(
                projectRepository.findAllByOwner(userName, pageRequest)
        );
    }

    @Override
    public ResponseEntity<ProjectDTO> getProjectById(String id, String userName) {
        return projectRepository.findByIdAndOwner(id, userName)
                .map(ProjectMapper.INSTANCE::projectToProjectDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

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

    @Override
    public ResponseEntity<DocumentResponseDTO> createProject(ProjectDTO projectDTO, String userName) {
        Project project = ProjectMapper.INSTANCE.projectDTOtoProject(projectDTO);
        project = projectRepository.save(project);
        return ResponseEntity.ok(new DocumentResponseDTO(project.getId()));
    }

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

    @Override
    public Diagram generate(DiagramGenerationRequestDTO params) throws IllegalArgumentException {
        int n = params.getNodes();
        int r = params.getRoots();
        double d = params.getDensity();

        if (r > n) {
            throw new IllegalArgumentException("Number of roots cannot be greater than number of nodes.");
        }

        List<Node> nodes = new ArrayList<>();
        List<Connection> connections = new ArrayList<>();
        Random rand = new Random();

        // 1. Create nodes with UUIDs as IDs
        List<String> nodeIds = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String id = UUID.randomUUID().toString();
            nodeIds.add(id);
            nodes.add(new Node(
                    id,
                    "S " + (i + 1),
                    "circle",
                    this.generateRandomNodePayload("s-" + (i + 1), "/path-" + (i + 1)),
                    0.0
            ));
        }

        // 2. Select roots (by index)
        Set<Integer> rootIndices = new HashSet<>();
        while (rootIndices.size() < r) {
            rootIndices.add(rand.nextInt(n));
        }

        // 3. Generate all possible edges where from < to (to ensure acyclicity)
        List<int[]> possibleEdges = new ArrayList<>();
        for (int from = 0; from < n - 1; from++) {
            for (int to = from + 1; to < n; to++) {
                if (!rootIndices.contains(to)) {
                    possibleEdges.add(new int[]{from, to});
                }
            }
        }

        // 4. Shuffle and add edges up to the target density
        int maxConnections = possibleEdges.size();
        int targetConnections = (int) Math.round(d * maxConnections);

        Collections.shuffle(possibleEdges, rand);
        Set<String> existingEdges = new HashSet<>();

        Map<String, Double> weightSums = new HashMap<>();
        Node targetNode;
        for (int i = 0; i < Math.min(targetConnections, maxConnections); i++) {
            int[] edge = possibleEdges.get(i);
            int from = edge[0];
            int to = edge[1];

            String sourceId = nodeIds.get(from);
            String targetId = nodeIds.get(to);

            String edgeKey = from + "->" + to;

            if (!existingEdges.contains(edgeKey)) {
                // sourceId wheight sum
                double currentSum = weightSums.getOrDefault(sourceId, 0.0);
                double maxAllowed = 1.0 - currentSum;
                double weight;
                // Se non c'è abbastanza spazio per il peso minimo, salta questa connection
                if (maxAllowed < 0.1) {
                    weight = 0.0;
                } else {
                    // random between 0.1 and maxAllowed
                    weight = 0.1 + rand.nextDouble() * (maxAllowed - 0.1);
                }

                existingEdges.add(edgeKey);
                targetNode = nodes.stream().filter(n1 -> n1.getId().equals(targetId)).findFirst().orElse(null);
                ConnectionPayload payload = generateRandomConnectionPayload(targetNode);
                targetNode.getPayload().setInitiator(false);
                String action = payload.getApiCall().getMethod() + "_" + payload.getApiCall().getPath().replaceAll("/", "");
                connections.add(new Connection(
                        UUID.randomUUID().toString(),
                        sourceId,
                        targetId,
                        false,
                        (int)(weight * 100) / 100.0, // 2 decimal
                        action,
                        payload
                ));
            }
        }

        // 5. Build diagram
        Diagram diagram = new Diagram();
        diagram.setId(UUID.randomUUID().toString());
        diagram.setName("Generated System");
        diagram.setData(new DiagramData(nodes, connections));

        return diagram;
    }

    private NodePayload generateRandomNodePayload(String nodeName, String path) {
        NodePayload payload = new NodePayload();
        Random rand = new Random();

        String[] languages = {"java", "python", "javascript"};
        String chosenLanguage = languages[rand.nextInt(languages.length)];

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

        // Create an endpoint
        Endpoint e1 = Endpoint.builder()
                .path(path)
                .method(methods[new Random().nextInt(methods.length)])
                .parameters(parameters)
                .responses(List.of(
                        new ApiResponse(200, "Successful response", "application/json","{}")
                ))
                .build();

        payload.setEndpoints(List.of(e1));

        String code = switch (chosenLanguage) {
            case "java" -> """
                    
                    """;
            case "python" -> """
                    
                    """;
            case "javascript" -> """
                    
                    """;
            default -> "// No code";
        };
        payload.setInitiator(true);

        return payload;
    }

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
