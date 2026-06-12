package it.uniroma3.siw.R3Play.authentication;

import it.uniroma3.siw.R3Play.model.Categoria;
import it.uniroma3.siw.R3Play.model.Utente;
import it.uniroma3.siw.R3Play.repository.CategoriaRepository;
import it.uniroma3.siw.R3Play.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Inizializzazione Admin se non esiste
        if (!userRepository.existsByEmail("admin@r3play.it")) {
            Utente admin = new Utente();
            admin.setNome("Admin");
            admin.setCognome("R3Play");
            admin.setEmail("admin@r3play.it");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setRuolo("ROLE_ADMIN");
            admin.setProvider("LOCAL");
            userRepository.save(admin);
        }

        // Inizializzazione categorie predefinite se vuoto
        if (categoriaRepository.count() == 0) {
            String[] nomiCategorie = {"Calcio", "Tennis", "Nuoto", "Basket", "Palestra", "Ciclismo"};
            for (String nome : nomiCategorie) {
                Categoria c = new Categoria();
                c.setNome(nome);
                categoriaRepository.save(c);
            }
        }
    }
}
