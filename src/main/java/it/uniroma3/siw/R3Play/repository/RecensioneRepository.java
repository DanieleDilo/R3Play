package it.uniroma3.siw.R3Play.repository;

import it.uniroma3.siw.R3Play.model.Recensione;
import org.springframework.data.repository.CrudRepository;

public interface RecensioneRepository extends CrudRepository<Recensione, Long> {
    // CrudRepository ci fornisce già il metodo findById
}
