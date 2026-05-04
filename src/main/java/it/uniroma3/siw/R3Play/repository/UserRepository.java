package it.uniroma3.siw.R3Play.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import it.uniroma3.siw.R3Play.model.Utente;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Utente, Long> {
    Optional<Utente> findByEmail(String email);
    boolean existsByEmail(String email);
}