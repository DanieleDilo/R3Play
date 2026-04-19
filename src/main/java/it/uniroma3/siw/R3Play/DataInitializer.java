package it.uniroma3.siw.R3Play;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import it.uniroma3.siw.R3Play.model.Articolo;
import it.uniroma3.siw.R3Play.model.Recensione;
import it.uniroma3.siw.R3Play.model.Utente;
import it.uniroma3.siw.R3Play.repository.ArticoloRepository;
import it.uniroma3.siw.R3Play.repository.RecensioneRepository;
import it.uniroma3.siw.R3Play.repository.UserRepository;

// @Component
// Data initializer disabled to prevent automatic DB population at startup
public class DataInitializer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticoloRepository articoloRepository;

    @Autowired
    private RecensioneRepository recensioneRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeDatabase() {
        if (this.articoloRepository.count() > 0) {
            return;
        }

        Utente marco = createUserIfMissing("Marco", "Rossi", "marco.rossi@r3play.it", "Marco2026!", "LOCAL");
        Utente giulia = createUserIfMissing("Giulia", "Ferrari", "giulia.ferrari@r3play.it", "Giulia2026!", "LOCAL");
        Utente andrea = createUserIfMissing("Andrea", "Bianchi", "andrea.bianchi@r3play.it", "Andrea2026!", "LOCAL");
        Utente alice = createUserIfMissing("Alice", "Neri", "alice.neri@r3play.it", "Alice2026!", "LOCAL");
        Utente federico = createUserIfMissing("Federico", "Verdi", "federico.verdi@r3play.it", "Federico2026!", "LOCAL");
        Utente sara = createUserIfMissing("Sara", "Conti", "sara.conti@r3play.it", "Sara2026!", "LOCAL");
        Utente luca = createUserIfMissing("Luca", "Moretti", "luca.moretti@r3play.it", "Luca2026!", "LOCAL");
        Utente elena = createUserIfMissing("Elena", "Romano", "elena.romano@r3play.it", "Elena2026!", "LOCAL");
        Utente tommaso = createUserIfMissing("Tommaso", "Ricci", "tommaso.ricci@r3play.it", "Tommaso2026!", "LOCAL");
        Utente michela = createUserIfMissing("Michela", "Patuano", "michela.patuano@r3play.it", "Michela2026!", "LOCAL");

        Articolo art1 = new Articolo();
        art1.setNome("Scarpe da basket premium");
        art1.setDescrizione("Scarpe originali in ottime condizioni, con supporto alla caviglia e suola ad alta aderenza.");
        art1.setPrezzo(89.90);
        art1.setVenditore(giulia);
        art1.setUrlFoto("https://images.unsplash.com/photo-1517649763962-0c623066013b?auto=format&fit=crop&w=1200&q=80");
        art1.setFotoUrls(List.of(
                "https://images.unsplash.com/photo-1517649763962-0c623066013b?auto=format&fit=crop&w=1200&q=80",
                "https://images.unsplash.com/photo-1528701800489-20f77111a8a5?auto=format&fit=crop&w=1200&q=80",
                "https://images.unsplash.com/photo-1529655656329-4115f10520f8?auto=format&fit=crop&w=1200&q=80"
        ));

        Articolo art2 = new Articolo();
        art2.setNome("Pallone da calcio match");
        art2.setDescrizione("Pallone da calcio di livello professionale, cuciture resistenti e pressione stabile per allenamenti e partite.");
        art2.setPrezzo(42.50);
        art2.setVenditore(andrea);
        art2.setUrlFoto("https://images.unsplash.com/photo-1523264751971-33c1cb04891f?auto=format&fit=crop&w=1200&q=80");
        art2.setFotoUrls(List.of(
                "https://images.unsplash.com/photo-1523264751971-33c1cb04891f?auto=format&fit=crop&w=1200&q=80",
                "https://images.unsplash.com/photo-1517649763962-0c623066013b?auto=format&fit=crop&w=1200&q=80"
        ));

        Articolo art3 = new Articolo();
        art3.setNome("Giacca tecnica da running");
        art3.setDescrizione("Giacca antivento leggera e traspirante, perfetta per corse all'aperto anche in giornata umida.");
        art3.setPrezzo(59.00);
        art3.setVenditore(marco);
        art3.setUrlFoto("https://images.unsplash.com/photo-1521412644187-c49fa049e84d?auto=format&fit=crop&w=1200&q=80");
        art3.setFotoUrls(List.of(
                "https://images.unsplash.com/photo-1521412644187-c49fa049e84d?auto=format&fit=crop&w=1200&q=80",
                "https://images.unsplash.com/photo-1520974735194-1f76e0b09586?auto=format&fit=crop&w=1200&q=80"
        ));

        Articolo art4 = new Articolo();
        art4.setNome("Zaino sportivo da allenamento");
        art4.setDescrizione("Zaino resistente all'acqua con scomparti dedicati per scarpe, borraccia e accessori.");
        art4.setPrezzo(39.90);
        art4.setVenditore(giulia);
        art4.setUrlFoto("https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=1200&q=80");
        art4.setFotoUrls(List.of(
                "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=1200&q=80",
                "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=1200&q=80"
        ));

        Articolo art5 = new Articolo();
        art5.setNome("Giubbino da ciclismo riflettente");
        art5.setDescrizione("Giacca da bici tecnica con inserti riflettenti e rete traspirante sul dorso.");
        art5.setPrezzo(74.00);
        art5.setVenditore(alice);
        art5.setUrlFoto("https://images.unsplash.com/photo-1522156373664-4c5f7f0d93e6?auto=format&fit=crop&w=1200&q=80");
        art5.setFotoUrls(List.of(
                "https://images.unsplash.com/photo-1522156373664-4c5f7f0d93e6?auto=format&fit=crop&w=1200&q=80",
                "https://images.unsplash.com/photo-1508606572321-901ea4437070?auto=format&fit=crop&w=1200&q=80"
        ));

        Articolo art6 = new Articolo();
        art6.setNome("Tuta da palestra full zip");
        art6.setDescrizione("Tuta fitness con tessuto elasticizzato e dettagli minimal neri e verdi.");
        art6.setPrezzo(49.90);
        art6.setVenditore(federico);
        art6.setUrlFoto("https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=1200&q=80");
        art6.setFotoUrls(List.of(
                "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=1200&q=80",
                "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?auto=format&fit=crop&w=1200&q=80"
        ));

        Articolo art7 = new Articolo();
        art7.setNome("Guanti da boxe professionali");
        art7.setDescrizione("Guanti imbottiti in pelle, perfetti per sparring leggero e allenamenti intensi.");
        art7.setPrezzo(55.00);
        art7.setVenditore(sara);
        art7.setUrlFoto("https://images.unsplash.com/photo-1517014161427-38d8a2d72e1f?auto=format&fit=crop&w=1200&q=80");
        art7.setFotoUrls(List.of(
                "https://images.unsplash.com/photo-1517014161427-38d8a2d72e1f?auto=format&fit=crop&w=1200&q=80",
                "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=1200&q=80"
        ));

        Articolo art8 = new Articolo();
        art8.setNome("Smartwatch sportivo");
        art8.setDescrizione("Smartwatch con GPS, cardiofrequenzimetro e funzioni da allenamento avanzato.");
        art8.setPrezzo(129.90);
        art8.setVenditore(luca);
        art8.setUrlFoto("https://images.unsplash.com/photo-1516728778615-2d590ea1856f?auto=format&fit=crop&w=1200&q=80");
        art8.setFotoUrls(List.of(
                "https://images.unsplash.com/photo-1516728778615-2d590ea1856f?auto=format&fit=crop&w=1200&q=80",
                "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80"
        ));

        Articolo art9 = new Articolo();
        art9.setNome("Pantaloni da yoga a compressione");
        art9.setDescrizione("Pantaloni aderenti con vita alta, ideali per yoga, pilates e allenamenti indoor.");
        art9.setPrezzo(34.90);
        art9.setVenditore(elena);
        art9.setUrlFoto("https://images.unsplash.com/photo-1526401485004-5dfb2120bd72?auto=format&fit=crop&w=1200&q=80");
        art9.setFotoUrls(List.of(
                "https://images.unsplash.com/photo-1526401485004-5dfb2120bd72?auto=format&fit=crop&w=1200&q=80",
                "https://images.unsplash.com/photo-1508609317117-0f7b10cf0cb4?auto=format&fit=crop&w=1200&q=80"
        ));

        Articolo art10 = new Articolo();
        art10.setNome("Set pesi da casa 10kg");
        art10.setDescrizione("Coppia di manubri regolabili con impugnatura antiscivolo e peso modulare.");
        art10.setPrezzo(69.90);
        art10.setVenditore(tommaso);
        art10.setUrlFoto("https://images.unsplash.com/photo-1517960413843-0aeea35a6d48?auto=format&fit=crop&w=1200&q=80");
        art10.setFotoUrls(List.of(
                "https://images.unsplash.com/photo-1517960413843-0aeea35a6d48?auto=format&fit=crop&w=1200&q=80",
                "https://images.unsplash.com/photo-1594737625785-0d02c20fda78?auto=format&fit=crop&w=1200&q=80"
        ));

        this.articoloRepository.save(art1);
        this.articoloRepository.save(art2);
        this.articoloRepository.save(art3);
        this.articoloRepository.save(art4);
        this.articoloRepository.save(art5);
        this.articoloRepository.save(art6);
        this.articoloRepository.save(art7);
        this.articoloRepository.save(art8);
        this.articoloRepository.save(art9);
        this.articoloRepository.save(art10);

        List<Recensione> reviews = List.of(
                newReview(art1, federico, 5, "Scarpe comode, perfette per il parquet. Consiglio al 100%."),
                newReview(art1, sara, 4, "Ottima tenuta, solo un leggero segno sulla suola. Buon affare."),
                newReview(art1, luca, 5, "Materiali top e perfette per allenamenti intensi."),
                newReview(art1, elena, 5, "Molto belle e comode, la resa cromatica è fedele."),
                newReview(art2, alice, 4, "Pallone preciso, ha mantenuto bene la pressione anche dopo molte partite."),
                newReview(art2, marco, 5, "Perfetto per partite di calcetto e allenamenti indoor."),
                newReview(art2, giulia, 4, "Ottimo grip, consigliato a chi cerca un pallone affidabile."),
                newReview(art2, michela, 5, "Qualità superiore rispetto ad altri palloni ordinari."),
                newReview(art3, tommaso, 5, "Giacca perfetta per running con brezza. Non fa sudare troppo."),
                newReview(art3, federico, 5, "Il tessuto è morbido e anti vento. Ideale per lunghe corse."),
                newReview(art3, andrea, 4, "Buona vestibilità, unica nota: un piccolo odore di fabbrica sparito dopo un lavaggio."),
                newReview(art3, luca, 5, "Design bello e ottima traspirabilità. La uso anche nei weekend."),
                newReview(art4, elena, 4, "Zaino spazioso e resistente; tiene tutto in ordine."),
                newReview(art4, michela, 5, "Molto comodo e perfetto per la palestra. La tracolla è imbottita."),
                newReview(art4, sara, 4, "Solido e waterproof, lo uso anche per il lavoro."),
                newReview(art4, alice, 5, "Capiente e con scomparti intelligenti. Vale il prezzo."),
                newReview(art5, giulia, 5, "Giacca da bici elegante e comoda, ottima visibilità su strada."),
                newReview(art5, tommaso, 4, "Perfetta per le uscite al tramonto, molto leggera."),
                newReview(art5, marco, 5, "Materiale top e taglio aderente. La consiglio a chi va spesso in bici."),
                newReview(art5, luca, 4, "Molto traspirante, comoda anche sotto il sole."),
                newReview(art6, andrea, 4, "Tuta molto elegante, buon tessuto stretch."),
                newReview(art6, federico, 5, "Perfetta per allenamenti leggeri e uscite in città."),
                newReview(art6, michela, 5, "Adoro il taglio e il tessuto, vestibilità eccellente."),
                newReview(art6, elena, 4, "Ottimo prodotto, il verde è molto elegante."),
                newReview(art7, sara, 5, "Guanti robusti e comodi, ottima protezione per le mani."),
                newReview(art7, alice, 4, "Buona imbottitura, consigliati per allenamenti regolari."),
                newReview(art7, giulia, 5, "Prezzo giusto e qualità professionale. Li riprenderò."),
                newReview(art7, tommaso, 4, "Bel design e ottima presa. Li uso anche per sacco leggero."),
                newReview(art8, luca, 5, "Funzioni complete e batteria duratura. Mi segue perfettamente negli allenamenti."),
                newReview(art8, elena, 5, "Schermo chiaro e app intuitiva. Ottimo smartwatch sportivo."),
                newReview(art8, andrea, 4, "Molte features, semplice da usare. Molto preciso nel tracking."),
                newReview(art8, michela, 5, "Lo uso tutti i giorni, perfetto anche per monitorare il sonno."),
                newReview(art9, marco, 4, "Pantaloni comodi e aderenti, ottimi per yoga e pilates."),
                newReview(art9, sara, 5, "Vestibilità eccellente e materiale molto morbido."),
                newReview(art9, alice, 4, "Perfetti per lo stretching, non scivolano."),
                newReview(art9, federico, 5, "Ideali per allenamenti indoor, molto confortevoli."),
                newReview(art10, tommaso, 5, "Manubri facili da regolare, ottimi per la palestra in casa."),
                newReview(art10, elena, 4, "Pratici e solidi, si montano velocemente."),
                newReview(art10, luca, 5, "Perfetti per gli allenamenti a circuito, stabilità ottima."),
                newReview(art10, giulia, 4, "Qualità eccellente, consigliati per chi inizia.")
        );

        this.recensioneRepository.saveAll(reviews);
    }

    private Recensione newReview(Articolo articolo, Utente autore, int valutazione, String testo) {
        Recensione recensione = new Recensione();
        recensione.setArticolo(articolo);
        recensione.setAutore(autore);
        recensione.setValutazione(valutazione);
        recensione.setTesto(testo);
        return recensione;
    }

    private Utente createUserIfMissing(String nome, String cognome, String email, String password, String provider) {
        Utente utente = this.userRepository.findByEmail(email).orElse(null);
        if (utente == null) {
            utente = new Utente();
            utente.setNome(nome);
            utente.setCognome(cognome);
            utente.setEmail(email);
            utente.setPassword(passwordEncoder.encode(password));
            utente.setProvider(provider);
            this.userRepository.save(utente);
        }
        return utente;
    }
}
