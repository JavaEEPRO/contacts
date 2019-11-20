package si.inspirited.persistence.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
public class Group implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    private Long ownerId;
}
