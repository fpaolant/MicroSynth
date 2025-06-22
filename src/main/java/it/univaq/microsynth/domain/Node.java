package it.univaq.microsynth.domain;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Node {
    private static final long serialVersionUID = -8030066578324371602L;

    @NotNull
    private String id;

    @NotNull
    private String label;

    @NotNull
    private String shape;

    @NotNull
    private Payload payload;

    @NotNull
    private Long weight;
}
