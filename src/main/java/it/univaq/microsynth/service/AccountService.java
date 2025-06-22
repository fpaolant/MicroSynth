package it.univaq.microsynth.service;


import it.univaq.microsynth.domain.Role;
import it.univaq.microsynth.domain.dto.PaginatedRequestDTO;
import it.univaq.microsynth.domain.dto.UserResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.util.List;


public interface AccountService {
    ResponseEntity<Page<UserResponseDTO>> getAllUsers(PaginatedRequestDTO paginatedRequestDTO);

    List<Role> getUserRoles(String id);

    ResponseEntity<UserResponseDTO> getUserById(String id);

    ResponseEntity<UserResponseDTO> updateUser(String id, UserResponseDTO user);

    ResponseEntity<String> deleteUser(String id);

    ResponseEntity<String> promoteUser(String id);

    ResponseEntity<String> demoteUser(String id);

    ResponseEntity<UserResponseDTO> createUser(UserResponseDTO user);
}
