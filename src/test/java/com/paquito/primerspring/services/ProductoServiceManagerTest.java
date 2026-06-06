package com.paquito.primerspring.services;

import com.paquito.primerspring.models.Producto;
import com.paquito.primerspring.repositories.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductoServiceManagerTest {

    @Mock
    private ProductoRepository repository;

    @InjectMocks
    private ProductoServiceManager service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("findAll debe retornar todos los productos")
    void testFindAll() {
        Producto p1 = new Producto();
        p1.setId(1L);   // Long
        p1.setNombre("Lapicero");

        Producto p2 = new Producto();
        p2.setId(2L);   // Long
        p2.setNombre("Cuaderno");

        when(repository.findAll()).thenReturn(Arrays.asList(p1, p2));

        List<Producto> result = service.findAll();

        assertEquals(2, result.size());
        assertEquals("Lapicero", result.get(0).getNombre());
        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("findById existente")
    void testFindByIdFound() {
        Producto p = new Producto();
        p.setId(7L);   // Long
        p.setNombre("Borrador");

        when(repository.findById(7L)).thenReturn(Optional.of(p));

        Producto result = service.findById(7L);

        assertNotNull(result);
        assertEquals(7L, result.getId());
        verify(repository).findById(7L);
    }

    @Test
    @DisplayName("findById no existente")
    void testFindByIdNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertNull(service.findById(99L));
        verify(repository).findById(99L);
    }

    @Test
    @DisplayName("save delega en repository.save")
    void testSave() {
        Producto nuevo = new Producto();
        nuevo.setNombre("Resaltador");
        nuevo.setPrecio(2500.0);

        Producto guardado = new Producto();
        guardado.setId(10L);   // Long
        guardado.setNombre("Resaltador");
        guardado.setPrecio(2500.0);

        when(repository.save(nuevo)).thenReturn(guardado);

        Producto result = service.save(nuevo);

        assertEquals(10L, result.getId());
        verify(repository).save(nuevo);
    }

    @Test
    @DisplayName("update existente actualiza campos")
    void testUpdateWhenExists() {
        Producto existente = new Producto();
        existente.setId(5L);   // Long
        existente.setNombre("Antiguo");
        existente.setPrecio(1000.0);

        Producto cambios = new Producto();
        cambios.setNombre("Nuevo");
        cambios.setPrecio(2000.0);

        when(repository.findById(5L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        Producto actualizado = service.update(5L, cambios);

        assertNotNull(actualizado);
        assertEquals("Nuevo", actualizado.getNombre());
        assertEquals(2000.0, actualizado.getPrecio());

        ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
        verify(repository).save(captor.capture());
        assertEquals("Nuevo", captor.getValue().getNombre());
    }

    @Test
    @DisplayName("update no existente retorna null")
    void testUpdateWhenNotExists() {
        when(repository.findById(123L)).thenReturn(Optional.empty());
        assertNull(service.update(123L, new Producto()));
        verify(repository).findById(123L);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("deleteById delega en repository.deleteById")
    void testDeleteById() {
        service.deleteById(8L);
        verify(repository).deleteById(8L);
    }
}