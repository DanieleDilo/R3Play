package it.uniroma3.siw.R3Play.controller;

import it.uniroma3.siw.R3Play.service.PerformanceAnalysisService;
import it.uniroma3.siw.R3Play.service.PerformanceAnalysisService.ResultatoAnalisi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * CONTROLLER LAYER — Analisi delle Prestazioni (§8.2)
 *
 * Accessibile solo agli amministratori.
 * Esegue e mostra il confronto tra le strategie di fetch JPA:
 * LAZY, JOIN FETCH, EntityGraph.
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class PerformanceController {

    @Autowired
    private PerformanceAnalysisService performanceAnalysisService;

    /**
     * Mostra il pannello di analisi delle fetch strategy.
     * Richiede ruolo ADMIN.
     */
    @GetMapping("/performance")
    public String mostraAnalisiPerformance(Model model) {
        Map<String, ResultatoAnalisi> risultati = performanceAnalysisService.eseguiAnalisiCompleta();
        model.addAttribute("risultati", risultati);
        return "admin/performance";
    }
}
