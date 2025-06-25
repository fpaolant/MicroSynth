package it.univaq.microsynth.service.impl;

import it.univaq.microsynth.domain.Role;
import it.univaq.microsynth.domain.User;
import it.univaq.microsynth.domain.dto.ChangePasswordDTO;
import it.univaq.microsynth.domain.dto.LoginResponseDTO;
import it.univaq.microsynth.domain.dto.UserCredentialsDTO;
import it.univaq.microsynth.domain.dto.UserDTO;
import it.univaq.microsynth.domain.mapper.UserMapper;
import it.univaq.microsynth.repository.UserRepository;
import it.univaq.microsynth.service.AuthService;
import it.univaq.microsynth.service.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;


    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }


    @Override
    public ResponseEntity<Boolean> register(UserDTO userDTO) {
        String encodedPassword = passwordEncoder.encode(userDTO.getPassword());
        userDTO.setPassword(encodedPassword);


        if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            return ResponseEntity.status(409).build();
        }

        User user = UserMapper.INSTANCE.userDTOToUser(userDTO);

        // USER default role
        user.setRoles(Set.of(Role.USER));
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);
        return ResponseEntity.ok(true);
    }

    @Override
    public ResponseEntity<LoginResponseDTO> login(UserCredentialsDTO credentials) {
        Optional<User> user = userRepository.findByUsername(credentials.getUsername());
        log.info("User {} trying to login", user.get().getUsername());
        if (user.isEmpty() || !passwordEncoder.matches(credentials.getPassword(), user.get().getPassword())) {
            log.info("User {} ACCESS DENIED", user.get().getUsername());
            return ResponseEntity.status(401).build();
        }
        log.info("User {} granted ACCESS", user.get().getUsername());
        String token = jwtService.generateToken(user.get());

        return ResponseEntity.ok(new LoginResponseDTO(token, user.get().getUsername()));
    }

    @Override
    public ResponseEntity<LoginResponseDTO> refresh(String token) {
        if (!jwtService.validateToken(token)) {
            return ResponseEntity.status(401).build();
        }

        String username = jwtService.extractUsername(token);
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        String newToken = jwtService.generateToken(user.get());
        return ResponseEntity.ok(new LoginResponseDTO(newToken, user.get().getUsername()));
    }

    @Override
    public ResponseEntity<Boolean> changePassword(ChangePasswordDTO changePasswordDTO) {
        Optional<User> optionalUser = userRepository.findByUsername(changePasswordDTO.getUsername());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(404).body(false);
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(changePasswordDTO.getOldPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body(false);
        }

        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(true);
    }

}
