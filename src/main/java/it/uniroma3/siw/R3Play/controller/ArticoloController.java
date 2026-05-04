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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * CONTROLLER LAYER — Articoli
 *
 * Responsabilità: gestire le richieste HTTP e delegare la logica ai Service.
 * NON contiene logica di business (niente if/else su regole di dominio qui).
 * NON accede direttamente ai Repository.
 */
@Controller
public class ArticoloController {

    @Autowired
    private ArticoloService articoloService;

    @Autowired
    private RecensioneService recensioneService;

    @Autowired
    private UtenteService utenteService;

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
    // WELCOME / SPLASH
    // =========================================================
    @GetMapping("/")
    public String welcome(@AuthenticationPrincipal Object principal) {
        return utenteService.risolviUtente(principal) != null ? "redirect:/vetrina" : "welcome";
    }

    // =========================================================
    // VETRINA (lista articoli)
    // =========================================================
    @GetMapping("/vetrina")
    public String vetrina(@RequestParam(name = "q", required = false) String query,
                          Model model, @AuthenticationPrincipal Object principal) {

        List<Articolo> articoli = (query != null && !query.isBlank())
                ? articoloService.cercaPerTesto(query)
                : articoloService.trovaTutti();

        List<Utente> utentiTrovati = (query != null && !query.isBlank())
                ? utenteService.cercaPerTesto(query)
                : List.of();

        model.addAttribute("articoli", articoli);
        model.addAttribute("utentiTrovati", utentiTrovati);
        model.addAttribute("query", query);
        model.addAttribute("risultatiFiltro", query != null && !query.isBlank());

        Utente u = utenteService.risolviUtente(principal);
        if (u != null) {
            model.addAttribute("emailLoggata", u.getEmail());
            model.addAttribute("nomeLoggato", utenteService.buildNomeCompleto(u));
        }
        return "vetrina";
    }

    // =========================================================
    // DETTAGLIO ARTICOLO
    // =========================================================
    @GetMapping("/articolo/{id}")
    public String dettaglioArticolo(@PathVariable Long id, Model model,
                                    @AuthenticationPrincipal Object principal) {
        Articolo articolo = articoloService.trovaPerId(id).orElse(null);
        if (articolo == null) return "redirect:/vetrina";

        Utente u = utenteService.risolviUtente(principal);
        model.addAttribute("articolo", articolo);
        model.addAttribute("nomeLoggato", u != null ? utenteService.buildNomeCompleto(u) : "Ospite");
        model.addAttribute("emailLoggata", u != null ? u.getEmail() : null);
        return "dettaglio-articolo";
    }

    // =========================================================
    // NUOVO ARTICOLO
    // =========================================================
    @GetMapping("/articolo/nuovo")
    public String formNuovoArticolo(Model model) {
        model.addAttribute("articolo", new Articolo());
        return "nuovo-articolo";
    }

    @PostMapping("/articolo/nuovo")
    public String salvaNuovoArticolo(@ModelAttribute Articolo articolo,
                                     @RequestParam("fileImmagine") MultipartFile[] immagini,
                                     @AuthenticationPrincipal Object principal) {
        Utente venditore = utenteService.risolviUtente(principal);
        if (venditore == null) return "redirect:/login";
        articoloService.salvaArticolo(articolo, venditore, immagini);
        return "redirect:/armadio";
    }

    // =========================================================
    // MODIFICA ARTICOLO
    // =========================================================
    @GetMapping("/articolo/modifica/{id}")
    public String formModificaArticolo(@PathVariable Long id, Model model,
                                       @AuthenticationPrincipal Object principal) {
        Articolo articolo = articoloService.trovaPerId(id).orElse(null);
        Utente u = utenteService.risolviUtente(principal);
        if (articolo == null || u == null) return "redirect:/";
        model.addAttribute("articolo", articolo);
        return "modifica-articolo";
    }

    @PostMapping("/articolo/modifica/{id}")
    public String salvaModificaArticolo(@PathVariable Long id,
                                        @ModelAttribute Articolo datiModificati,
                                        @RequestParam("fileImmagine") MultipartFile[] immagini,
                                        @AuthenticationPrincipal Object principal) {
        Utente u = utenteService.risolviUtente(principal);
        if (u == null) return "redirect:/login";
        try {
            articoloService.modificaArticolo(id, datiModificati, immagini, u);
        } catch (SecurityException e) {
            return "redirect:/";
        }
        return "redirect:/armadio";
    }

    // =========================================================
    // ELIMINA ARTICOLO
    // =========================================================
    @GetMapping("/articolo/elimina/{id}")
    public String eliminaArticolo(@PathVariable Long id, @AuthenticationPrincipal Object principal) {
        Utente u = utenteService.risolviUtente(principal);
        if (u == null) return "redirect:/login";
        try {
            articoloService.eliminaArticolo(id, u);
        } catch (SecurityException e) {
            return "redirect:/";
        }
        return "redirect:/armadio";
    }
}