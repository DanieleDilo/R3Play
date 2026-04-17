package it.uniroma3.siw.R3Play.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import java.util.List;

import it.uniroma3.siw.R3Play.model.Articolo;
import it.uniroma3.siw.R3Play.model.Recensione;
import it.uniroma3.siw.R3Play.model.Utente;
import it.uniroma3.siw.R3Play.repository.ArticoloRepository;
import it.uniroma3.siw.R3Play.repository.RecensioneRepository;
import it.uniroma3.siw.R3Play.repository.UserRepository;

@SuppressWarnings("null")
@Controller
public class ArticoloController {

    @Autowired
    private ArticoloRepository articoloRepository;

    @Autowired
    private RecensioneRepository recensioneRepository;

    @Autowired
    private UserRepository userRepository;

    // ==========================================
    // 1. VETRINA
    // ==========================================
    @GetMapping("/")
    public String mostraVetrina(Model model, @AuthenticationPrincipal Object principal) {
        Iterable<Articolo> tuttiGliArticoli = this.articoloRepository.findAll();
        model.addAttribute("articoli", tuttiGliArticoli);

        Utente utenteLoggato = getUtenteLoggato(principal);
        if (utenteLoggato != null) {
            model.addAttribute("emailLoggata", utenteLoggato.getEmail());
        }

        return "vetrina";
    }

    // ==========================================
    // 2. NUOVO ARTICOLO
    // ==========================================
    @GetMapping("/articolo/nuovo")
    public String mostraFormNuovoArticolo(Model model) {
        model.addAttribute("articolo", new Articolo());
        return "nuovo-articolo";
    }

    @PostMapping("/articolo/nuovo")
    public String salvaNuovoArticolo(@ModelAttribute("articolo") Articolo articolo,
            @RequestParam("fileImmagine") MultipartFile immagine,
            @AuthenticationPrincipal Object principal) {

        Utente venditore = getUtenteLoggato(principal);
        if (venditore != null) {
            articolo.setVenditore(venditore);
        }

       // Gestione Immagine con NOME UNICO
        try {
            if (!immagine.isEmpty()) {
                String uploadDir = "src/main/resources/static/uploads/";
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

                // --- FIX: Rendiamo il nome unico usando il tempo in millisecondi ---
                String fileName = System.currentTimeMillis() + "_" + immagine.getOriginalFilename();
                // -------------------------------------------------------------------

                Path filePath = uploadPath.resolve(fileName);
                Files.copy(immagine.getInputStream(), filePath); // Ora non troverà mai un duplicato!
                articolo.setUrlFoto("/uploads/" + fileName);
            }
        } catch (IOException e) {
            System.out.println("Errore immagine: " + e.getMessage());
        }

        this.articoloRepository.save(articolo);
        return "redirect:/armadio"; 
    }

    // ==========================================
    // 3. MODIFICA ARTICOLO
    // ==========================================
    @GetMapping("/articolo/modifica/{id}")
    public String mostraFormModifica(@PathVariable("id") Long id, Model model,
            @AuthenticationPrincipal Object principal) {

        Articolo articolo = this.articoloRepository.findById(id).orElse(null);
        Utente utenteLoggato = getUtenteLoggato(principal);

        if (articolo == null || utenteLoggato == null) return "redirect:/";

        if (articolo.getVenditore() != null && utenteLoggato.getEmail().equals(articolo.getVenditore().getEmail())) {
            model.addAttribute("articolo", articolo);
            return "modifica-articolo";
        }

        return "redirect:/";
    }

    @PostMapping("/articolo/modifica/{id}")
    public String salvaModificaArticolo(@PathVariable("id") Long id,
            @ModelAttribute("articolo") Articolo articoloModificato,
            @AuthenticationPrincipal Object principal) {
        
        Articolo articoloEsistente = this.articoloRepository.findById(id).orElse(null);
        Utente utenteLoggato = getUtenteLoggato(principal);

        if (articoloEsistente != null && utenteLoggato != null) {
            if (articoloEsistente.getVenditore() != null && utenteLoggato.getEmail().equals(articoloEsistente.getVenditore().getEmail())) {
                articoloEsistente.setNome(articoloModificato.getNome());
                articoloEsistente.setDescrizione(articoloModificato.getDescrizione());
                articoloEsistente.setPrezzo(articoloModificato.getPrezzo());
                this.articoloRepository.save(articoloEsistente);
            }
        }
        return "redirect:/armadio";
    }

