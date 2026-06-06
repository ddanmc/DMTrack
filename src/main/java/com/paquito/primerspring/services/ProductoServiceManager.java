package com.paquito.primerspring.services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.paquito.primerspring.models.Producto;
import com.paquito.primerspring.repositories.ProductoRepository;

@Service
public class ProductoServiceManager implements ProductoService {

    @Autowired
    private ProductoRepository repository;

    @Override
    public List<Producto> findAll() {
        return repository.findAll();
    }

    @Override
    public Producto findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public Producto save(Producto producto) {
        return repository.save(producto);
    }

    @Override
    public Producto update(Long id, Producto producto) {
        Producto existing = repository.findById(id).orElse(null);
        if (existing != null) {
            existing.setNombre(producto.getNombre());
            existing.setDescripcion(producto.getDescripcion());
            existing.setPrecio(producto.getPrecio());
            existing.setCantidad(producto.getCantidad());
            existing.setStock(producto.getStock());
            existing.setCategoria(producto.getCategoria());
            return repository.save(existing);
        }
        return null;
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
