package it.univaq.microsynth.service;


import it.univaq.microsynth.domain.Diagram;
import it.univaq.microsynth.domain.dto.GenerationParamsDTO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public interface GeneratorService {

    Diagram generate(GenerationParamsDTO params);
    ByteArrayOutputStream exportDockerCompose(Diagram diagram) throws IllegalArgumentException, IOException;
}
