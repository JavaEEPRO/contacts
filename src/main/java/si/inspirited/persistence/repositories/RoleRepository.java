package si.inspirited.persistence.repositories;

import org.springframework.data.repository.PagingAndSortingRepository;
import si.inspirited.persistence.model.Role;

public interface RoleRepository extends PagingAndSortingRepository<Role, Long> {
}