    // ==========================================
    // 4. DETTAGLIO ARTICOLO
    // ==========================================
    @GetMapping("/articolo/{id}")
    public String mostraDettaglioArticolo(@PathVariable("id") Long id, Model model,
            @AuthenticationPrincipal Object principal) {
        
        Articolo articolo = this.articoloRepository.findById(id).orElse(null);
        if (articolo == null) return "redirect:/";

        Utente utenteLoggato = getUtenteLoggato(principal);
        if (utenteLoggato != null) {
            model.addAttribute("emailLoggata", utenteLoggato.getEmail());
            model.addAttribute("nomeLoggato", utenteLoggato.getNome());
        } else {
            model.addAttribute("nomeLoggato", "Ospite");
        }

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
            @AuthenticationPrincipal Object principal) {

        Articolo articolo = this.articoloRepository.findById(idArticolo).orElse(null);
        Utente autore = getUtenteLoggato(principal);

        if (articolo != null && autore != null) {
            recensione.setId(null); 
            recensione.setArticolo(articolo);
            recensione.setAutore(autore);
            this.recensioneRepository.save(recensione);
        }

        return "redirect:/articolo/" + idArticolo;
    }

    // ==========================================
    // 6. ELIMINA RECENSIONE
    // ==========================================
    @GetMapping("/recensione/elimina/{id}")
    public String eliminaRecensione(@PathVariable("id") Long id,
            @AuthenticationPrincipal Object principal) { 

        Recensione recensione = this.recensioneRepository.findById(id).orElse(null);
        Utente utenteLoggato = getUtenteLoggato(principal);

        if (recensione != null && utenteLoggato != null) {
            if (recensione.getAutore() != null && utenteLoggato.getEmail().equals(recensione.getAutore().getEmail())) {
                Long articoloId = recensione.getArticolo().getId();
                this.recensioneRepository.delete(recensione);
                return "redirect:/articolo/" + articoloId;
            }
        }
        return "redirect:/";
    }

    // ==========================================
    // 7. ELIMINA ARTICOLO (Dal proprio armadio)
    // ==========================================
    @GetMapping("/articolo/elimina/{id}")
    public String eliminaArticolo(@PathVariable("id") Long id, 
                                  @AuthenticationPrincipal Object principal) {
        
        Articolo articolo = this.articoloRepository.findById(id).orElse(null);
        Utente utenteLoggato = getUtenteLoggato(principal);
        
        if (articolo != null && utenteLoggato != null) {
            if (articolo.getVenditore() != null && utenteLoggato.getEmail().equals(articolo.getVenditore().getEmail())) {
                this.articoloRepository.delete(articolo);
            }
        }
        return "redirect:/armadio";
    }

    // ==========================================
    // 8. IL MIO ARMADIO (Area Personale)
    // ==========================================
    @GetMapping("/armadio")
    public String mostraMioArmadio(Model model, @AuthenticationPrincipal Object principal) {

        Utente utenteLoggato = getUtenteLoggato(principal);
        if (utenteLoggato == null) return "redirect:/login";

        List<Articolo> mieiArticoli = this.articoloRepository.findByVenditore(utenteLoggato);

        double mediaValutazioni = 0.0;
        int totaleRecensioni = 0;
        int sommaStelle = 0;
        for (Articolo articolo : mieiArticoli) {
            if (articolo.getRecensioni() != null) {
                for (Recensione r : articolo.getRecensioni()) {
                    sommaStelle += r.getValutazione();
                    totaleRecensioni++;
                }
            }
        }
        if (totaleRecensioni > 0) {
            mediaValutazioni = Math.round(((double) sommaStelle / totaleRecensioni) * 10.0) / 10.0;
        }

        model.addAttribute("utente", utenteLoggato);
        model.addAttribute("mieiArticoli", mieiArticoli);
        model.addAttribute("mediaValutazioni", mediaValutazioni);

        return "armadio";
    }

    
    // ==========================================
    // METODO HELPER UNIVERSALE PER IL LOGIN
    // ==========================================
    private Utente getUtenteLoggato(Object principal) {
        if (principal == null) return null;

        String email = null;
        String nome = null;
        String cognome = null;
        String provider = null;

        // 1. Estrazione dati in base al tipo di login
        if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
            org.springframework.security.oauth2.core.user.OAuth2User oauth = (org.springframework.security.oauth2.core.user.OAuth2User) principal;
            email = oauth.getAttribute("email");
            nome = oauth.getAttribute("given_name");
            cognome = oauth.getAttribute("family_name");
            provider = "GOOGLE";
        } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            email = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        }

        // 2. Controllo e salvataggio sul Database
        if (email != null) {
            Utente utente = this.userRepository.findByEmail(email).orElse(null);
            
            // AUTO-REGISTRAZIONE SE L'UTENTE GOOGLE È NUOVO
            if (utente == null && "GOOGLE".equals(provider)) {
                utente = new Utente();
                utente.setEmail(email);
                utente.setNome(nome != null ? nome : "Utente");
                utente.setCognome(cognome != null ? cognome : "Google");
                utente.setProvider(provider);
                
                // Salviamo il nuovo utente Google nel DB per sempre!
                this.userRepository.save(utente);
            }
            return utente;
        }
        return null;
    }
}