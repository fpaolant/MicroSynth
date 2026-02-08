package it.univaq.microsynth.domain;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;


/**
 * Class to represent a connection between two nodes in the graph, with its source, target, weight and label
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Connection implements Serializable {
    @Serial
    private static final long serialVersionUID = -294424037526113965L;

    @NotNull
    private String id;

    @NotNull
    private String source;

    @NotNull
    private String target;

    @NotNull
    private boolean isLoop;

    @NotNull
    private Double weight;

    @NotNull
    private String label;

    @NotNull
    private ConnectionPayload payload;
}
