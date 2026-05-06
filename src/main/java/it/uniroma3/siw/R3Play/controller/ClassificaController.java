package it.uniroma3.siw.R3Play.controller;

import it.uniroma3.siw.R3Play.model.Utente;
import it.uniroma3.siw.R3Play.service.RecensioneService;
import it.uniroma3.siw.R3Play.service.UtenteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ClassificaController {

    @Autowired
    private UtenteService utenteService;

    @Autowired
    private RecensioneService recensioneService;

    @GetMapping("/classifica")
    public String classifica(Model model, @AuthenticationPrincipal Object principal) {
        List<Object[]> risultati = utenteService.ottieniClassifica();
        
        List<Utente> classifica = new ArrayList<>();
        Map<Long, Double> medie = new HashMap<>();
        Map<Long, Long> totali = new HashMap<>();
        
        for (Object[] r : risultati) {
            Utente u = (Utente) r[0];
            Double media = (Double) r[1];
            Long totale = (Long) r[2];
            
            classifica.add(u);
            medie.put(u.getId(), media != null ? Math.round(media * 10.0) / 10.0 : 0.0);
            totali.put(u.getId(), totale);
        }

        model.addAttribute("classifica", classifica);
        model.addAttribute("medie", medie);
        model.addAttribute("totali", totali);

        Utente u = utenteService.risolviUtente(principal);
        if (u != null) {
            model.addAttribute("nomeLoggato", utenteService.buildNomeCompleto(u));
            model.addAttribute("emailLoggata", u.getEmail());
        }

        return "classifica";
    }
}
