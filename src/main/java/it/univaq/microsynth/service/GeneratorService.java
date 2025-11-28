package it.univaq.microsynth.service;

import it.univaq.microsynth.domain.dto.BundleGenerationRequestDTO;
import it.univaq.microsynth.domain.dto.DiagramDTO;

import java.io.File;
import java.util.List;

public interface GeneratorService {
    File generateBundle(List<BundleGenerationRequestDTO> requests) throws Exception;
    List<BundleGenerationRequestDTO> convertGraphToRequests(DiagramDTO graph);
}
