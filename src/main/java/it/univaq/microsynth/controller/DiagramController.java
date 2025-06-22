package it.univaq.microsynth.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import it.univaq.microsynth.domain.dto.DiagramDTO;
import it.univaq.microsynth.domain.dto.DocumentResponseDTO;
import it.univaq.microsynth.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/diagram")
public class DiagramController {

    private static final Logger log = LoggerFactory.getLogger(ProjectController.class);
    private final ProjectService projectService;

    public DiagramController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * Create a new diagram for a project
     * @param projectId project id
     * @param diagramDTO
     * @return
     */
    @Operation(summary = "Update project diagram", description = "return updated project id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "404", description = "diagram not found"),
            @ApiResponse(responseCode = "500", description = "Internal server Error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<DocumentResponseDTO> updateDiagram(@PathVariable("id") String projectId, @RequestBody DiagramDTO diagramDTO) {
        return projectService.updateDiagram(projectId, diagramDTO);
    }


}
