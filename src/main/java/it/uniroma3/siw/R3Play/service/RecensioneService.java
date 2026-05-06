package it.uniroma3.siw.R3Play.service;

import it.uniroma3.siw.R3Play.model.Recensione;
import it.uniroma3.siw.R3Play.model.Utente;
import it.uniroma3.siw.R3Play.repository.RecensioneRepository;
import it.uniroma3.siw.R3Play.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RecensioneService {

    @Autowired
    private RecensioneRepository recensioneRepository;

    @Autowired
    private UserRepository userRepository;

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

        recensione.setId(null);
        recensione.setAutore(autore);
        recensione.setDestinatario(destinatario);

        return recensioneRepository.save(recensione);
    }

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
