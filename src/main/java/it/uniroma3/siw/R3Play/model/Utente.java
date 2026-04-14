package it.uniroma3.siw.R3Play.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Utente {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String nome;
    private String cognome;
    private String email;

    @OneToMany(mappedBy = "venditore")
    private List<Articolo> articoliInVendita;

    @OneToMany(mappedBy = "autore")
    private List<Recensione> recensioniScritte;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Articolo> getArticoliInVendita() {
        return articoliInVendita;
    }

    public void setArticoliInVendita(List<Articolo> articoliInVendita) {
        this.articoliInVendita = articoliInVendita;
    }

    public List<Recensione> getRecensioniScritte() {
        return recensioniScritte;
    }

    public void setRecensioniScritte(List<Recensione> recensioniScritte) {
        this.recensioniScritte = recensioniScritte;
    }

   
}