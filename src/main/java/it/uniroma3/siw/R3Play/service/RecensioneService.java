package it.uniroma3.siw.R3Play.service;

import it.uniroma3.siw.R3Play.model.Recensione;
import it.uniroma3.siw.R3Play.model.Utente;
import it.uniroma3.siw.R3Play.repository.RecensioneRepository;
import it.uniroma3.siw.R3Play.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer per la gestione delle recensioni.
 * Implementa le regole di business:
 * - Un utente non può recensire se stesso
 * - Un utente può avere una sola recensione per venditore
 * - Solo l'autore o un ADMIN può eliminare una recensione
 */
@Service
public class RecensioneService {

    @Autowired
    private RecensioneRepository recensioneRepository;

    @Autowired
    private UserRepository userRepository;

    // =============================================
    // OPERAZIONI DI LETTURA
    // =============================================

    @Transactional(readOnly = true)
    public List<Recensione> trovaRecensioniRicevute(Utente venditore) {
        return recensioneRepository.findByDestinatarioOrderByIdDesc(venditore);
    }

    @Transactional(readOnly = true)
    public double calcolaMediaValutazione(Utente venditore) {
        return recensioneRepository.calcolaMediaValutazione(venditore)
                .map(avg -> Math.round(avg * 10.0) / 10.0)
                .orElse(0.0);
    }

    @Transactional(readOnly = true)
    public long contaRecensioniRicevute(Utente venditore) {
        return recensioneRepository.findByDestinatario(venditore).size();
    }

    // =============================================
    // OPERAZIONI DI SCRITTURA
    // =============================================

    /**
     * Aggiunge una recensione a un venditore.
     * Regole di business:
     * 1. L'autore non può recensire se stesso
     * 2. Non si può recensire due volte lo stesso venditore
     */
    @Transactional
    public Recensione aggiungiRecensione(Long idVenditore, Recensione recensione, Utente autore) {
        Utente destinatario = userRepository.findById(idVenditore)
                .orElseThrow(() -> new IllegalArgumentException("Venditore non trovato: " + idVenditore));

        if (destinatario.getId().equals(autore.getId())) {
            throw new IllegalStateException("Non puoi recensire te stesso.");
        }

        if (recensioneRepository.existsByAutoreAndDestinatario(autore, destinatario)) {
            throw new IllegalStateException("Hai già recensito questo venditore.");
        }

        recensione.setId(null); // Forza creazione nuova
        recensione.setAutore(autore);
        recensione.setDestinatario(destinatario);

        return recensioneRepository.save(recensione);
    }

    /**
     * Elimina una recensione.
     * Solo l'autore o un ADMIN può eliminarla.
     * Restituisce l'ID del destinatario per il redirect.
     */
    @Transactional
    public Long eliminaRecensione(Long id, Utente utenteLoggato) {
        Recensione recensione = recensioneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recensione non trovata: " + id));

        boolean isAdmin = "ROLE_ADMIN".equals(utenteLoggato.getRuolo());
        boolean isAutore = recensione.getAutore() != null
                && utenteLoggato.getEmail().equals(recensione.getAutore().getEmail());

        if (!isAdmin && !isAutore) {
            throw new SecurityException("Non hai i permessi per eliminare questa recensione.");
        }

        Long idVenditore = recensione.getDestinatario().getId();
        recensioneRepository.delete(recensione);
        return idVenditore;
    }
}
