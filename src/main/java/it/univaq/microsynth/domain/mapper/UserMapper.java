package it.univaq.microsynth.domain.mapper;

import it.univaq.microsynth.domain.User;
import it.univaq.microsynth.domain.dto.UserDTO;
import it.univaq.microsynth.domain.dto.UserResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper( UserMapper.class );

    UserResponseDTO userToResponseDto(User user);
    User responseDTOToUser(UserResponseDTO userResponseDTO);

    UserDTO userToUserDto(User user);
    @Mapping(target = "id", ignore = true)
    User userDTOToUser(UserDTO userDTO);
}
