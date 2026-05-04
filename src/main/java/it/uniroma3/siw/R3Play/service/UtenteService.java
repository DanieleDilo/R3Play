package it.uniroma3.siw.R3Play.service;

import it.uniroma3.siw.R3Play.model.Utente;
import it.uniroma3.siw.R3Play.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service layer per la gestione degli utenti.
 * Gestisce registrazione, aggiornamento profilo e recupero utente.
 */
@Service
public class UtenteService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // =============================================
    // OPERAZIONI DI LETTURA
    // =============================================

    @Transactional(readOnly = true)
    public Optional<Utente> trovaPerId(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Utente> trovaPerEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public boolean emailGiaRegistrata(String email) {
        return userRepository.existsByEmail(email);
    }

    // =============================================
    // OPERAZIONI DI SCRITTURA
    // =============================================

    /**
     * Registra un nuovo utente locale.
     * Verifica che l'email non sia già in uso e codifica la password.
     */
    @Transactional
    public Utente registraUtente(String nome, String cognome, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("Email già registrata: " + email);
        }

        Utente utente = new Utente();
        utente.setNome(nome);
        utente.setCognome(cognome);
        utente.setEmail(email);
        utente.setPassword(passwordEncoder.encode(password));
        utente.setProvider("LOCAL");
        utente.setRuolo("ROLE_USER");

        return userRepository.save(utente);
    }

    /**
     * Aggiorna nome e cognome dell'utente.
     */
    @Transactional
    public Utente aggiornaProfilo(Utente utente, String nome, String cognome) {
        utente.setNome(nome);
        utente.setCognome(cognome);
        return userRepository.save(utente);
    }

    /**
     * Recupera o crea un utente da OAuth2 Google.
     * Se l'utente non esiste, lo crea automaticamente.
     */
    @Transactional
    public Utente recuperaOCreaUtenteGoogle(String email, String nome, String cognome) {
        return userRepository.findByEmail(email).map(utente -> {
            // Aggiorna nome/cognome se mancanti
            boolean dirty = false;
            if ((utente.getNome() == null || utente.getNome().isBlank()) && nome != null) {
                utente.setNome(capitalize(nome));
                dirty = true;
            }
            if ((utente.getCognome() == null || utente.getCognome().isBlank()) && cognome != null) {
                utente.setCognome(capitalize(cognome));
                dirty = true;
            }
            return dirty ? userRepository.save(utente) : utente;
        }).orElseGet(() -> {
            // Crea nuovo utente Google
            Utente nuovo = new Utente();
            nuovo.setEmail(email);
            nuovo.setNome(capitalize(nome != null ? nome : "Utente"));
            nuovo.setCognome(capitalize(cognome != null ? cognome : "Google"));
            nuovo.setProvider("GOOGLE");
            nuovo.setRuolo("ROLE_USER");
            return userRepository.save(nuovo);
        });
    }

    // =============================================
    // HELPER PRIVATI
    // =============================================

    private String capitalize(String text) {
        if (text == null || text.isBlank()) return "Utente";
        String[] words = text.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            if (sb.length() > 0) sb.append(" ");
            sb.append(word.substring(0, 1).toUpperCase()).append(word.substring(1).toLowerCase());
        }
        return sb.toString();
    }
}
