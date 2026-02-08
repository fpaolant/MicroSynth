package it.univaq.microsynth.domain;


import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * Class to represent an endpoint, with its path, method, parameters and responses
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Endpoint implements Serializable {
    @Serial
    private static final long serialVersionUID = 3824507327591719012L;

    private String path = "";
    private String summary = "";

    private String method = "";      // GET, POST, etc.
    private List<Parameter> parameters = new ArrayList<>(); // parameters array
    private List<ApiResponse> responses = List.of(new ApiResponse(200, "Successful operation", "application/json", "{}"));
    private String code = "";

}
