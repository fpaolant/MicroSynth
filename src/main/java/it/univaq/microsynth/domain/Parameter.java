package it.univaq.microsynth.domain;

import it.univaq.microsynth.Enum.ParameterType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;


/**
 * Class to represent a parameter, with its name, type and whether it is required or not
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
@ToString
public class Parameter implements Serializable {
    @Serial
    private static final long serialVersionUID = 7789940623071028026L;

    @NotNull
    private String name;

    @Builder.Default
    private ParameterType type = ParameterType.STRING;

    @Builder.Default
    private boolean required = false;
}
