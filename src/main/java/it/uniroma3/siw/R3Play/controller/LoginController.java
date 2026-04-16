package it.uniroma3.siw.R3Play.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import it.uniroma3.siw.R3Play.model.Utente;
import it.uniroma3.siw.R3Play.repository.UserRepository;

@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Ci serve per criptare la password!

    // Mostra la pagina di Login personalizzata
    @GetMapping("/login")
    public String mostraPaginaLogin() {
        return "login";
    }

    // Mostra la pagina per registrarsi (creare un nuovo account)
    @GetMapping("/registrati")
    public String mostraFormRegistrazione(Model model) {
        model.addAttribute("utente", new Utente());
        return "registrazione";
    }

    // Riceve i dati dal form di registrazione
    @PostMapping("/registrati")
    public String registraNuovoUtente(@ModelAttribute("utente") Utente utente, Model model) {
        
        // 1. Controllo se l'email esiste già
        if (this.userRepository.findByEmail(utente.getEmail()).isPresent()) {
            model.addAttribute("errore", "Questa email è già registrata. Prova a fare il login!");
            return "registrazione";
        }

        // 2. Criptiamo la password prima di salvarla!
        String passwordCriptata = passwordEncoder.encode(utente.getPassword());
        utente.setPassword(passwordCriptata);
        
        // 3. Salviamo chi è il "provider" (per distinguerli da Google)
        utente.setProvider("LOCAL");

        // 4. Salvo nel DB
        this.userRepository.save(utente);

        // 5. Rimando alla pagina di login con un messaggio di successo
        return "redirect:/login?registrato=true";
    }
}