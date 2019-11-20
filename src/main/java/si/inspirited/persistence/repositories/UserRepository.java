package si.inspirited.persistence.repositories;

import org.springframework.data.repository.PagingAndSortingRepository;
import si.inspirited.persistence.model.User;

public interface UserRepository extends PagingAndSortingRepository<User, Long> {
}
