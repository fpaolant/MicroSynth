package it.univaq.microsynth.domain;

import lombok.*;
import org.springframework.http.MediaType;

import java.io.Serial;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ApiResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 4446395446478890360L;

    private int status = 200;
    private String description = "";
    private String type = "application/json"; // MediaType.APPLICATION_JSON
    private Object content = "";
}