package it.uniroma3.siw.R3Play.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.R3Play.model.Articolo;
import it.uniroma3.siw.R3Play.model.Utente;

import java.util.List;
import java.util.Optional;

public interface ArticoloRepository extends JpaRepository<Articolo, Long> {

    // --- Query derivate da nome metodo ---
    List<Articolo> findByVenditore(Utente venditore);

    List<Articolo> findByNomeContainingIgnoreCaseOrDescrizioneContainingIgnoreCase(
            String nome, String descrizione);

    // --- Strategia 1: LAZY (default) — causa N+1 query ---
    // (usa semplicemente findAll() ereditato da JpaRepository)

    // --- Strategia 2: JOIN FETCH (JPQL) — una singola query ottimizzata ---
    @Query("SELECT DISTINCT a FROM Articolo a LEFT JOIN FETCH a.venditore LEFT JOIN FETCH a.fotoUrls")
    List<Articolo> findAllConVenditoreEFoto();

    // --- Strategia 3: EntityGraph — fetch selettivo, dichiarativo ---
    @EntityGraph(attributePaths = {"venditore", "fotoUrls"})
    List<Articolo> findAllByOrderByIdDesc();

    // --- Strategia 4: JPQL per articoli di un venditore con foto ---
    @Query("SELECT a FROM Articolo a LEFT JOIN FETCH a.fotoUrls WHERE a.venditore = :venditore")
    List<Articolo> findByVenditoreConFoto(@Param("venditore") Utente venditore);

    // --- Ricerca full-text con venditore già caricato ---
    @Query("SELECT DISTINCT a FROM Articolo a LEFT JOIN FETCH a.venditore " +
           "WHERE LOWER(a.nome) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(a.descrizione) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Articolo> searchConVenditore(@Param("q") String query);

    // Conta articoli per venditore (utile per statistiche)
    long countByVenditore(Utente venditore);

    // Trova per id con venditore già fetchato (evita lazy load sul controller)
    @EntityGraph(attributePaths = {"venditore", "fotoUrls"})
    Optional<Articolo> findWithDetailById(Long id);
}