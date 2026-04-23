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
    private String password;
    private String provider; // GOOGLE o LOCAL

    @OneToMany(mappedBy = "destinatario", cascade = CascadeType.ALL)
    private List<Recensione> recensioniRicevute;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public List<Recensione> getRecensioniRicevute() {
        return recensioniRicevute;
    }

    public void setRecensioniRicevute(List<Recensione> recensioniRicevute) {
        this.recensioniRicevute = recensioniRicevute;
    }

    // Getter e Setter...
}