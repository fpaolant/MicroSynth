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
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;


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
    @PutMapping("/{project_id}")
    public ResponseEntity<DocumentResponseDTO> updateDiagram(@PathVariable("project_id") String projectId, @RequestBody DiagramDTO diagramDTO) {
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

    /**
     * Generate a diagram for a project
     * @param params GenerationParamsDTO
     * @return Diagram
     */
    @Operation(summary = "Generate a diagram", description = "return diagram")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server Error")
    })
    @PostMapping("/generate")
    public ResponseEntity<?> generate(@RequestBody @Valid GenerationParamsDTO params) {
        log.info("Generating a diagram {}", params.toString());

        try {
            Diagram diagram = generatorService.generate(params);
            log.info("Generated diagram");
            return ResponseEntity.ok(diagram);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error generating diagram: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }

    }



    /**
     * Export docker compose FULL for a given diagram
     * @param diagram Diagram
     * @return InputStreamResource file stream yaml of docker compose
     * microsynth.zip
     * ├── docker-compose.yml
     * ├── service0/
     * │   ├── Dockerfile
     * │   └── main.py
     * ├── service1/
     * │   ├── Dockerfile
     * │   └── Main.java
     * ...
     */
    @PostMapping("/export/compose")
    public ResponseEntity<InputStreamResource> exportDockerCompose(@RequestBody @Nullable Diagram diagram) throws IOException {
        if(diagram == null) diagram = this.generatorService.generate(new GenerationParamsDTO(3,1,0.5));
        ByteArrayOutputStream zipStream = generatorService.exportDockerCompose(diagram);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=microsynth.zip");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(zipStream.size())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(new ByteArrayInputStream(zipStream.toByteArray())));
    }

}
