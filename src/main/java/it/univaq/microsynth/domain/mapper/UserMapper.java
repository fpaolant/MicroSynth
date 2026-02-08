package it.univaq.microsynth.domain.mapper;

import it.univaq.microsynth.domain.User;
import it.univaq.microsynth.domain.dto.UserDTO;
import it.univaq.microsynth.domain.dto.UserResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Mapper for User and UserDTO
 */
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper( UserMapper.class );

    /**
     * Maps a User to a UserResponseDTO
     * @param user the User to map
     * @return the mapped UserResponseDTO
     */
    UserResponseDTO userToResponseDto(User user);

        /**
        * Maps a UserResponseDTO to a User
        * @param userResponseDTO the UserResponseDTO to map
        * @return the mapped User
        */
    User responseDTOToUser(UserResponseDTO userResponseDTO);

    /**
     * Maps a User to a UserDTO
     * @param user the User to map
     * @return the mapped UserDTO
     */
    UserDTO userToUserDto(User user);

    /**
     * Maps a UserDTO to a User
     * @param userDTO the UserDTO to map
     * @return the mapped User
     */
    User userDTOToUser(UserDTO userDTO);
}
