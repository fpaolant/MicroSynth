package it.univaq.microsynth.service;


import it.univaq.microsynth.domain.Diagram;
import it.univaq.microsynth.domain.Project;
import it.univaq.microsynth.domain.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;


public interface ProjectService {
    /**
     * Retrieves a paginated list of projects associated with a specific user.
     *
     * @param userName            The username of the user whose projects are to be retrieved.
     * @param paginatedRequestDTO An object containing pagination parameters such as page number and page size.
     * @return A ResponseEntity containing a Page of Project objects, which includes the list of projects and pagination metadata.
     */
    ResponseEntity<Page<Project>> getUserProjects(String userName, PaginatedRequestDTO paginatedRequestDTO);

    /**
     * Retrieves a specific project by its ID and the associated username.
     *
     * @param id       The unique identifier of the project to be retrieved.
     * @param userName The username of the user who owns the project.
     * @return A ResponseEntity containing a ProjectDTO object if the project is found, or an appropriate error response if not found or if access is denied.
     */
    ResponseEntity<ProjectDTO> getProjectById(String id, String userName);

    /**
     * Retrieves a specific diagram by its ID and the associated project ID.
     *
     * @param projectId The unique identifier of the project to which the diagram belongs.
     * @param diagramId The unique identifier of the diagram to be retrieved.
     * @return A ResponseEntity containing a DiagramDTO object if the diagram is found, or an appropriate error response if not found or if access is denied.
     */
    ResponseEntity<DiagramDTO> getDiagramById(String projectId, String diagramId);

    /**
     * Updates an existing project with new information provided in a ProjectDTO object.
     *
     * @param id         The unique identifier of the project to be updated.
     * @param projectDTO An object containing the updated information for the project.
     * @return A ResponseEntity containing a DocumentResponseDTO object if the update is successful, or an appropriate error response if the project is not found or if access is denied.
     */
    ResponseEntity<DocumentResponseDTO> updateProject(String id, ProjectDTO projectDTO);

    /**
     * Updates an existing diagram with new information provided in a DiagramDTO object.
     *
     * @param id         The unique identifier of the diagram to be updated.
     * @param diagramDTO An object containing the updated information for the diagram.
     * @return A ResponseEntity containing a DocumentResponseDTO object if the update is successful, or an appropriate error response if the diagram is not found or if access is denied.
     */
    ResponseEntity<DocumentResponseDTO> updateDiagram(String id, DiagramDTO diagramDTO);

    /**
     * Creates a new project based on the information provided in a ProjectDTO object and associates it with a specific user.
     *
     * @param projectDTO An object containing the information for the new project to be created.
     * @param userName   The username of the user who will own the newly created project.
     * @return A ResponseEntity containing a DocumentResponseDTO object if the project is successfully created, or an appropriate error response if there is an issue during creation.
     */
    ResponseEntity<DocumentResponseDTO> createProject(ProjectDTO projectDTO, String userName);

    /**
     * Deletes an existing project identified by its unique ID.
     * @param id
     * @return
     */
    ResponseEntity<?> deleteProject(String id);

    /**
     * Deletes an existing diagram identified by its unique ID and the associated project ID.
     * @param projectId
     * @param diagramId
     * @return
     */
    ResponseEntity<?> deleteDiagram(String projectId, String diagramId);

    /**
     * Generates a random diagram based on the information provided in a DiagramGenerationRequestDTO object.
     *
     * @param params An object containing the parameters needed for diagram generation, such as project ID, diagram type, and any additional settings.
     * @return A Diagram object representing the generated diagram based on the provided parameters.
     * @throws IllegalArgumentException If the provided parameters are invalid or if there is an issue during the generation process.
     */
    Diagram generate(DiagramGenerationRequestDTO params) throws IllegalArgumentException;

}
