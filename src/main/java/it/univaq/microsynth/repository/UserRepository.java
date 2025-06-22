package it.univaq.microsynth.repository;

import it.univaq.microsynth.domain.Role;
import it.univaq.microsynth.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);

    List<Role> getRolesById(String id);
}