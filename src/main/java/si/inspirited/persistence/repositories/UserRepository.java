package si.inspirited.persistence.repositories;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import si.inspirited.persistence.model.User;

@RepositoryRestResource(path = "users", collectionResourceRel = "users")
public interface UserRepository extends PagingAndSortingRepository<User, Long> {

    User findByLogin(String login);

    @Override
    <S extends User> S save(S s);
}
