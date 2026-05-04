package it.uniroma3.siw.R3Play.controller;

import it.uniroma3.siw.R3Play.model.Utente;
import it.uniroma3.siw.R3Play.service.UtenteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * CONTROLLER LAYER — Login e Registrazione
 *
 * Gestisce le richieste HTTP per autenticazione.
 * Delega la logica di registrazione a UtenteService.
 */
@Controller
public class LoginController {

    @Autowired
    private UtenteService utenteService;

    @GetMapping("/login")
    public String mostraLogin() {
        return "login";
    }

    @GetMapping("/registrati")
    public String mostraFormRegistrazione(Model model) {
        model.addAttribute("utente", new Utente());
        return "registrazione";
    }

    /**
     * Gestisce la registrazione di un nuovo utente.
     * Tutta la logica (controllo duplicati, encoding password) è nel service.
     */
    @PostMapping("/registrati")
    public String registraNuovoUtente(@ModelAttribute("utente") Utente utente,
                                      @RequestParam(value = "confermaPassword", required = false) String confermaPassword,
                                      Model model) {
        // Validazione password
        if (confermaPassword != null && !utente.getPassword().equals(confermaPassword)) {
            model.addAttribute("errore", "Le password non coincidono.");
            return "registrazione";
        }

        try {
            utenteService.registraUtente(
                utente.getNome(),
                utente.getCognome(),
                utente.getEmail(),
                utente.getPassword()
            );
        } catch (IllegalStateException e) {
            model.addAttribute("errore", "Email già registrata. Prova a fare il login!");
            return "registrazione";
        }

        return "redirect:/login?registrato=true";
    }
}