package it.univaq.microsynth.service;


import it.univaq.microsynth.domain.dto.LoginResponseDTO;
import it.univaq.microsynth.domain.dto.UserCredentialsDTO;
import it.univaq.microsynth.domain.dto.UserDTO;
import org.springframework.http.ResponseEntity;


public interface AuthService {

    ResponseEntity<Boolean> register(UserDTO user);

    ResponseEntity<LoginResponseDTO> login(UserCredentialsDTO credentials);

    ResponseEntity<LoginResponseDTO> refresh(String token);

}
