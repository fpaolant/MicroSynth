package it.univaq.microsynth.domain;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Class to represent a project, with its name, owner and diagrams
 */
@Document(collection = "projects")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Project extends BaseDocument {
    private static final long serialVersionUID = -294424037526113965L;

    @NotNull
    private String name;

    @NotNull
    private String owner;

    private List<Diagram> diagrams;

}
