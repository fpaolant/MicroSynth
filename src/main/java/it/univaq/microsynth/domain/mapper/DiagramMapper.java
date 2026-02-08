package it.univaq.microsynth.domain.mapper;

import it.univaq.microsynth.domain.Diagram;
import it.univaq.microsynth.domain.dto.DiagramDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


/**
 * Mapper for Diagram and DiagramDTO
 */
@Mapper(componentModel = "spring")
public interface DiagramMapper {
    DiagramMapper INSTANCE = Mappers.getMapper(DiagramMapper.class);

    /**
     * Maps a Diagram to a DiagramDTO
     * @param diagram the Diagram to map
     * @return the mapped DiagramDTO
     */
    DiagramDTO diagramToDiagramDTO(Diagram diagram);

    /**
     * Maps a DiagramDTO to a Diagram
     * @param diagramDTO the DiagramDTO to map
     * @return the mapped Diagram
     */
    Diagram diagramDTOToDiagram(DiagramDTO diagramDTO);

}
