package it.uniroma3.siw.R3Play.repository;

import it.uniroma3.siw.R3Play.model.Recensione;
import it.uniroma3.siw.R3Play.model.Utente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecensioneRepository extends JpaRepository<Recensione, Long> {

    // Tutte le recensioni ricevute da un utente (destinatario)
    List<Recensione> findByDestinatario(Utente destinatario);

    // Tutte le recensioni scritte da un utente (autore)
    List<Recensione> findByAutore(Utente autore);

    // Controlla se un utente ha già recensito un altro utente (no duplicati)
    boolean existsByAutoreAndDestinatario(Utente autore, Utente destinatario);

    // Media valutazione ricevuta da un venditore
    @Query("SELECT AVG(r.valutazione) FROM Recensione r WHERE r.destinatario = :utente")
    Optional<Double> calcolaMediaValutazione(@Param("utente") Utente utente);

    // Tutte le recensioni con autore già fetchato (evita N+1)
    @EntityGraph(attributePaths = {"autore", "destinatario"})
    List<Recensione> findByDestinatarioOrderByIdDesc(Utente destinatario);
}
