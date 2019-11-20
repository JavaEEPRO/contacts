package si.inspirited.persistence.repositories;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import si.inspirited.persistence.model.Group;

@RepositoryRestResource(path = "groups", collectionResourceRel = "groups")
public interface GroupRepository extends PagingAndSortingRepository<Group, Long> {
}
