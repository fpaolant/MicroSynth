package it.univaq.microsynth.repository;

import it.univaq.microsynth.domain.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;


public interface ProjectRepository extends MongoRepository<Project, String> {

    /**
     * Find a project by its ID and owner. This method is used to ensure that a user can only access their own projects.
     *
     * @param id       The ID of the project to find.
     * @param userName The username of the owner of the project.
     * @return An Optional containing the found project, or empty if no project with the given ID and owner exists.
     */
    Optional<Project> findByIdAndOwner(String id, String userName);

    /**
     * Find all projects owned by a specific user, with pagination support. This method is used to retrieve a paginated list of projects for a user.
     *
     * @param owner    The username of the owner of the projects to find.
     * @param pageable A Pageable object containing pagination information such as page number and page size.
     * @return A Page containing the projects owned by the specified user, according to the provided pagination parameters.
     */
    Page<Project> findAllByOwner(String owner, Pageable pageable);
}