package it.univaq.microsynth.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import it.univaq.microsynth.domain.Diagram;
import it.univaq.microsynth.domain.dto.DiagramDTO;
import it.univaq.microsynth.domain.dto.DocumentResponseDTO;
import it.univaq.microsynth.domain.dto.DiagramGenerationRequestDTO;
import it.univaq.microsynth.service.ProjectService;
import jakarta.validation.Valid;
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
     * Update a diagram for a project
     * @param projectId project id
     * @param diagramDTO diagram to update
     * @return updated diagram
     */
    @Operation(summary = "Update project diagram", description = "return updated project id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "404", description = "diagram not found"),
            @ApiResponse(responseCode = "500", description = "Internal server Error")
    })
    @PutMapping("/{project_id}")
    public ResponseEntity<DocumentResponseDTO> updateDiagram(@PathVariable("project_id") String projectId, @RequestBody DiagramDTO diagramDTO) {
        return projectService.updateDiagram(projectId, diagramDTO);
    }

    /**
     * Delete a diagram for a project
     * @param projectId project id
     * @param diagramId diagram id
     * @return status of deletion
     */
    @Operation(summary = "Delete project diagram", description = "return status of deletion")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "404", description = "diagram not found"),
            @ApiResponse(responseCode = "500", description = "Internal server Error")
    })
    @DeleteMapping("/{project_id}/{diagram_id}")
    public ResponseEntity<?> deleteDiagram(@PathVariable("project_id") String projectId,
                                           @PathVariable("diagram_id") String diagramId) {
        return projectService.deleteDiagram(projectId, diagramId);
    }

    /**
     * Generate a diagram for a project
     * @param params parameters for diagram generation
     * @return generated diagram
     */
    @Operation(summary = "Generate a diagram", description = "return diagram")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server Error")
    })
    @PostMapping("/generate")
    public ResponseEntity<?> generate(@RequestBody @Valid DiagramGenerationRequestDTO params) {

        try {
            Diagram diagram = projectService.generate(params);
            return ResponseEntity.ok(diagram);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error generating diagram: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }

    }

}
