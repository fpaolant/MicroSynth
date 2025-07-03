package it.univaq.microsynth.service;


import it.univaq.microsynth.domain.Diagram;
import it.univaq.microsynth.domain.dto.GenerationParamsDTO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public interface GeneratorService {

    Diagram generate(GenerationParamsDTO params);
    String exportDockerCompose(Diagram diagram) throws IOException;
    ByteArrayOutputStream exportDockerComposeFull(Diagram diagram) throws IOException;
}
