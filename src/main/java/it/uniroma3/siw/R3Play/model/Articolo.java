package it.uniroma3.siw.R3Play.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Articolo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String nome;
    private String descrizione;
    private Double prezzo;
    private String urlFoto;

    @ElementCollection
    @CollectionTable(name = "articolo_foto_urls", joinColumns = @JoinColumn(name = "articolo_id"))
    @Column(name = "url_foto")
    private List<String> fotoUrls = new ArrayList<>();

    @ManyToOne
    private Utente venditore;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;


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

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public Double getPrezzo() {
        return prezzo;
    }

    public void setPrezzo(Double prezzo) {
        this.prezzo = prezzo;
    }

    public String getUrlFoto() {
        return urlFoto;
    }

    public void setUrlFoto(String urlFoto) {
        this.urlFoto = urlFoto;
    }

    public List<String> getFotoUrls() {
        return fotoUrls;
    }

    public void setFotoUrls(List<String> fotoUrls) {
        this.fotoUrls = fotoUrls;
    }

    public void addFotoUrl(String fotoUrl) {
        if (this.fotoUrls == null) {
            this.fotoUrls = new ArrayList<>();
        }
        this.fotoUrls.add(fotoUrl);
    }

    public Utente getVenditore() {
        return venditore;
    }

    public void setVenditore(Utente venditore) {
        this.venditore = venditore;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

}