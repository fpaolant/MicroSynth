package it.univaq.microsynth.repository;

import it.univaq.microsynth.domain.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;


public interface ProjectRepository extends MongoRepository<Project, String> {

    Optional<Project> findByIdAndOwner(String id, String userName);

    Page<Project> findAllByOwner(String owner, Pageable pageable);
}