package it.univaq.microsynth.domain;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;


/**
 * Class to represent the data of a diagram, with its nodes and connections
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class DiagramData implements Serializable {
    @Serial
    private static final long serialVersionUID = -294424037526113965L;

    @NotNull
    private List<Node> nodes;

    @NotNull
    private List<Connection> connections;

}

