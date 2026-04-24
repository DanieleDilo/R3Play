package it.uniroma3.siw.R3Play.authentication;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import it.uniroma3.siw.R3Play.model.Utente;
import it.uniroma3.siw.R3Play.repository.UserRepository;

@Component
public class AdminInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Controlla se l'admin esiste già nel DB
        if (userRepository.findByEmail("admin@email.it").isEmpty()) {
            Utente admin = new Utente();
            admin.setNome("Amministratore");
            admin.setCognome("Sistema");
            admin.setEmail("admin@email.it");
            admin.setPassword(passwordEncoder.encode("Admin"));
            admin.setProvider("LOCAL");
            admin.setRuolo("ROLE_ADMIN");
            
            userRepository.save(admin);
            System.out.println("Utente Admin creato con successo. Email: admin@email.it | Password: Admin");
        }
    }
}
