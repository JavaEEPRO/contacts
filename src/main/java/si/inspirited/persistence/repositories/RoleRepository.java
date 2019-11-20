package si.inspirited.persistence.repositories;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import si.inspirited.persistence.model.Role;

@Repository
public interface RoleRepository extends PagingAndSortingRepository<Role, Long> {

    Role findByName(String name);
}
