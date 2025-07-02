package it.univaq.microsynth.service;


import it.univaq.microsynth.domain.Diagram;
import it.univaq.microsynth.domain.dto.GenerationParamsDTO;


public interface GeneratorService {

    Diagram generate(GenerationParamsDTO params);
}
