package it.uniroma3.siw.R3Play.controller;

import it.uniroma3.siw.R3Play.model.Articolo;
import it.uniroma3.siw.R3Play.model.Recensione;
import it.uniroma3.siw.R3Play.model.Utente;
import it.uniroma3.siw.R3Play.service.ArticoloService;
import it.uniroma3.siw.R3Play.service.RecensioneService;
import it.uniroma3.siw.R3Play.service.UtenteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class UtenteController {

    @Autowired
    private UtenteService utenteService;

    @Autowired
    private ArticoloService articoloService;

    @Autowired
    private RecensioneService recensioneService;

    @ModelAttribute("nomeLoggato")
    public String populateNomeLoggato(@AuthenticationPrincipal Object principal) {
        Utente u = utenteService.risolviUtente(principal);
        return u != null ? utenteService.buildNomeCompleto(u) : null;
    }

    @ModelAttribute("emailLoggata")
    public String populateEmailLoggata(@AuthenticationPrincipal Object principal) {
        Utente u = utenteService.risolviUtente(principal);
        return u != null ? u.getEmail() : null;
    }

    // =========================================================
    // ARMADIO (profilo privato dell'utente)
    // =========================================================
    @GetMapping("/armadio")
    public String armadio(Model model, @AuthenticationPrincipal Object principal) {
        Utente u = utenteService.risolviUtente(principal);
        if (u == null) return "redirect:/login";

        List<Articolo> mieiArticoli = articoloService.trovaPerVenditore(u);
        double media = recensioneService.calcolaMediaValutazione(u);
        long totaleRecensioni = recensioneService.contaRecensioniRicevute(u);

        model.addAttribute("utente", u);
        model.addAttribute("mieiArticoli", mieiArticoli);
        model.addAttribute("mediaValutazioni", media);
        model.addAttribute("totaleRecensioni", totaleRecensioni);
        model.addAttribute("recensioniRicevute", recensioneService.trovaRecensioniRicevute(u));
        return "armadio";
    }

    // =========================================================
    // PROFILO VENDITORE (pubblico)
    // =========================================================
    @GetMapping("/utente/{id}")
    public String profiloVenditore(@PathVariable Long id, Model model,
                                   @AuthenticationPrincipal Object principal) {
        Utente venditore = utenteService.trovaPerId(id).orElse(null);
        if (venditore == null) return "redirect:/";

        List<Articolo> articoli = articoloService.trovaPerVenditore(venditore);
        double media = recensioneService.calcolaMediaValutazione(venditore);
        long totale = recensioneService.contaRecensioniRicevute(venditore);

        model.addAttribute("venditore", venditore);
        model.addAttribute("articoliVenditore", articoli);
        model.addAttribute("mediaValutazioni", media);
        model.addAttribute("totaleRecensioni", totale);
        model.addAttribute("recensioni", recensioneService.trovaRecensioniRicevute(venditore));
        model.addAttribute("nuovaRecensione", new Recensione());

        Utente visitatore = utenteService.risolviUtente(principal);
        if (visitatore != null) model.addAttribute("utente", visitatore);

        return "profilo-utente";
    }

    // =========================================================
    // MODIFICA PROFILO
    // =========================================================
    @PostMapping("/utente/modifica")
    public String modificaProfilo(@RequestParam String nome, @RequestParam String cognome,
                                  @AuthenticationPrincipal Object principal) {
        Utente u = utenteService.risolviUtente(principal);
        if (u != null) utenteService.aggiornaProfilo(u, nome, cognome);
        return "redirect:/armadio";
    }

    // =========================================================
    // RECENSIONI
    // =========================================================
    @PostMapping("/utente/{id}/recensione")
    public String aggiungiRecensione(@PathVariable Long id,
                                     @ModelAttribute("nuovaRecensione") Recensione recensione,
                                     @AuthenticationPrincipal Object principal) {
        Utente autore = utenteService.risolviUtente(principal);
        if (autore == null) return "redirect:/login";
        try {
            recensioneService.aggiungiRecensione(id, recensione, autore);
        } catch (IllegalStateException e) {
            // Auto-recensione: ignoriamo silenziosamente
        }
        return "redirect:/utente/" + id;
    }

    @PostMapping("/recensione/elimina/{id}")
    public String eliminaRecensione(@PathVariable Long id, @AuthenticationPrincipal Object principal) {
        Utente u = utenteService.risolviUtente(principal);
        if (u == null) return "redirect:/login";
        try {
            Long idVenditore = recensioneService.eliminaRecensione(id, u);
            return "redirect:/utente/" + idVenditore;
        } catch (SecurityException e) {
            return "redirect:/";
        }
    }

}
