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

    List<Articolo> findByVenditore(Utente venditore);

    List<Articolo> findByNomeContainingIgnoreCaseOrDescrizioneContainingIgnoreCase(
            String nome, String descrizione);

    @EntityGraph(attributePaths = {"venditore", "fotoUrls"})
    List<Articolo> findAllByOrderByIdDesc();

    @Query("SELECT a FROM Articolo a LEFT JOIN FETCH a.fotoUrls WHERE a.venditore = :venditore")
    List<Articolo> findByVenditoreConFoto(@Param("venditore") Utente venditore);

    @Query("SELECT DISTINCT a FROM Articolo a LEFT JOIN FETCH a.venditore " +
           "WHERE LOWER(a.nome) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(a.descrizione) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Articolo> searchConVenditore(@Param("q") String query);


    long countByVenditore(Utente venditore);

    @EntityGraph(attributePaths = {"venditore", "fotoUrls"})
    Optional<Articolo> findWithDetailById(Long id);

    List<Articolo> findAllByOrderByPrezzoDesc();

    List<Articolo> findByCategoriaId(Long categoriaId);

}