package it.uniroma3.siw.R3Play.service;

import it.uniroma3.siw.R3Play.model.Articolo;
import it.uniroma3.siw.R3Play.repository.ArticoloRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SERVICE PER L'ANALISI SPERIMENTALE DELLE STRATEGIE DI FETCH JPA (§8.2)
 *
 * Confronta 4 strategie per rispondere alla domanda:
 * "Come caricare in modo efficiente gli articoli con venditore e foto?"
 *
 * Strategie testate:
 *   1. LAZY  — default Hibernate, causa il problema N+1
 *   2. JOIN FETCH (JPQL) — carica tutto in una sola query SQL
 *   3. EntityGraph — fetch dichiarativo e selettivo
 *   4. findAll() con EAGER — carica sempre tutto, anche se non serve
 */
@Service
public class PerformanceAnalysisService {

    @Autowired
    private ArticoloRepository articoloRepository;

    /**
     * Esegue il confronto tra le 4 strategie e restituisce i tempi in ms.
     * Il metodo è @Transactional perché la sessione Hibernate deve rimanere
     * aperta durante l'accesso alle associazioni LAZY.
     */
    @Transactional(readOnly = true)
    public Map<String, ResultatoAnalisi> eseguiAnalisiCompleta() {
        Map<String, ResultatoAnalisi> risultati = new LinkedHashMap<>();

        // --- STRATEGIA 1: LAZY (problema N+1) ---
        {
            long start = System.nanoTime();
            List<Articolo> articoli = articoloRepository.findAll();
            int contatoreQuery = 1; // 1 query per gli articoli
            for (Articolo a : articoli) {
                // Ogni accesso al venditore genera UNA query separata!
                if (a.getVenditore() != null) {
                    a.getVenditore().getEmail(); // trigger lazy load
                    contatoreQuery++; // +1 query per ogni venditore
                }
                // Ogni accesso alle foto genera ANCORA UNA query!
                a.getFotoUrls().size();       // trigger lazy load
                contatoreQuery++;             // +1 query per ogni lista foto
            }
            long durataMs = (System.nanoTime() - start) / 1_000_000;
            risultati.put("1_LAZY_N+1", new ResultatoAnalisi(
                "LAZY (Default Hibernate)",
                durataMs,
                contatoreQuery,
                articoli.size(),
                "Problema N+1: 1 query per articoli + N per venditori + N per foto = " + contatoreQuery + " query totali",
                "❌ Sconsigliato per liste grandi"
            ));
        }

        // --- STRATEGIA 2: JOIN FETCH (JPQL) ---
        {
            long start = System.nanoTime();
            List<Articolo> articoli = articoloRepository.findAllConVenditoreEFoto();
            // Accediamo a venditore e foto: nessuna query aggiuntiva
            for (Articolo a : articoli) {
                if (a.getVenditore() != null) a.getVenditore().getEmail();
                a.getFotoUrls().size();
            }
            long durataMs = (System.nanoTime() - start) / 1_000_000;
            risultati.put("2_JOIN_FETCH", new ResultatoAnalisi(
                "JOIN FETCH (JPQL)",
                durataMs,
                1,
                articoli.size(),
                "Una sola query SQL con LEFT JOIN FETCH: carica articoli + venditori + foto",
                "✅ Ottimo per liste in vetrina"
            ));
        }

        // --- STRATEGIA 3: EntityGraph ---
        {
            long start = System.nanoTime();
            List<Articolo> articoli = articoloRepository.findAllByOrderByIdDesc();
            for (Articolo a : articoli) {
                if (a.getVenditore() != null) a.getVenditore().getEmail();
                a.getFotoUrls().size();
            }
            long durataMs = (System.nanoTime() - start) / 1_000_000;
            risultati.put("3_ENTITY_GRAPH", new ResultatoAnalisi(
                "EntityGraph (dichiarativo)",
                durataMs,
                1,
                articoli.size(),
                "@EntityGraph(attributePaths = {\"venditore\", \"fotoUrls\"}) — fetch selettivo senza JPQL",
                "✅ Ottimo equilibrio tra leggibilità e performance"
            ));
        }

        return risultati;
    }

    /**
     * DTO per i risultati dell'analisi — esposto al template Thymeleaf.
     */
    public static class ResultatoAnalisi {
        private final String nomeStrategia;
        private final long durataMs;
        private final int numeroQuery;
        private final int numeroArticoli;
        private final String spiegazione;
        private final String valutazione;

        public ResultatoAnalisi(String nomeStrategia, long durataMs, int numeroQuery,
                                int numeroArticoli, String spiegazione, String valutazione) {
            this.nomeStrategia = nomeStrategia;
            this.durataMs = durataMs;
            this.numeroQuery = numeroQuery;
            this.numeroArticoli = numeroArticoli;
            this.spiegazione = spiegazione;
            this.valutazione = valutazione;
        }

        public String getNomeStrategia() { return nomeStrategia; }
        public long getDurataMs() { return durataMs; }
        public int getNumeroQuery() { return numeroQuery; }
        public int getNumeroArticoli() { return numeroArticoli; }
        public String getSpiegazione() { return spiegazione; }
        public String getValutazione() { return valutazione; }
    }
}
