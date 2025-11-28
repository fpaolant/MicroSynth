package it.univaq.microsynth.service;


import it.univaq.microsynth.domain.Diagram;
import it.univaq.microsynth.domain.Project;
import it.univaq.microsynth.domain.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;


public interface ProjectService {
    ResponseEntity<Page<Project>> getUserProjects(String userName, PaginatedRequestDTO paginatedRequestDTO);

    ResponseEntity<ProjectDTO> getProjectById(String id, String userName);

    ResponseEntity<DiagramDTO> getDiagramById(String projectId, String diagramId);

    ResponseEntity<DocumentResponseDTO> updateProject(String id, ProjectDTO projectDTO);

    ResponseEntity<DocumentResponseDTO> updateDiagram(String id, DiagramDTO diagramDTO);

    ResponseEntity<DocumentResponseDTO> createProject(ProjectDTO projectDTO, String userName);

    ResponseEntity<?> deleteProject(String id);

    ResponseEntity<?> deleteDiagram(String projectId, String diagramId);

    Diagram generate(DiagramGenerationRequestDTO params) throws IllegalArgumentException;

}
