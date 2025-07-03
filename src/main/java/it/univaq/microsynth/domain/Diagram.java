package it.univaq.microsynth.domain;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serial;

@Document(collection = "diagrams  ")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Diagram extends BaseDocument {
    @Serial
    private static final long serialVersionUID = -294424037526113965L;

    @NotNull
    private String id;

    @NotNull
    private String name;

    @NotNull
    private DiagramData data;

}
