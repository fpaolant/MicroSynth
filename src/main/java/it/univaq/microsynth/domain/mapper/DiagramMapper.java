package it.univaq.microsynth.domain.mapper;

import it.univaq.microsynth.domain.Diagram;
import it.univaq.microsynth.domain.dto.DiagramDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "spring")
public interface DiagramMapper {
    DiagramMapper INSTANCE = Mappers.getMapper(DiagramMapper.class);

    DiagramDTO diagramToDiagramDTO(Diagram diagram);

    Diagram diagramDTOToDiagram(DiagramDTO diagramDTO);

}
