package si.inspirited.persistence.repositories;

import org.springframework.data.repository.PagingAndSortingRepository;
import si.inspirited.persistence.model.Group;

public interface GroupRepository extends PagingAndSortingRepository<Group, Long> {
}
