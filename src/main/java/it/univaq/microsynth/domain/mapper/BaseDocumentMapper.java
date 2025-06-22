package it.univaq.microsynth.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring",
        uses = {
                UserMapper.class
})
public interface BaseDocumentMapper {
    BaseDocumentMapper INSTANCE = Mappers.getMapper( BaseDocumentMapper.class );

}
