# Guida Completa al Progetto R3Play
### Architettura, Logica e Funzionalità del Marketplace Sportivo

Questa guida è stata pensata per aiutarti a comprendere ogni aspetto tecnico e funzionale del progetto **R3Play**. Copre l'intera struttura del codice, le scelte architetturali e le ottimizzazioni implementate.

---

## 1. Architettura del Progetto
Il progetto segue il pattern **MVC (Model-View-Controller)** di Spring Boot, organizzato in layer (strati) per garantire la separazione delle responsabilità:

*   **Model (Entità)**: Rappresentano i dati salvati nel Database (es. Articolo, Utente, Categoria).
*   **Repository**: Interfacce che dialogano con il DB tramite Spring Data JPA.
*   **Service**: Qui risiede la "Logica di Business". I Service coordinano le operazioni, gestiscono le transazioni (`@Transactional`) e applicano le regole (es. "non puoi recensire te stesso").
*   **Controller**: Gestiscono le richieste HTTP, preparano i dati e restituiscono le viste (Thymeleaf) o dati JSON (per React).
*   **View (Template)**: Pagine HTML dinamiche realizzate con **Thymeleaf**.

---

## 2. Il Modello dei Dati (Entità)
Il cuore del sistema è composto da quattro entità principali:

1.  **Utente**: Gestisce nome, email, password (criptata), ruolo (`USER` o `ADMIN`) e il provider di login (`LOCAL` o `GOOGLE`).
2.  **Articolo**: Rappresenta un annuncio. È collegato a un **Venditore** (Utente) e a una **Categoria**. Gestisce anche una lista di URL per le immagini.
3.  **Categoria**: Semplice entità con un nome (es. "Basket", "Nuoto") che raggruppa gli articoli.
4.  **Recensione**: Un feedback lasciato da un Utente (Autore) a un altro (Destinatario), con un voto da 1 a 5 e un commento.

### Relazioni Chiave:
*   `Articolo` -> `Utente`: Molti-a-Uno (Un utente può vendere molti articoli).
*   `Articolo` -> `Categoria`: Molti-a-Uno (Molti articoli appartengono a una categoria).
*   `Recensione` -> `Utente` (Autore/Destinatario): Molti-a-Uno.

---

## 3. Funzionalità Administrative
L'amministratore ha poteri speciali per mantenere l'ordine nel marketplace:

*   **Gestione Categorie**: Tramite l' `AdminController`, l'admin può visualizzare, creare ed eliminare le categorie. Questo permette di espandere il catalogo del sito dinamicamente.
*   **Moderazione**: L'admin può eliminare qualsiasi annuncio o recensione ritenuta inappropriata.
*   **AdminInitializer**: Al primo avvio, il sistema crea automaticamente un account admin (`admin@r3play.it` / `admin`) per permettere l'accesso immediato alle funzioni di gestione.

---

## 4. Sistema di Classifica (Leaderboard)
È una delle funzionalità più avanzate del sito. Permette agli utenti di vedere chi sono i venditori più affidabili.

*   **La Query Ottimizzata**: Invece di fare centinaia di query (una per ogni utente), il `UserRepository` esegue una singola query SQL complessa (`findUtentiConMediaRecensioni`) che calcola media voti e numero di feedback in un colpo solo.
*   **Visualizzazione**: La pagina `/classifica` mostra i profili ordinati per media valutazione, con stelle dorate e statistiche di vendita.

---

## 5. Gestione Annunci e Immagini
*   **Creazione/Modifica**: Gli utenti possono caricare immagini tramite i form. Il `ArticoloService` gestisce il salvataggio dei file sul disco (`/uploads/`) e salva il percorso nel database.
*   **Ricerca Avanzata**: La barra di ricerca nella Vetrina permette di cercare contemporaneamente sia per **nome articolo** che per **nome utente**, rendendo facilissimo trovare un venditore specifico.

---

## 6. Sicurezza e Autenticazione
Utilizziamo **Spring Security** con una configurazione ibrida:

*   **Login Locale**: Email e Password salvate nel DB (le password sono "hashate" con BCrypt per sicurezza).
*   **Login Google (OAuth2)**: Integrazione con Google per un accesso rapido. Il sistema riconosce l'email e crea automaticamente un profilo locale se non esiste.
*   **Protezione Rotte**: Pagine come `/armadio` o `/articolo/nuovo` sono accessibili solo se loggati. Le rotte `/admin/**` richiedono esplicitamente il ruolo `ROLE_ADMIN`.

---

## 7. Frontend Moderno
*   **Glassmorphism CSS**: Il design usa trasparenze, sfocature (`backdrop-filter`) e colori vibranti (arancione neon su sfondo scuro) per un look "Apple-style".
*   **React Integration**: La sezione dei commenti/recensioni è un'applicazione **React** separata montata dentro Thymeleaf. Questo permette di postare ed eliminare recensioni in tempo reale senza ricaricare l'intera pagina.

---

## 8. Ottimizzazioni Performance (N+1 Problem)
Il progetto è stato ottimizzato per essere veloce anche con molti dati:

*   **Fetch Strategies**: Usiamo `@EntityGraph` e `JOIN FETCH` nei Repository. Questo istruisce Hibernate a recuperare le entità collegate (come il Venditore di un Articolo) con un unico "JOIN" SQL, invece di fare tante piccole query separate.
*   **Transactional**: Tutte le operazioni sul DB sono protette da transazioni. Se un'operazione fallisce (es. errore disco durante salvataggio foto), il database torna automaticamente allo stato precedente, evitando dati corrotti.

---

## 9. Come Studiare il Codice
Per padroneggiare il progetto, segui questo ordine di lettura:
1.  **Model**: Guarda come sono fatte le classi Java e le annotazioni `@Entity`.
2.  **Controller**: Osserva come le rotte (URL) mappano i dati verso i template.
3.  **Service**: Studia la logica di business (come vengono filtrati o salvati i dati).
4.  **Template (HTML)**: Nota come Thymeleaf usa `th:each`, `th:if` e `th:text` per mostrare i dati dinamici.

---
> [!TIP]
> **Consiglio per l'esame**: Se ti chiedono come hai gestito le performance, cita il **problema N+1** e spiega che lo hai risolto usando gli **EntityGraph** nei Repository per ottimizzare i tempi di caricamento.
