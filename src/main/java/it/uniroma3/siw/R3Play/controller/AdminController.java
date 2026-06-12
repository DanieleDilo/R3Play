package it.uniroma3.siw.R3Play.controller;

import it.uniroma3.siw.R3Play.model.Categoria;
import it.uniroma3.siw.R3Play.service.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping("/categorie")
    public String gestioneCategorie(Model model) {
        model.addAttribute("categorie", categoriaService.trovaTutte());
        model.addAttribute("nuovaCategoria", new Categoria());
        return "admin/categorie";
    }

    @PostMapping("/categoria")
    public String nuovaCategoria(@ModelAttribute("nuovaCategoria") Categoria categoria) {
        categoriaService.salvaCategoria(categoria);
        return "redirect:/admin/categorie";
    }

    @PostMapping("/categoria/elimina/{id}")
    public String eliminaCategoria(@PathVariable Long id) {
        categoriaService.eliminaCategoria(id);
        return "redirect:/admin/categorie";
    }
}
