package it.univaq.microsynth.domain;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Connection {
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
    private Long weight;

    @NotNull
    private String label;

    @NotNull
    private Payload payload;
}
