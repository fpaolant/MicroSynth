package it.univaq.microsynth.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


/**
 * Mapper for BaseDocument and its subclasses
 */
@Mapper(
        componentModel = "spring",
        uses = { UserMapper.class}
)
public interface BaseDocumentMapper {
    BaseDocumentMapper INSTANCE = Mappers.getMapper( BaseDocumentMapper.class );

}
