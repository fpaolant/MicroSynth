package it.univaq.microsynth.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import it.univaq.microsynth.domain.dto.DocumentResponseDTO;
import it.univaq.microsynth.domain.dto.PaginatedRequestDTO;
import it.univaq.microsynth.domain.dto.ProjectDTO;
import it.univaq.microsynth.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/project")
public class ProjectController {

    private static final Logger log = LoggerFactory.getLogger(ProjectController.class);
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Operation(summary = "Get all user projects", description = "return list of user projects")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "500", description = "Internal server Error")
    })
    @GetMapping
    public ResponseEntity<?> getUserProjects(PaginatedRequestDTO paginatedRequestDTO) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        return projectService.getUserProjects(userName, paginatedRequestDTO);
    }

    @Operation(summary = "Get project by id", description = "return project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "404", description = "project not found"),
            @ApiResponse(responseCode = "500", description = "Internal server Error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProjectDTO> getProjectById(@PathVariable String id) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        return projectService.getProjectById(id, userName);
    }

    @Operation(summary = "Create project", description = "return created project id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "500", description = "Internal server Error")
    })
    @PostMapping
    public ResponseEntity<DocumentResponseDTO> createProject(@RequestBody ProjectDTO projectDTO) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("User {} is creating a project", userName);
        projectDTO.setUserName(userName);
        return projectService.createProject(projectDTO, userName);
    }

    @Operation(summary = "Update project", description = "return updated project id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "404", description = "project not found"),
            @ApiResponse(responseCode = "500", description = "Internal server Error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<DocumentResponseDTO> updateProject(@PathVariable String id, @RequestBody ProjectDTO projectDTO) {
        return projectService.updateProject(id, projectDTO);
    }

    @Operation(summary = "Delete project", description = "return success message")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "404", description = "project not found"),
            @ApiResponse(responseCode = "500", description = "Internal server Error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable String id) {
        return projectService.deleteProject(id);
    }





}
