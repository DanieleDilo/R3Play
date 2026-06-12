package it.uniroma3.siw.R3Play.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import it.uniroma3.siw.R3Play.model.Utente;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Utente, Long> {
    Optional<Utente> findByEmail(String email);
    boolean existsByEmail(String email);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM Utente u WHERE LOWER(u.nome) LIKE LOWER(CONCAT('%', :testo, '%')) OR LOWER(u.cognome) LIKE LOWER(CONCAT('%', :testo, '%'))")
    java.util.List<Utente> findByNomeOrCognomeContainingIgnoreCase(@org.springframework.data.repository.query.Param("testo") String testo);

    @org.springframework.data.jpa.repository.Query("SELECT u, AVG(r.valutazione), COUNT(r) FROM Utente u LEFT JOIN u.recensioniRicevute r GROUP BY u ORDER BY AVG(r.valutazione) DESC NULLS LAST")
    java.util.List<Object[]> findUtentiConMediaRecensioni();
}