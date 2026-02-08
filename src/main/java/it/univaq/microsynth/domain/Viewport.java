package it.univaq.microsynth.domain;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * Class to represent a viewport, with its x and y coordinates and its zoom level (Store editor viewport)
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Viewport implements Serializable {
    @Serial
    private static final long serialVersionUID = 1853319905779568707L;

    private Double x;
    private Double y;
    private Double k;
}
