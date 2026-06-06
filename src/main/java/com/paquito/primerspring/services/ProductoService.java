package com.paquito.primerspring.services;

import java.util.List;
import com.paquito.primerspring.models.Producto;

public interface ProductoService {
    List<Producto> findAll();
    Producto findById(Long id);
    Producto save(Producto producto);
    Producto update(Long id, Producto producto);
    void deleteById(Long id);
}

