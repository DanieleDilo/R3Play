package it.uniroma3.siw.R3Play.repository;

import it.uniroma3.siw.R3Play.model.Categoria;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends CrudRepository<Categoria, Long> {
    public boolean existsByNome(String nome);
}
