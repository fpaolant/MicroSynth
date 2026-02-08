package it.univaq.microsynth.controller;

import it.univaq.microsynth.domain.dto.BundleGenerationRequestDTO;
import it.univaq.microsynth.domain.dto.DiagramDTO;
import it.univaq.microsynth.service.GeneratorService;
import it.univaq.microsynth.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/generator")
@RequiredArgsConstructor
public class GeneratorController {
    private final GeneratorService generatorService;
    private final ProjectService projectService;


    @GetMapping(value = "/generate/{project_id}/{diagram_id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<FileSystemResource> generate(@PathVariable("project_id") String projectId, @PathVariable("diagram_id") String diagramId) {

        log.info("[GENERATOR] Start generating files for project {} and diagram {}", projectId, diagramId);
        ResponseEntity<DiagramDTO> diagramResponse = projectService.getDiagramById(projectId, diagramId);

        if (diagramResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
            log.error("[GENERATOR] Diagram not found for project {} and {}", projectId, diagramId);
            return ResponseEntity.notFound().build();
        }
        DiagramDTO diagram = diagramResponse.getBody();

        // convert diagram to bundle generation requests
        List<BundleGenerationRequestDTO> requests = generatorService.convertGraphToRequests(diagram);

        try {
            File zipFile = generatorService.generateBundle(requests);
            FileSystemResource resource = new FileSystemResource(zipFile);
            log.info("[GENERATOR] Bundle zip with name {} created", zipFile.getName());

            log.info("[GENERATOR] End generating files for project {}", projectId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipFile.getName() + "\"")
                    .contentLength(zipFile.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            log.error("[GENERATOR] error generating bundle for {} with error {}", projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
}
