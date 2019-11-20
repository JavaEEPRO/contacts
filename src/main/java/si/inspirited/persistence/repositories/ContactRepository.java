package si.inspirited.persistence.repositories;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import si.inspirited.persistence.model.Contact;

@RepositoryRestResource(path = "contacts", collectionResourceRel = "contacts")
public interface ContactRepository extends PagingAndSortingRepository<Contact, Long> {
}
