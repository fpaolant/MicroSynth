package it.univaq.microsynth.domain;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * Class to represent a position, with its x and y coordinates (sed for node in editor)
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Position implements Serializable {
    @Serial
    private static final long serialVersionUID = -3072920902527925120L;

    private Double x;
    private Double y;
}
