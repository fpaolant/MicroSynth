package it.univaq.microsynth.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * Base class for all documents in the database, with common fields like id, createdAt and updatedAt
 */
@Setter
@Getter
@EnableMongoAuditing
public class BaseDocument implements Serializable {
    @Serial
    private static final long serialVersionUID = 8267652114427102115L;

    @Id
    private String id;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

}
