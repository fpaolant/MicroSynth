package it.univaq.microsynth.domain.mapper;

import it.univaq.microsynth.domain.Project;
import it.univaq.microsynth.domain.dto.ProjectDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    ProjectMapper INSTANCE = Mappers.getMapper( ProjectMapper.class );

    @Mapping(target = "userName", source = "owner")
    ProjectDTO projectToProjectDTO(Project project);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", source = "userName")
    Project projectDTOtoProject(ProjectDTO projectDTO);
}
