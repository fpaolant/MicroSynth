package it.univaq.microsynth.domain;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

/**
 * Class to represent a user, with its username, password, email, firstname, lastname and roles
 */
@Document(collection = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class User extends BaseDocument {
    private static final long serialVersionUID = -7687035742032026470L;

    private String username;
    private String password;
    private String email;
    private String firstname;
    private String lastname;

    private Set<Role> roles;

}
