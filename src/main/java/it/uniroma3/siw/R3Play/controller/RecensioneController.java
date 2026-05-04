package it.uniroma3.siw.R3Play.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import it.uniroma3.siw.R3Play.model.Recensione;
import it.uniroma3.siw.R3Play.model.Utente;
import it.uniroma3.siw.R3Play.repository.RecensioneRepository;
import it.uniroma3.siw.R3Play.repository.UserRepository;

@Controller
public class RecensioneController {

    @Autowired
    private RecensioneRepository recensioneRepository;

    @Autowired
    private UserRepository userRepository;

    // ==========================================
    // ELIMINA RECENSIONE
    // ==========================================
    @GetMapping("/recensione/elimina/{id}")
    public String eliminaRecensione(@PathVariable("id") Long id,
            @AuthenticationPrincipal Object principal) { 

        Recensione recensione = this.recensioneRepository.findById(id).orElse(null);
        Utente utenteLoggato = getUtenteLoggato(principal);

        if (recensione != null && utenteLoggato != null) {
            if ("ROLE_ADMIN".equals(utenteLoggato.getRuolo()) || (recensione.getAutore() != null && utenteLoggato.getEmail().equals(recensione.getAutore().getEmail()))) {
                Long idVenditore = recensione.getDestinatario().getId();
                this.recensioneRepository.delete(recensione);
                return "redirect:/utente/" + idVenditore;
            }
        }
        return "redirect:/";
    }

    // Metodo helper per ottenere l'utente loggato
    private Utente getUtenteLoggato(Object principal) {
        if (principal == null) return null;

        String email = null;

        if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
            org.springframework.security.oauth2.core.user.OAuth2User oauth = (org.springframework.security.oauth2.core.user.OAuth2User) principal;
            Object emailAttr = oauth.getAttribute("email");
            email = emailAttr != null ? emailAttr.toString() : null;
        } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            email = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        }

        if (email != null) {
            return this.userRepository.findByEmail(email).orElse(null);
        }
        return null;
    }
}