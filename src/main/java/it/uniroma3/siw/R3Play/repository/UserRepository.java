package it.uniroma3.siw.R3Play.repository;

import org.springframework.data.repository.CrudRepository;
import it.uniroma3.siw.R3Play.model.Utente;
import java.util.Optional;

public interface UserRepository extends CrudRepository<Utente, Long> {
    Optional<Utente> findByEmail(String email);
}