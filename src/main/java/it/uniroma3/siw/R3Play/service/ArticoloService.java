package it.uniroma3.siw.R3Play.service;

import it.uniroma3.siw.R3Play.model.Articolo;
import it.uniroma3.siw.R3Play.model.Utente;
import it.uniroma3.siw.R3Play.repository.ArticoloRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * Service layer per la gestione degli articoli.
 * Contiene tutta la logica di business: nessuna logica di accesso ai dati
 * deve risiedere nel Controller.
 *
 * Usa @Transactional(readOnly=true) per le letture e @Transactional per le scritture,
 * come da requisiti del corso (§7 - Gestione delle transazioni).
 */
@Service
public class ArticoloService {

    @Autowired
    private ArticoloRepository articoloRepository;

    @Value("${upload.dir:uploads}")
    private String uploadDir;

    // =============================================
    // OPERAZIONI DI LETTURA (readOnly = true)
    // =============================================

    /**
     * Recupera tutti gli articoli. Usa @EntityGraph per risolvere il
     * problema N+1 (venditore e foto vengono caricati in un'unica query).
     */
    @Transactional(readOnly = true)
    public List<Articolo> trovaTutti() {
        return articoloRepository.findAllByOrderByIdDesc();
    }

    /**
     * Ricerca articoli per testo (nome o descrizione).
     */
    @Transactional(readOnly = true)
    public List<Articolo> cercaPerTesto(String query) {
        return articoloRepository.searchConVenditore(query);
    }

    /**
     * Trova gli articoli di un singolo venditore con foto già caricate.
     */
    @Transactional(readOnly = true)
    public List<Articolo> trovaPerVenditore(Utente venditore) {
        return articoloRepository.findByVenditoreConFoto(venditore);
    }

    /**
     * Trova un articolo per ID con tutti i dettagli (venditore + foto).
     */
    @Transactional(readOnly = true)
    public Optional<Articolo> trovaPerId(Long id) {
        return articoloRepository.findWithDetailById(id);
    }

    /**
     * Conta gli articoli pubblicati da un venditore.
     */
    @Transactional(readOnly = true)
    public long contaPerVenditore(Utente venditore) {
        return articoloRepository.countByVenditore(venditore);
    }

    // =============================================
    // OPERAZIONI DI SCRITTURA (@Transactional)
    // =============================================

    /**
     * Crea un nuovo articolo con upload delle immagini.
     * Transazionale: se l'upload fallisce a metà, il DB non viene aggiornato.
     */
    @Transactional
    public Articolo salvaArticolo(Articolo articolo, Utente venditore, MultipartFile[] immagini) {
        articolo.setVenditore(venditore);
        gestisciUploadImmagini(articolo, immagini);
        return articoloRepository.save(articolo);
    }

    /**
     * Modifica un articolo esistente.
     * Lancia eccezione se l'utente non è il proprietario né un admin.
     */
    @Transactional
    public Articolo modificaArticolo(Long id, Articolo datiModificati, MultipartFile[] immagini, Utente utenteLoggato) {
        Articolo articolo = articoloRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Articolo non trovato: " + id));

        if (!isProprietarioOAdmin(articolo, utenteLoggato)) {
            throw new SecurityException("Non hai i permessi per modificare questo articolo.");
        }

        articolo.setNome(datiModificati.getNome());
        articolo.setDescrizione(datiModificati.getDescrizione());
        articolo.setPrezzo(datiModificati.getPrezzo());

        gestisciUploadImmagini(articolo, immagini);

        return articoloRepository.save(articolo);
    }

    /**
     * Elimina un articolo.
     * Verifica che l'utente sia il proprietario o un ADMIN.
     */
    @Transactional
    public void eliminaArticolo(Long id, Utente utenteLoggato) {
        Articolo articolo = articoloRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Articolo non trovato: " + id));

        if (!isProprietarioOAdmin(articolo, utenteLoggato)) {
            throw new SecurityException("Non hai i permessi per eliminare questo articolo.");
        }

        articoloRepository.delete(articolo);
    }

    // =============================================
    // METODI PRIVATI DI SUPPORTO
    // =============================================

    private boolean isProprietarioOAdmin(Articolo articolo, Utente utente) {
        if ("ROLE_ADMIN".equals(utente.getRuolo())) return true;
        return articolo.getVenditore() != null
                && utente.getEmail().equals(articolo.getVenditore().getEmail());
    }

    private void gestisciUploadImmagini(Articolo articolo, MultipartFile[] immagini) {
        if (immagini == null || immagini.length == 0) return;
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            for (MultipartFile immagine : immagini) {
                if (immagine != null && !immagine.isEmpty()) {
                    String fileName = System.currentTimeMillis() + "_" + immagine.getOriginalFilename();
                    Files.copy(immagine.getInputStream(), uploadPath.resolve(fileName));
                    String url = "/uploads/" + fileName;
                    if (articolo.getUrlFoto() == null) {
                        articolo.setUrlFoto(url);
                    }
                    articolo.addFotoUrl(url);
                }
            }
        } catch (IOException e) {
            // Rilancia come RuntimeException per far rollback della transazione
            throw new RuntimeException("Errore durante l'upload dell'immagine: " + e.getMessage(), e);
        }
    }
}
