package it.univaq.microsynth.domain;


import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * Class to represent the payload of a node, with its language, type, base path, description and endpoints
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class NodePayload implements Serializable {
    @Serial
    private static final long serialVersionUID = 155747012485606274L;

    @NotNull
    private String language = "javascript"; // javascript, java, python

    private String type = "";        // controller
    private String basePath = "";
    private String description = "";

    private List<Endpoint> endpoints = new ArrayList<>();

    @NotNull
    private Boolean initiator = false;
}
