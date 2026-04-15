package it.uniroma3.siw.R3Play.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
// Import fondamentali per l'autenticazione OAuth2
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

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
    // 2. NUOVO ARTICOLO (Daniele - Insert)
    // ==========================================
    @GetMapping("/articolo/nuovo")
    public String mostraFormNuovoArticolo(Model model) {
        model.addAttribute("articolo", new Articolo());
        return "nuovo-articolo"; 
    }

    @PostMapping("/articolo/nuovo")
    public String salvaNuovoArticolo(@ModelAttribute("articolo") Articolo articolo, 
                                     @RequestParam("fileImmagine") MultipartFile immagine) {
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
            System.out.println("Errore durante il salvataggio dell'immagine!");
            e.printStackTrace();
        }
        this.articoloRepository.save(articolo);
        return "redirect:/"; 
    }

    // ==========================================
    // 3. MODIFICA ARTICOLO (Daniele - Update)
    // ==========================================
    @GetMapping("/articolo/modifica/{id}")
    public String mostraFormModifica(@PathVariable("id") Long id, Model model) {
        Articolo articoloDaModificare = this.articoloRepository.findById(id).orElse(null);
        model.addAttribute("articolo", articoloDaModificare);
        return "modifica-articolo";
    }

    @PostMapping("/articolo/modifica/{id}")
    public String salvaModificaArticolo(@ModelAttribute("articolo") Articolo articolo) {
        this.articoloRepository.save(articolo);
        return "redirect:/";
    }

    // ==========================================
    // 4. DETTAGLIO ARTICOLO (Mattia - Read 2)
    // ==========================================
    @GetMapping("/articolo/{id}")
    public String mostraDettaglioArticolo(@PathVariable("id") Long id, Model model) {
        Articolo articolo = this.articoloRepository.findById(id).orElse(null);
        
        if (articolo == null) {
            return "redirect:/";
        }

        model.addAttribute("articolo", articolo);
        // Prepariamo l'oggetto per il form (Caso 5)
        model.addAttribute("recensione", new Recensione());
        
        return "dettaglio-articolo"; 
    }

  // ==========================================
    // 5. AGGIUNGI RECENSIONE (Collegata a Google!)
    // ==========================================
    @PostMapping("/articolo/{idArticolo}/recensione")
    public String aggiungiRecensione(@PathVariable("idArticolo") Long idArticolo, 
                                     @ModelAttribute("recensione") Recensione recensione,
                                     @AuthenticationPrincipal OAuth2User principal) {
        
        Articolo articolo = this.articoloRepository.findById(idArticolo).orElse(null);
        
        // Se l'articolo esiste e l'utente è loggato con Google
        if (articolo != null && principal != null) {
            
            // Prendiamo la mail di Google
            String email = principal.getAttribute("email");
            String nome = principal.getAttribute("given_name");
            String cognome = principal.getAttribute("family_name");

            // Cerchiamo l'utente usando il TUO UserRepository
            Utente autore = this.userRepository.findByEmail(email).orElse(null);
            
            // Se è la prima volta che entra, lo creiamo!
            if (autore == null) {
                autore = new Utente();
                autore.setEmail(email);
                autore.setNome(nome);
                autore.setCognome(cognome);
                this.userRepository.save(autore);
            }

            // Assegniamo il vero autore alla recensione
            recensione.setId(null); 
            recensione.setArticolo(articolo);
            recensione.setAutore(autore); 
            
            try {
                this.recensioneRepository.save(recensione);
            } catch (Exception e) {
                System.out.println("ERRORE SALVATAGGIO RECENSIONE: " + e.getMessage());
                return "redirect:/articolo/" + idArticolo + "?error"; 
            }
        }
        
        return "redirect:/articolo/" + idArticolo;
    }
    // ==========================================
    // 6. ELIMINA RECENSIONE (Mattia - Delete)
    // ==========================================
    @GetMapping("/recensione/elimina/{id}")
    public String eliminaRecensione(@PathVariable("id") Long id) {
        // 1. Cerchiamo la recensione per sapere a quale articolo apparteneva
        Recensione recensione = this.recensioneRepository.findById(id).orElse(null);
        
        if (recensione != null) {
            Long articoloId = recensione.getArticolo().getId();
            // 2. La eliminiamo dal database
            this.recensioneRepository.delete(recensione);
            // 3. Torniamo alla pagina di dettaglio dell'articolo
            return "redirect:/articolo/" + articoloId;
        }
        
        return "redirect:/";
    }
}