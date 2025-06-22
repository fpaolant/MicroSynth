package it.univaq.microsynth.domain;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class DiagramData {
    private static final long serialVersionUID = -294424037526113965L;

    @NotNull
    private List<Node> nodes;

    @NotNull
    private List<Connection> connections;
}

