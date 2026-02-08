package it.univaq.microsynth.domain;


import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to represent an API call, with its path, method and parameters
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class ApiCall implements Serializable {
    @Serial
    private static final long serialVersionUID = -944514557249279726L;

    private String path = "";
    private String method = "GET";      // GET, POST, etc.

    @Builder.Default
    private List<ParameterValue<?>> parameterValues = new ArrayList<>();

}
