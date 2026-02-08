package it.univaq.microsynth.repository;

import it.univaq.microsynth.domain.Role;
import it.univaq.microsynth.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    /**
     * Find a user by their username. This method is used to retrieve a user based on their unique username, which is typically used for authentication and authorization purposes.
     *
     * @param username The username of the user to find.
     * @return An Optional containing the found user, or empty if no user with the given username exists.
     */
    Optional<User> findByUsername(String username);

    /**
     * Find a user by id. This method is used to retrieve a user based on their unique identifier, which is typically used for various operations such as updating user information or retrieving user details.
     * @param id The unique identifier of the user to find.
     * @return An Optional containing the found user, or empty if no user with the given id exists.
     */
    List<Role> getRolesById(String id);
}