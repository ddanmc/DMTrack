package com.paquito.primerspring.controllers;

import com.paquito.primerspring.models.Producto;
import com.paquito.primerspring.repositories.ProductoRepository;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reactive/productos")
public class ProductoReactiveController {

    private final ProductoRepository productoRepository;

    public ProductoReactiveController(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @GetMapping
    public Flux<Producto> listarProductos() {
        List<Producto> productos = productoRepository.findAll();
        return Flux.fromIterable(productos);
    }

    @GetMapping("/{id}")
    public Mono<Producto> obtenerPorId(@PathVariable Long id) {
        Optional<Producto> producto = productoRepository.findById(id);
        return Mono.justOrEmpty(producto);
    }
}
