package it.univaq.microsynth.domain;


import lombok.*;

import java.io.Serial;
import java.io.Serializable;


/**
 * Class to represent the payload of a connection, with its API call and endpoint
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ConnectionPayload implements Serializable {
    @Serial
    private static final long serialVersionUID = 4693796028274168435L;

    private ApiCall apiCall;

    private Endpoint endpoint;
}
