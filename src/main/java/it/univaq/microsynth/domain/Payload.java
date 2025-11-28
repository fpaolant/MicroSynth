package it.univaq.microsynth.domain;


import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.http.MediaType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Payload implements Serializable {
    private static final long serialVersionUID = -8030066578324371602L;

    @NotNull
    private String code = "";

    @NotNull
    private String language = "javascript"; // javascript, java, python

    // Campi specifici NodePayload
    private String type = "";        // controller
    private String basePath = "";
    private String description = "";
    private String method = "";      // GET, POST, etc.
    private List<Parameter> parameters = new ArrayList<>(); // parameters array
    private List<ApiResponse> responses = List.of(new ApiResponse(200, "Successful operation", "application/json", "{}"));

    // Campi specifici ConnectionPayload
    private String path = "";
    private String summary = "";
}
