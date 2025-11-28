package it.univaq.microsynth.domain;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
@ToString
public class ParameterValue<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 7789940623071028026L;

    @NotNull
    private String name;

    @NotNull
    private T value;
}
