package it.univaq.microsynth.service.impl;

import it.univaq.microsynth.domain.Role;
import it.univaq.microsynth.domain.dto.PaginatedRequestDTO;
import it.univaq.microsynth.domain.dto.UserResponseDTO;
import it.univaq.microsynth.domain.mapper.UserMapper;
import it.univaq.microsynth.repository.UserRepository;
import it.univaq.microsynth.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class AccountServiceImpl implements AccountService {
    private UserRepository userRepository;
    private UserMapper userMapper;

    public AccountServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public ResponseEntity<Page<UserResponseDTO>> getAllUsers(PaginatedRequestDTO paginatedRequestDTO) {
        Sort sort = paginatedRequestDTO.getSortDir().equalsIgnoreCase("asc") ? Sort.by(paginatedRequestDTO.getSortBy()).ascending() : Sort.by(paginatedRequestDTO.getSortBy()).descending();
        PageRequest pageRequest = PageRequest.of(paginatedRequestDTO.getPage(), paginatedRequestDTO.getSize(), sort);
        Page<UserResponseDTO> users = userRepository.findAll(pageRequest)
                .map(userMapper::userToResponseDto);
        return ResponseEntity.ok(users);
    }

    @Override
    public List<Role> getUserRoles(String id) {
        return userRepository.getRolesById(id);
    }

    @Override
    public ResponseEntity<UserResponseDTO> getUserById(String id) {
        return userRepository.findById(id)
                .map(userMapper::userToResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<UserResponseDTO> createUser(UserResponseDTO user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.status(409).build();
        }
        user.setCreatedAt(String.valueOf(LocalDateTime.now()));
        user.setUpdatedAt(String.valueOf(LocalDateTime.now()));
        user.setRoles(Set.of(Role.USER));
        userRepository.save(userMapper.responseDTOToUser(user));
        return ResponseEntity.ok(user);
    }

    @Override
    public ResponseEntity<UserResponseDTO> updateUser(String id, UserResponseDTO user) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    existingUser.setUsername(user.getUsername());
                    existingUser.setFirstname(user.getFirstname());
                    existingUser.setLastname(user.getLastname());
                    existingUser.setEmail(user.getEmail());
                    existingUser.setUpdatedAt(LocalDateTime.now());
                    userRepository.save(existingUser);
                    return ResponseEntity.ok(userMapper.userToResponseDto(existingUser));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<String> deleteUser(String id) {
        return userRepository.findById(id)
                .map(user -> {
                    userRepository.delete(user);
                    return ResponseEntity.ok("User deleted successfully");
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<String> promoteUser(String id) {
    return userRepository.findById(id)
            .map(user -> {
                if (user.getRoles().contains(Role.ADMIN)) {
                    return ResponseEntity.ok("User promoted successfully");
                }
                user.getRoles().add(Role.ADMIN);
                user.setUpdatedAt(LocalDateTime.now());
                // Save the updated user
                userRepository.save(user);
                return ResponseEntity.ok("User promoted successfully");
            })
            .orElse(ResponseEntity.notFound().build());
}

    @Override
    public ResponseEntity<String> demoteUser(String id) {
        return userRepository.findById(id)
                .map(user -> {
                    user.getRoles().remove(Role.ADMIN);
                    user.setUpdatedAt(LocalDateTime.now());
                    // Save the updated user
                    userRepository.save(user);
                    return ResponseEntity.ok("User demoted successfully");
                })
                .orElse(ResponseEntity.notFound().build());
    }


}
