package com.paquito.primerspring.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paquito.primerspring.models.Categoria;
import com.paquito.primerspring.repositories.CategoriaRepository;

@Service
public class CategoriaServiceManager implements CategoriaService {

    @Autowired
    private CategoriaRepository repository;

    @Override
    public List<Categoria> findAll() {
        return (List<Categoria>) this.repository.findAll();
    }

    @Override
    public Categoria findById(Long id) {
        return this.repository.findById(id).orElse(null);
    }

    @Override
    public Categoria save(Categoria categoria) {
        return this.repository.save(categoria);
    }

    @Override
    public Categoria update(Long id, Categoria categoria) {
        Categoria existingCategoria = this.repository.findById(id).orElse(null);
        if (existingCategoria != null) {
            existingCategoria.setNombre(categoria.getNombre());
            existingCategoria.setDescripcion(categoria.getDescripcion());
            return this.repository.save(existingCategoria);
        }
        return null;
    }

    @Override
    public void deleteById(Long id) {
        this.repository.deleteById(id);
    }
}

