package com.paquito.primerspring.controllers;

import com.paquito.primerspring.models.Categoria;
import com.paquito.primerspring.services.CategoriaServiceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaServiceManager serviceManager;

    @GetMapping
    @Transactional(readOnly = true)
    public List<Categoria> findAllCategorias() {
        return this.serviceManager.findAll();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public Categoria findCategoriaById(@PathVariable Long id) {
        return this.serviceManager.findById(id);
    }

    @PostMapping
    public Categoria saveCategoria(@RequestBody Categoria categoria) {
        return this.serviceManager.save(categoria);
    }

    @PutMapping("/{id}")
    public Categoria updateCategoria(@PathVariable Long id, @RequestBody Categoria categoria) {
        return this.serviceManager.update(id, categoria);
    }

    @DeleteMapping("/{id}")
    public void deleteCategoria(@PathVariable Long id) {
        this.serviceManager.deleteById(id);
    }
}

