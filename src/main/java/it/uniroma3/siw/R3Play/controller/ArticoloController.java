package it.uniroma3.siw.R3Play.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
import it.uniroma3.siw.R3Play.repository.ArticoloRepository;

@Controller
public class ArticoloController {

    @Autowired
    private ArticoloRepository articoloRepository;

    // Mostra la pagina principale con tutti gli articoli
    @GetMapping("/")
    public String mostraVetrina(Model model) {
        // 1. Chiede al database TUTTI gli articoli salvati
        Iterable<Articolo> tuttiGliArticoli = this.articoloRepository.findAll();
        
        // 2. Li "impacchetta" e li invia alla pagina HTML con il nome "articoli"
        model.addAttribute("articoli", tuttiGliArticoli);
        
        // 3. Dice a Spring di aprire il file "vetrina.html"
        return "vetrina";
    }

    @GetMapping("/articolo/nuovo")
    public String mostraFormNuovoArticolo(Model model) {
        model.addAttribute("articolo", new Articolo());
        return "nuovo-articolo"; 
    }

    // Aggiunto @RequestParam per catturare il file dal form
    @PostMapping("/articolo/nuovo")
    public String salvaNuovoArticolo(@ModelAttribute("articolo") Articolo articolo, 
                                     @RequestParam("fileImmagine") MultipartFile immagine) {
        try {
            // Se l'utente ha caricato un'immagine
            if (!immagine.isEmpty()) {
                // 1. Creiamo una cartella "uploads" dentro la cartella static
                String uploadDir = "src/main/resources/static/uploads/";
                Path uploadPath = Paths.get(uploadDir);
                
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // 2. Salviamo il file fisicamente nella cartella
                String fileName = immagine.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(immagine.getInputStream(), filePath);

                // 3. Diciamo all'Articolo qual è il percorso per ritrovare l'immagine
                articolo.setUrlFoto("/uploads/" + fileName);
            }
        } catch (IOException e) {
            System.out.println("Errore durante il salvataggio dell'immagine!");
            e.printStackTrace();
        }

        // Salviamo l'articolo nel database (ora avrà il percorso dell'immagine corretto)
        this.articoloRepository.save(articolo);
        
        return "redirect:/"; 
    }

    // 1. Quando l'utente clicca su "Modifica", recupera l'articolo dal DB e mostra il form precompilato
    @GetMapping("/articolo/modifica/{id}")
    public String mostraFormModifica(@PathVariable("id") Long id, Model model) {
        // Cerca l'articolo per ID. Se lo trova lo passa al modello.
        Articolo articoloDaModificare = this.articoloRepository.findById(id).get();
        model.addAttribute("articolo", articoloDaModificare);
        return "modifica-articolo";
    }

    // 2. Quando l'utente salva le modifiche
    @PostMapping("/articolo/modifica/{id}")
    public String salvaModificaArticolo(@ModelAttribute("articolo") Articolo articolo) {
        // Spring Boot è intelligente: se l'oggetto "articolo" ha già un ID, 
        // fa in automatico un UPDATE invece di un INSERT!
        this.articoloRepository.save(articolo);
        return "redirect:/";
    }
}