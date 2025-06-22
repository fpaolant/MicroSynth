package it.univaq.microsynth.domain;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Payload {
    private static final long serialVersionUID = -8030066578324371602L;

    @NotNull
    private String code;

    @NotNull
    private String language;
}
