package it.uniroma3.siw.R3Play.repository;

import org.springframework.data.repository.CrudRepository;
import it.uniroma3.siw.R3Play.model.Articolo;
import it.uniroma3.siw.R3Play.model.Utente;
import java.util.List;

public interface ArticoloRepository extends CrudRepository<Articolo, Long> {
    
    // Spring Boot capirà in automatico questa query dal nome del metodo!
    List<Articolo> findByVenditore(Utente venditore);
}