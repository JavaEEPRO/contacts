package si.inspirited.persistence.repositories;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import si.inspirited.persistence.model.Privilege;

@Repository
public interface PrivilegeRepository extends PagingAndSortingRepository<Privilege, Long> {

    Privilege findByName(String name);
}
