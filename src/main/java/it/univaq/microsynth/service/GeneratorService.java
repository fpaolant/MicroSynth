package it.univaq.microsynth.service;

import it.univaq.microsynth.domain.dto.BundleGenerationRequestDTO;
import it.univaq.microsynth.domain.dto.DiagramDTO;

import java.io.File;
import java.util.List;

public interface GeneratorService {
    /**
     * Generates a zip file containing the generated code based on the provided list of BundleGenerationRequestDTO.
     *
     * @param requests A list of BundleGenerationRequestDTO containing the information needed for code generation.
     * @return A File object representing the generated zip file.
     * @throws Exception If an error occurs during the generation process.
     */
    File generateBundle(List<BundleGenerationRequestDTO> requests) throws Exception;

    /**
     * Converts a DiagramDTO into a list of BundleGenerationRequestDTO, which can be used for code generation.
     *
     * @param graph The DiagramDTO representing the graph to be converted.
     * @return A list of BundleGenerationRequestDTO derived from the provided DiagramDTO.
     */
    List<BundleGenerationRequestDTO> convertGraphToRequests(DiagramDTO graph);
}
