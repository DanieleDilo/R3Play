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

    // === ATTRIBUTO GLOBALE: utente loggato disponibile in tutti i template ===
    @ModelAttribute("nomeLoggato")
    public String populateNomeLoggato(@AuthenticationPrincipal Object principal) {
        Utente u = risolviUtente(principal);
        return u != null ? buildNome(u) : null;
    }

    @ModelAttribute("emailLoggata")
    public String populateEmailLoggata(@AuthenticationPrincipal Object principal) {
        Utente u = risolviUtente(principal);
        return u != null ? u.getEmail() : null;
    }

    // =========================================================
    // WELCOME / SPLASH
    // =========================================================
    @GetMapping("/")
    public String welcome(@AuthenticationPrincipal Object principal) {
        return risolviUtente(principal) != null ? "redirect:/vetrina" : "welcome";
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

        model.addAttribute("articoli", articoli);
        model.addAttribute("query", query);
        model.addAttribute("risultatiFiltro", query != null && !query.isBlank());

        Utente u = risolviUtente(principal);
        if (u != null) {
            model.addAttribute("emailLoggata", u.getEmail());
            model.addAttribute("nomeLoggato", buildNome(u));
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

        Utente u = risolviUtente(principal);
        model.addAttribute("articolo", articolo);
        model.addAttribute("nomeLoggato", u != null ? buildNome(u) : "Ospite");
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
        Utente venditore = risolviUtente(principal);
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
        Utente u = risolviUtente(principal);
        if (articolo == null || u == null) return "redirect:/";
        model.addAttribute("articolo", articolo);
        return "modifica-articolo";
    }

    @PostMapping("/articolo/modifica/{id}")
    public String salvaModificaArticolo(@PathVariable Long id,
                                        @ModelAttribute Articolo datiModificati,
                                        @RequestParam("fileImmagine") MultipartFile[] immagini,
                                        @AuthenticationPrincipal Object principal) {
        Utente u = risolviUtente(principal);
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
        Utente u = risolviUtente(principal);
        if (u == null) return "redirect:/login";
        try {
            articoloService.eliminaArticolo(id, u);
        } catch (SecurityException e) {
            return "redirect:/";
        }
        return "redirect:/armadio";
    }

    // =========================================================
    // ARMADIO (profilo privato dell'utente)
    // =========================================================
    @GetMapping("/armadio")
    public String armadio(Model model, @AuthenticationPrincipal Object principal) {
        Utente u = risolviUtente(principal);
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

        Utente visitatore = risolviUtente(principal);
        if (visitatore != null) model.addAttribute("utente", visitatore);

        return "profilo-utente";
    }

    // =========================================================
    // MODIFICA PROFILO
    // =========================================================
    @PostMapping("/utente/modifica")
    public String modificaProfilo(@RequestParam String nome, @RequestParam String cognome,
                                  @AuthenticationPrincipal Object principal) {
        Utente u = risolviUtente(principal);
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
        Utente autore = risolviUtente(principal);
        if (autore == null) return "redirect:/login";
        try {
            recensioneService.aggiungiRecensione(id, recensione, autore);
        } catch (IllegalStateException e) {
            // Già recensito o auto-recensione: ignoriamo silenziosamente
        }
        return "redirect:/utente/" + id;
    }

    @GetMapping("/recensione/elimina/{id}")
    public String eliminaRecensione(@PathVariable Long id, @AuthenticationPrincipal Object principal) {
        Utente u = risolviUtente(principal);
        if (u == null) return "redirect:/login";
        try {
            Long idVenditore = recensioneService.eliminaRecensione(id, u);
            return "redirect:/utente/" + idVenditore;
        } catch (SecurityException e) {
            return "redirect:/";
        }
    }

    // =========================================================
    // METODO HELPER: risolve l'utente dal principal (LOCAL o GOOGLE)
    // =========================================================
    private Utente risolviUtente(Object principal) {
        if (principal == null) return null;

        if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User oauth) {
            Object emailAttr = oauth.getAttribute("email");
            if (emailAttr == null) return null;
            String email = emailAttr.toString();
            String nome = getAttr(oauth, "given_name", getAttr(oauth, "name", email.split("@")[0]));
            String cognome = getAttr(oauth, "family_name", "");
            return utenteService.recuperaOCreaUtenteGoogle(email, nome, cognome);
        }

        if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            return utenteService.trovaPerEmail(ud.getUsername()).orElse(null);
        }

        return null;
    }

    private String getAttr(org.springframework.security.oauth2.core.user.OAuth2User oauth,
                            String key, String fallback) {
        Object val = oauth.getAttribute(key);
        return (val != null && !val.toString().isBlank()) ? val.toString() : fallback;
    }

    private String buildNome(Utente u) {
        String n = u.getNome() != null ? u.getNome() : "";
        String c = u.getCognome() != null ? u.getCognome() : "";
        return (n + " " + c).trim().isBlank() ? u.getEmail().split("@")[0] : (n + " " + c).trim();
    }
}