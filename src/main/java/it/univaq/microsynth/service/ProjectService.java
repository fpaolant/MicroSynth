package it.univaq.microsynth.service;


import it.univaq.microsynth.domain.Project;
import it.univaq.microsynth.domain.dto.DiagramDTO;
import it.univaq.microsynth.domain.dto.DocumentResponseDTO;
import it.univaq.microsynth.domain.dto.PaginatedRequestDTO;
import it.univaq.microsynth.domain.dto.ProjectDTO;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;


public interface ProjectService {
    ResponseEntity<Page<Project>> getUserProjects(String userName, PaginatedRequestDTO paginatedRequestDTO);

    ResponseEntity<ProjectDTO> getProjectById(String id, String userName);

    ResponseEntity<DocumentResponseDTO> updateProject(String id, ProjectDTO projectDTO);

    ResponseEntity<DocumentResponseDTO> updateDiagram(String id, DiagramDTO diagramDTO);

    ResponseEntity<DocumentResponseDTO> createProject(ProjectDTO projectDTO, String userName);

    ResponseEntity<?> deleteProject(String id);

    ResponseEntity<?> deleteDiagram(String projectId, String diagramId);
}
