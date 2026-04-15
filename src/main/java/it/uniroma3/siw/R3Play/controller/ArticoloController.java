package it.uniroma3.siw.R3Play.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import it.uniroma3.siw.R3Play.model.Articolo;
import it.uniroma3.siw.R3Play.model.Recensione;
import it.uniroma3.siw.R3Play.model.Utente;
import it.uniroma3.siw.R3Play.repository.ArticoloRepository;
import it.uniroma3.siw.R3Play.repository.RecensioneRepository;
import it.uniroma3.siw.R3Play.repository.UserRepository;

@Controller
public class ArticoloController {

    @Autowired
    private ArticoloRepository articoloRepository;

    @Autowired
    private RecensioneRepository recensioneRepository;

    @Autowired
    private UserRepository userRepository;

    // ==========================================
    // 1. VETRINA (Daniele - Read 1)
    // ==========================================
    @GetMapping("/")
    public String mostraVetrina(Model model) {
        Iterable<Articolo> tuttiGliArticoli = this.articoloRepository.findAll();
        model.addAttribute("articoli", tuttiGliArticoli);
        return "vetrina";
    }

    // ==========================================
    // 2. NUOVO ARTICOLO (Insert + Immagine + Venditore)
    // ==========================================
    @GetMapping("/articolo/nuovo")
    public String mostraFormNuovoArticolo(Model model) {
        model.addAttribute("articolo", new Articolo());
        return "nuovo-articolo"; 
    }

    @PostMapping("/articolo/nuovo")
    public String salvaNuovoArticolo(@ModelAttribute("articolo") Articolo articolo, 
                                     @RequestParam("fileImmagine") MultipartFile immagine,
                                     @AuthenticationPrincipal OAuth2User principal) { // <-- Prende chi è loggato
        
        // A. Gestione Utente Google (Venditore)
        if (principal != null) {
            String email = principal.getAttribute("email");
            Utente venditore = this.userRepository.findByEmail(email).orElse(null);
            
            // Se l'utente non esiste ancora nel DB, lo creiamo
            if (venditore == null) {
                venditore = new Utente();
                venditore.setEmail(email);
                venditore.setNome(principal.getAttribute("given_name"));
                venditore.setCognome(principal.getAttribute("family_name"));
                this.userRepository.save(venditore);
            }
            articolo.setVenditore(venditore); // Assegna il venditore!
        }

        // B. Gestione Immagine
        try {
            if (!immagine.isEmpty()) {
                String uploadDir = "src/main/resources/static/uploads/";
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                String fileName = immagine.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(immagine.getInputStream(), filePath);
                articolo.setUrlFoto("/uploads/" + fileName);
            }
        } catch (IOException e) {
            System.out.println("Errore salvataggio immagine: " + e.getMessage());
        }

        // C. Salvataggio finale
        this.articoloRepository.save(articolo);
        return "redirect:/"; 
    }

    // ==========================================
    // 3. MODIFICA ARTICOLO (Solo se sei il venditore!)
    // ==========================================
    @GetMapping("/articolo/modifica/{id}")
    public String mostraFormModifica(@PathVariable("id") Long id, Model model,
                                     @AuthenticationPrincipal OAuth2User principal) { // <-- Controllo Sicurezza
        
        Articolo articolo = this.articoloRepository.findById(id).orElse(null);
        
        if (articolo == null || principal == null) return "redirect:/";

        String emailLoggata = principal.getAttribute("email");
        
        // Verifica che chi tenta di modificare sia il vero venditore
        if (articolo.getVenditore() != null && emailLoggata.equals(articolo.getVenditore().getEmail())) {
            model.addAttribute("articolo", articolo);
            return "modifica-articolo";
        }
        
        return "redirect:/"; // Se furbetto, torna alla home
    }

    // Metodo per salvare effettivamente le modifiche dell'articolo
    @PostMapping("/articolo/modifica/{id}")
    public String salvaModificaArticolo(@PathVariable("id") Long id, 
                                        @ModelAttribute("articolo") Articolo articoloModificato,
                                        @AuthenticationPrincipal OAuth2User principal) {
        Articolo articoloEsistente = this.articoloRepository.findById(id).orElse(null);
        
        if (articoloEsistente != null && principal != null) {
            String emailLoggata = principal.getAttribute("email");
            if (articoloEsistente.getVenditore() != null && emailLoggata.equals(articoloEsistente.getVenditore().getEmail())) {
                // Aggiorniamo solo i campi testuali
                articoloEsistente.setNome(articoloModificato.getNome());
                articoloEsistente.setDescrizione(articoloModificato.getDescrizione());
                articoloEsistente.setPrezzo(articoloModificato.getPrezzo());
                this.articoloRepository.save(articoloEsistente);
            }
        }
        return "redirect:/";
    }

    // ==========================================
    // 4. DETTAGLIO ARTICOLO
    // ==========================================
    @GetMapping("/articolo/{id}")
    public String mostraDettaglioArticolo(@PathVariable("id") Long id, Model model) {
        Articolo articolo = this.articoloRepository.findById(id).orElse(null);
        if (articolo == null) return "redirect:/";

        model.addAttribute("articolo", articolo);
        model.addAttribute("recensione", new Recensione());
        return "dettaglio-articolo"; 
    }

    // ==========================================
    // 5. AGGIUNGI RECENSIONE
    // ==========================================
    @PostMapping("/articolo/{idArticolo}/recensione")
    public String aggiungiRecensione(@PathVariable("idArticolo") Long idArticolo, 
                                     @ModelAttribute("recensione") Recensione recensione,
                                     @AuthenticationPrincipal OAuth2User principal) {
        
        Articolo articolo = this.articoloRepository.findById(idArticolo).orElse(null);
        
        if (articolo != null && principal != null) {
            String email = principal.getAttribute("email");
            Utente autore = this.userRepository.findByEmail(email).orElse(null);
            
            if (autore == null) {
                autore = new Utente();
                autore.setEmail(email);
                autore.setNome(principal.getAttribute("given_name"));
                autore.setCognome(principal.getAttribute("family_name"));
                this.userRepository.save(autore);
            }

            recensione.setId(null); 
            recensione.setArticolo(articolo);
            recensione.setAutore(autore); 
            
            this.recensioneRepository.save(recensione);
        }
        return "redirect:/articolo/" + idArticolo;
    }

    // ==========================================
    // 6. ELIMINA RECENSIONE (Solo se sei l'autore!)
    // ==========================================
    @GetMapping("/recensione/elimina/{id}")
    public String eliminaRecensione(@PathVariable("id") Long id, 
                                    @AuthenticationPrincipal OAuth2User principal) { // <-- Controllo Sicurezza
        
        Recensione recensione = this.recensioneRepository.findById(id).orElse(null);
        
        if (recensione != null && principal != null) {
            String emailLoggata = principal.getAttribute("email");
            
            // Verifica che chi tenta di eliminare sia il vero autore
            if (recensione.getAutore() != null && emailLoggata.equals(recensione.getAutore().getEmail())) {
                Long articoloId = recensione.getArticolo().getId();
                this.recensioneRepository.delete(recensione);
                return "redirect:/articolo/" + articoloId;
            }
        }
        return "redirect:/";
    }
}