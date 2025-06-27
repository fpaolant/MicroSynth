package it.univaq.microsynth.service.impl;

import it.univaq.microsynth.domain.Diagram;
import it.univaq.microsynth.domain.Project;
import it.univaq.microsynth.domain.dto.DiagramDTO;
import it.univaq.microsynth.domain.dto.DocumentResponseDTO;
import it.univaq.microsynth.domain.dto.PaginatedRequestDTO;
import it.univaq.microsynth.domain.dto.ProjectDTO;
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

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class ProjectServiceImpl implements ProjectService {

    private ProjectRepository projectRepository;

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

                    if (existingDiagramOpt.isPresent()) {
                        Diagram existingDiagram = existingDiagramOpt.get();
                        existingDiagram.setName(diagramDTO.getName());
                        existingDiagram.setData(diagramDTO.getData());
                        // Update other fields if needed
                    } else {
                        // Create a new diagram and add to the project
                        Diagram newDiagram = new Diagram();
                        newDiagram.setId(UUID.randomUUID().toString());
                        newDiagram.setName(diagramDTO.getName());
                        newDiagram.setData(diagramDTO.getData());
                        // Set other fields from diagramDTO as needed
                        project.getDiagrams().add(newDiagram);
                    }

                    // Save the updated project if necessary
                    projectRepository.save(project);

                    log.info("Updated project {} with diagram: {}", id, diagramDTO);
                    return ResponseEntity.ok(new DocumentResponseDTO(project.getId()));
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


}
