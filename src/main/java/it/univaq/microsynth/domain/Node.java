package it.univaq.microsynth.domain;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;


/**
 * Class to represent a node, with its id, label, shape, payload and weight
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Node implements Serializable {
    @Serial
    private static final long serialVersionUID = -8030066578324371602L;

    @NotNull
    private String id;

    @NotNull
    private String label;

    @NotNull
    private String shape;

    @NotNull
    private NodePayload payload;

    @NotNull
    private Double weight;
}
