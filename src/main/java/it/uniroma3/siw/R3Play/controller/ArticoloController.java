package it.uniroma3.siw.R3Play.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${upload.dir:uploads}")
    private String uploadDir;

    @ModelAttribute("nomeLoggato")
    public String populateNomeLoggato(@AuthenticationPrincipal Object principal) {
        Utente utenteLoggato = getUtenteLoggato(principal);
        return utenteLoggato != null ? buildNomeCompleto(utenteLoggato) : null;
    }

    @ModelAttribute("emailLoggata")
    public String populateEmailLoggata(@AuthenticationPrincipal Object principal) {
        Utente utenteLoggato = getUtenteLoggato(principal);
        return utenteLoggato != null ? utenteLoggato.getEmail() : null;
    }

    // ==========================================
    // 1. WELCOME / SPLASH
    // ==========================================
    @GetMapping("/")
    public String mostraWelcome(Model model, @AuthenticationPrincipal Object principal) {
        Utente utenteLoggato = getUtenteLoggato(principal);
        if (utenteLoggato != null) {
            return "redirect:/vetrina";
        }
        return "welcome";
    }

    // ==========================================
    // 2. VETRINA
    // ==========================================
    @GetMapping("/vetrina")
    public String mostraVetrina(@RequestParam(name = "q", required = false) String query,
            Model model, @AuthenticationPrincipal Object principal) {

        Iterable<Articolo> articoli;
        if (query != null && !query.isBlank()) {
            articoli = this.articoloRepository.findByNomeContainingIgnoreCaseOrDescrizioneContainingIgnoreCase(query, query);
            model.addAttribute("query", query);
            model.addAttribute("risultatiFiltro", true);
        } else {
            articoli = this.articoloRepository.findAll();
            model.addAttribute("risultatiFiltro", false);
        }

        model.addAttribute("articoli", articoli);

        Utente utenteLoggato = getUtenteLoggato(principal);
        if (utenteLoggato != null) {
            model.addAttribute("emailLoggata", utenteLoggato.getEmail());
            model.addAttribute("nomeLoggato", buildNomeCompleto(utenteLoggato));
        }

        return "vetrina";
    }

    // ==========================================
    // 3. NUOVO ARTICOLO
    // ==========================================
    @GetMapping("/articolo/nuovo")
    public String mostraFormNuovoArticolo(Model model) {
        model.addAttribute("articolo", new Articolo());
        return "nuovo-articolo";
    }

    @PostMapping("/articolo/nuovo")
    public String salvaNuovoArticolo(@ModelAttribute("articolo") Articolo articolo,
            @RequestParam("fileImmagine") MultipartFile[] immagini,
            @AuthenticationPrincipal Object principal) {

        Utente venditore = getUtenteLoggato(principal);
        if (venditore != null) {
            articolo.setVenditore(venditore);
        }

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            boolean firstImage = true;
            for (MultipartFile immagine : immagini) {
                if (immagine != null && !immagine.isEmpty()) {
                    String fileName = System.currentTimeMillis() + "_" + immagine.getOriginalFilename();
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(immagine.getInputStream(), filePath);
                    String fileUrl = "/uploads/" + fileName;
                    if (firstImage) {
                        articolo.setUrlFoto(fileUrl);
                        firstImage = false;
                    }
                    articolo.addFotoUrl(fileUrl);
                }
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
            @RequestParam("fileImmagine") MultipartFile[] immagini,
            @AuthenticationPrincipal Object principal) {
        
        Articolo articoloEsistente = this.articoloRepository.findById(id).orElse(null);
        Utente utenteLoggato = getUtenteLoggato(principal);

        if (articoloEsistente != null && utenteLoggato != null) {
            if (articoloEsistente.getVenditore() != null && utenteLoggato.getEmail().equals(articoloEsistente.getVenditore().getEmail())) {
                articoloEsistente.setNome(articoloModificato.getNome());
                articoloEsistente.setDescrizione(articoloModificato.getDescrizione());
                articoloEsistente.setPrezzo(articoloModificato.getPrezzo());

                try {
                    Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }

                    for (MultipartFile immagine : immagini) {
                        if (immagine != null && !immagine.isEmpty()) {
                            String fileName = System.currentTimeMillis() + "_" + immagine.getOriginalFilename();
                            Path filePath = uploadPath.resolve(fileName);
                            Files.copy(immagine.getInputStream(), filePath);
                            String fileUrl = "/uploads/" + fileName;
                            articoloEsistente.addFotoUrl(fileUrl);
                            if (articoloEsistente.getUrlFoto() == null) {
                                articoloEsistente.setUrlFoto(fileUrl);
                            }
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Errore immagine durante la modifica: " + e.getMessage());
                }

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
            model.addAttribute("nomeLoggato", buildNomeCompleto(utenteLoggato));
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
            Object emailAttr = oauth.getAttribute("email");
            email = emailAttr != null ? emailAttr.toString() : null;
            provider = "GOOGLE";

            Object givenName = oauth.getAttribute("given_name");
            Object familyName = oauth.getAttribute("family_name");
            Object fullName = oauth.getAttribute("name");

            if (givenName != null) {
                nome = givenName.toString();
            }
            if (familyName != null) {
                cognome = familyName.toString();
            }
            if ((nome == null || nome.isBlank()) && fullName != null) {
                String[] parts = fullName.toString().trim().split(" ");
                if (parts.length > 0) {
                    nome = parts[0];
                    if (parts.length > 1) {
                        cognome = parts[parts.length - 1];
                    }
                }
            }
            if ((nome == null || nome.isBlank()) && email != null) {
                nome = email.split("@")[0];
            }
        } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            email = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        }

        // 2. Controllo e salvataggio sul Database
        if (email != null) {
            Utente utente = this.userRepository.findByEmail(email).orElse(null);
            if (utente == null && "GOOGLE".equals(provider)) {
                utente = new Utente();
                utente.setEmail(email);
                utente.setNome(capitalize(nome != null ? nome : "Utente"));
                utente.setCognome(capitalize(cognome != null ? cognome : "Google"));
                utente.setProvider(provider);
                this.userRepository.save(utente);
            } else if (utente != null && "GOOGLE".equals(provider)) {
                boolean dirty = false;
                if ((utente.getNome() == null || utente.getNome().isBlank()) && nome != null) {
                    utente.setNome(capitalize(nome));
                    dirty = true;
                }
                if ((utente.getCognome() == null || utente.getCognome().isBlank()) && cognome != null) {
                    utente.setCognome(capitalize(cognome));
                    dirty = true;
                }
                if (dirty) {
                    this.userRepository.save(utente);
                }
            }
            return utente;
        }
        return null;
    }

    private String buildNomeCompleto(Utente utente) {
        if (utente == null) {
            return null;
        }
        String nome = utente.getNome();
        String cognome = utente.getCognome();
        if ((nome == null || nome.isBlank()) && (cognome == null || cognome.isBlank())) {
            String email = utente.getEmail();
            return email != null ? email.split("@")[0] : "Utente";
        }
        StringBuilder fullName = new StringBuilder();
        if (nome != null && !nome.isBlank()) {
            fullName.append(capitalize(nome));
        }
        if (cognome != null && !cognome.isBlank()) {
            if (fullName.length() > 0) {
                fullName.append(" ");
            }
            fullName.append(capitalize(cognome));
        }
        return fullName.toString();
    }

    private String capitalize(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        String[] words = text.trim().split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (builder.length() > 0) {
                builder.append(" ");
            }
            if (word.length() == 1) {
                builder.append(word.toUpperCase());
            } else {
                builder.append(word.substring(0, 1).toUpperCase()).append(word.substring(1).toLowerCase());
            }
        }
        return builder.toString();
    }

    // ==========================================
    // 9. MODIFICA PROFILO UTENTE
    // ==========================================
    @PostMapping("/utente/modifica")
    public String modificaProfiloUtente(@RequestParam("nome") String nome, 
                                        @RequestParam("cognome") String cognome, 
                                        @AuthenticationPrincipal Object principal) {
        
        // Recuperiamo l'utente loggato usando il nostro fidato helper
        Utente utente = getUtenteLoggato(principal);
        
        if (utente != null) {
            // Aggiorniamo i dati
            utente.setNome(nome);
            utente.setCognome(cognome);
            
            // Salviamo nel database
            this.userRepository.save(utente);
        }
        
        // Lo rimandiamo all'armadio dove vedrà subito il nuovo nome!
        return "redirect:/armadio";
    }
}