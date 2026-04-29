package it.uniroma3.siw.R3Play.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import it.uniroma3.siw.R3Play.model.Articolo;
import it.uniroma3.siw.R3Play.model.Recensione;
import it.uniroma3.siw.R3Play.model.Utente;
import it.uniroma3.siw.R3Play.repository.ArticoloRepository;
import it.uniroma3.siw.R3Play.repository.UserRepository;

import java.util.List;

@Controller
public class UtenteController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticoloRepository articoloRepository;

    @ModelAttribute("nomeLoggato")
    public String populateNomeLoggato(@AuthenticationPrincipal Object principal) {
        Utente utenteLoggato = getUtenteLoggato(principal);
        return utenteLoggato != null ? buildNomeCompleto(utenteLoggato) : null;
    }

    @ModelAttribute("emailLoggata")
    public String populateEmailLoggata(@AuthenticationPrincipal Object principal) {
        Utente utenteLoggato = getUtenteLoggato(principal);
        return utenteLoggato != null ? utenteLoggato.getEmail() : null;
    }

    // ==========================================
    // IL MIO ARMADIO (Area Personale)
    // ==========================================
    @GetMapping("/armadio")
    @org.springframework.transaction.annotation.Transactional
    public String mostraMioArmadio(Model model, @AuthenticationPrincipal Object principal) {

        Utente utenteLoggato = getUtenteLoggato(principal);
        if (utenteLoggato == null) return "redirect:/login";

        List<Articolo> mieiArticoli = this.articoloRepository.findByVenditore(utenteLoggato);

        // Nuovo calcolo basato sulle recensioni dell'utente (non dell'articolo)
        double mediaValutazioni = 0.0;
        int totaleRecensioni = 0;
        
        if (utenteLoggato.getRecensioniRicevute() != null && !utenteLoggato.getRecensioniRicevute().isEmpty()) {
            totaleRecensioni = utenteLoggato.getRecensioniRicevute().size();
            int sommaStelle = 0;
            for (Recensione r : utenteLoggato.getRecensioniRicevute()) {
                sommaStelle += r.getValutazione();
            }
            mediaValutazioni = Math.round(((double) sommaStelle / totaleRecensioni) * 10.0) / 10.0;
        }

        model.addAttribute("utente", utenteLoggato);
        model.addAttribute("mieiArticoli", mieiArticoli);
        model.addAttribute("mediaValutazioni", mediaValutazioni);
        model.addAttribute("recensioniRicevute", utenteLoggato.getRecensioniRicevute());

        return "armadio";
    }

    // ==========================================
    // MODIFICA PROFILO UTENTE
    // ==========================================
    @PostMapping("/utente/modifica")
    public String modificaProfiloUtente(@RequestParam("nome") String nome, 
                                        @RequestParam("cognome") String cognome, 
                                        @AuthenticationPrincipal Object principal) {
        
        Utente utente = getUtenteLoggato(principal);
        
        if (utente != null) {
            utente.setNome(nome);
            utente.setCognome(cognome);
            this.userRepository.save(utente);
        }
        
        return "redirect:/armadio";
    }

    // ==========================================
    // PROFILO PUBBLICO VENDITORE
    // ==========================================
    @GetMapping("/utente/{id}")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public String mostraProfiloVenditore(@PathVariable("id") Long id, Model model, @AuthenticationPrincipal Object principal) {
        
        Utente venditore = this.userRepository.findById(id).orElse(null);
        if (venditore == null) return "redirect:/";

        List<Articolo> articoliVenditore = this.articoloRepository.findByVenditore(venditore);

        double mediaValutazioni = 0.0;
        int totaleRecensioni = 0;
        if (venditore.getRecensioniRicevute() != null && !venditore.getRecensioniRicevute().isEmpty()) {
            totaleRecensioni = venditore.getRecensioniRicevute().size();
            int sommaStelle = 0;
            for (Recensione r : venditore.getRecensioniRicevute()) {
                sommaStelle += r.getValutazione();
            }
            mediaValutazioni = Math.round(((double) sommaStelle / totaleRecensioni) * 10.0) / 10.0;
        }

        model.addAttribute("venditore", venditore);
        model.addAttribute("articoliVenditore", articoliVenditore);
        model.addAttribute("mediaValutazioni", mediaValutazioni);
        model.addAttribute("totaleRecensioni", totaleRecensioni);
        model.addAttribute("nuovaRecensione", new Recensione()); 

        Utente visitatore = getUtenteLoggato(principal);
        if (visitatore != null) {
            model.addAttribute("utente", visitatore);
        }

        return "profilo-utente";
    }

    // Metodo helper per ottenere l'utente loggato
    private Utente getUtenteLoggato(Object principal) {
        if (principal == null) return null;

        String email = null;
        String nome = null;
        String cognome = null;
        String provider = null;

        if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
            org.springframework.security.oauth2.core.user.OAuth2User oauth = (org.springframework.security.oauth2.core.user.OAuth2User) principal;
            Object emailAttr = oauth.getAttribute("email");
            email = emailAttr != null ? emailAttr.toString() : null;
            provider = "GOOGLE";

            Object givenName = oauth.getAttribute("given_name");
            Object familyName = oauth.getAttribute("family_name");
            Object fullName = oauth.getAttribute("name");

            if (givenName != null) {
                nome = givenName.toString();
            }
            if (familyName != null) {
                cognome = familyName.toString();
            }
            if ((nome == null || nome.isBlank()) && fullName != null) {
                String[] parts = fullName.toString().trim().split(" ");
                if (parts.length > 0) {
                    nome = parts[0];
                    if (parts.length > 1) {
                        cognome = parts[parts.length - 1];
                    }
                }
            }
            if ((nome == null || nome.isBlank()) && email != null) {
                nome = email.split("@")[0];
            }
        } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            email = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        }

        if (email != null) {
            Utente utente = this.userRepository.findByEmail(email).orElse(null);
            if (utente == null && "GOOGLE".equals(provider)) {
                utente = new Utente();
                utente.setEmail(email);
                utente.setNome(capitalize(nome != null ? nome : "Utente"));
                utente.setCognome(capitalize(cognome != null ? cognome : "Google"));
                utente.setProvider(provider);
                this.userRepository.save(utente);
            } else if (utente != null && "GOOGLE".equals(provider)) {
                boolean dirty = false;
                if ((utente.getNome() == null || utente.getNome().isBlank()) && nome != null) {
                    utente.setNome(capitalize(nome));
                    dirty = true;
                }
                if ((utente.getCognome() == null || utente.getCognome().isBlank()) && cognome != null) {
                    utente.setCognome(capitalize(cognome));
                    dirty = true;
                }
                if (dirty) {
                    this.userRepository.save(utente);
                }
            }
            return utente;
        }
        return null;
    }

    private String buildNomeCompleto(Utente utente) {
        if (utente == null) return "Ospite";
        
        String nome = (utente.getNome() != null) ? utente.getNome() : "";
        String cognome = (utente.getCognome() != null) ? utente.getCognome() : "";
        
        if (nome.isBlank() && cognome.isBlank()) {
            return utente.getEmail().split("@")[0]; // Fallback sull'email
        }
        
        return (nome + " " + cognome).trim();
    }

    // Rendi il metodo capitalize resistente ai null
    private String capitalize(String text) {
        if (text == null || text.isBlank()) return "Utente"; // Default se manca
        
        String[] words = text.trim().split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            if (builder.length() > 0) builder.append(" ");
            builder.append(word.substring(0, 1).toUpperCase())
                   .append(word.substring(1).toLowerCase());
        }
        return builder.toString();
    }
}