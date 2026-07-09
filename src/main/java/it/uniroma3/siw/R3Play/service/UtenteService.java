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

    @Transactional(readOnly = true)
    public java.util.List<Utente> cercaPerTesto(String testo) {
        return userRepository.findByNomeOrCognomeContainingIgnoreCase(testo);
    }

    @Transactional(readOnly = true)
    public java.util.List<Object[]> ottieniClassifica() {
        return userRepository.findUtentiConMediaRecensioniByOrderByNomeAsc();
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
    // HELPER GLOBALI (Risoluzione utente da Principal)
    // =============================================

    /**
     * Risolve l'utente loggato a partire dal Principal di Spring Security.
     * Funziona sia con login locale (UserDetails) che con Google OAuth2.
     */
    @Transactional
    public Utente risolviUtente(Object principal) {
        if (principal == null) return null;

        if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User oauth) {
            Object emailAttr = oauth.getAttribute("email");
            if (emailAttr == null) return null;
            String email = emailAttr.toString();
            String nome = getAttr(oauth, "given_name", getAttr(oauth, "name", email.split("@")[0]));
            String cognome = getAttr(oauth, "family_name", "");
            return recuperaOCreaUtenteGoogle(email, nome, cognome);
        }

        if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            return trovaPerEmail(ud.getUsername()).orElse(null);
        }

        return null;
    }

    /**
     * Costruisce il nome completo da mostrare nell'interfaccia.
     * Se l'utente non ha nome/cognome, usa la prima parte dell'email.
     */
    public String buildNomeCompleto(Utente u) {
        if (u == null) return "Ospite";
        String n = u.getNome() != null ? u.getNome() : "";
        String c = u.getCognome() != null ? u.getCognome() : "";
        String fullName = (n + " " + c).trim();
        return fullName.isBlank() ? u.getEmail().split("@")[0] : fullName;
    }

    // =============================================
    // HELPER PRIVATI
    // =============================================

    private String getAttr(org.springframework.security.oauth2.core.user.OAuth2User oauth, String key, String fallback) {
        Object val = oauth.getAttribute(key);
        return (val != null && !val.toString().isBlank()) ? val.toString() : fallback;
    }

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
