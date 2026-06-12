package it.uniroma3.siw.R3Play.service;

import it.uniroma3.siw.R3Play.model.Categoria;
import it.uniroma3.siw.R3Play.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.util.List;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    public List<Categoria> trovaTutte() {
        return (List<Categoria>) categoriaRepository.findAll();
    }

    public Categoria trovaPerId(Long id) {
        return categoriaRepository.findById(id).orElse(null);
    }

    @Transactional
    public Categoria salvaCategoria(Categoria categoria) {
        if (!categoriaRepository.existsByNome(categoria.getNome())) {
            return categoriaRepository.save(categoria);
        }
        return null;
    }

    @Transactional
    public void eliminaCategoria(Long id) {
        categoriaRepository.deleteById(id);
    }
}
