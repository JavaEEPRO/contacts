package si.inspirited.persistence.model;

import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;

@Entity
@Data
public class Privilege implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    @ManyToMany(mappedBy = "privileges")
    private Collection<Role> roles;

    public Privilege() {
        super();
    }

    public Privilege(final String name) {
        super();
        this.name = name;
    }
}
