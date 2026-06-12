package it.uniroma3.siw.R3Play.controller;

import it.uniroma3.siw.R3Play.model.Recensione;
import it.uniroma3.siw.R3Play.model.Utente;
import it.uniroma3.siw.R3Play.service.RecensioneService;
import it.uniroma3.siw.R3Play.service.UtenteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recensioni")
public class RecensioneApiController {

    @Autowired
    private RecensioneService recensioneService;

    @Autowired
    private UtenteService utenteService;

    @GetMapping("/{idVenditore}")
    public ResponseEntity<List<Map<String, Object>>> getRecensioni(@PathVariable Long idVenditore) {
        Utente venditore = utenteService.trovaPerId(idVenditore).orElse(null);
        if (venditore == null) return ResponseEntity.notFound().build();

        List<Recensione> recensioni = recensioneService.trovaRecensioniRicevute(venditore);
        List<Map<String, Object>> dto = recensioni.stream()
                .map(r -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id",          r.getId());
                    m.put("testo",       r.getTesto() != null ? r.getTesto() : "");
                    m.put("valutazione", r.getValutazione());
                    m.put("autore",      r.getAutore() != null
                                           ? r.getAutore().getNome() + " " + r.getAutore().getCognome()
                                           : "Utente");
                    m.put("autoreEmail", r.getAutore() != null ? r.getAutore().getEmail() : "");
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{idVenditore}")
    public ResponseEntity<Map<String, Object>> aggiungiRecensione(
            @PathVariable Long idVenditore,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal Object principal) {

        Utente autore = utenteService.risolviUtente(principal);
        if (autore == null) return ResponseEntity.status(401).build();

        try {
            Recensione nuova = new Recensione();
            nuova.setTesto((String) body.get("testo"));
            Object valObj = body.get("valutazione");
            nuova.setValutazione(valObj instanceof Integer ? (Integer) valObj : ((Number) valObj).intValue());

            Recensione salvata = recensioneService.aggiungiRecensione(idVenditore, nuova, autore);

            Map<String, Object> resp = new HashMap<>();
            resp.put("id",          salvata.getId());
            resp.put("testo",       salvata.getTesto());
            resp.put("valutazione", salvata.getValutazione());
            resp.put("autore",      autore.getNome() + " " + autore.getCognome());
            resp.put("autoreEmail", autore.getEmail());
            return ResponseEntity.ok(resp);

        } catch (IllegalStateException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("errore", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminaRecensione(
            @PathVariable Long id,
            @AuthenticationPrincipal Object principal) {

        Utente u = utenteService.risolviUtente(principal);
        if (u == null) return ResponseEntity.status(401).build();

        try {
            recensioneService.eliminaRecensione(id, u);
            Map<String, Object> ok = new HashMap<>();
            ok.put("eliminato", true);
            return ResponseEntity.ok(ok);
        } catch (SecurityException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("errore", "Non autorizzato");
            return ResponseEntity.status(403).body(err);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
