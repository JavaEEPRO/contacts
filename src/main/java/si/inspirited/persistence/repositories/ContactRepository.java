package si.inspirited.persistence.repositories;

import org.springframework.data.repository.PagingAndSortingRepository;
import si.inspirited.persistence.model.Contact;

public interface ContactRepository extends PagingAndSortingRepository<Contact, Long> {
}
