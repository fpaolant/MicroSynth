package it.univaq.microsynth.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import it.univaq.microsynth.domain.Diagram;
import it.univaq.microsynth.domain.dto.DiagramDTO;
import it.univaq.microsynth.domain.dto.DocumentResponseDTO;
import it.univaq.microsynth.domain.dto.GenerationParamsDTO;
import it.univaq.microsynth.service.GeneratorService;
import it.univaq.microsynth.service.ProjectService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/diagram")
public class DiagramController {

    private static final Logger log = LoggerFactory.getLogger(ProjectController.class);
    private final ProjectService projectService;

    private final GeneratorService generatorService;

    public DiagramController(ProjectService projectService, GeneratorService generatorService) {
        this.projectService = projectService;
        this.generatorService = generatorService;
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

    /**
     * Delete a diagram for a project
     * @param projectId project id
     * @param diagramId diagram id
     * @return
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

    @PostMapping("/generate")
    public ResponseEntity<Diagram> generate(@RequestBody @Valid GenerationParamsDTO params) {
        Diagram diagram = generatorService.generate(params);
        return ResponseEntity.ok(diagram);
    }

}
