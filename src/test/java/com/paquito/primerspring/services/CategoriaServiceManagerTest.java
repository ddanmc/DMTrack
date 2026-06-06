package com.paquito.primerspring.services;

import com.paquito.primerspring.models.Categoria;
import com.paquito.primerspring.repositories.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoriaServiceManagerTest {

    @Mock
    private CategoriaRepository repository;

    @InjectMocks
    private CategoriaServiceManager service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAllVacio() {
        when(repository.findAll()).thenReturn(Collections.emptyList());
        List<Categoria> result = service.findAll();
        assertTrue(result.isEmpty());
        verify(repository).findAll();
    }

    @Test
    @DisplayName("findById existente")
    void testFindById() {
        Categoria c = new Categoria();
        c.setId(3L);   // Long
        c.setNombre("Papelería");
        when(repository.findById(3L)).thenReturn(Optional.of(c));

        Categoria r = service.findById(3L);
        assertNotNull(r);
        assertEquals("Papelería", r.getNombre());
        verify(repository).findById(3L);
    }

    @Test
    @DisplayName("save delega en repository.save")
    void testSave() {
        Categoria c = new Categoria();
        c.setNombre("Oficina");
        when(repository.save(c)).thenReturn(c);

        Categoria r = service.save(c);
        assertEquals("Oficina", r.getNombre());
        verify(repository).save(c);
    }

    @Test
    @DisplayName("update existente")
    void testUpdateExists() {
        Categoria existente = new Categoria();
        existente.setId(9L);   // Long
        existente.setNombre("Old");
        existente.setDescripcion("Desc");

        Categoria cambios = new Categoria();
        cambios.setNombre("New");
        cambios.setDescripcion("Nueva desc");

        when(repository.findById(9L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));

        Categoria r = service.update(9L, cambios);
        assertEquals("New", r.getNombre());
        assertEquals("Nueva desc", r.getDescripcion());
        verify(repository).save(any(Categoria.class));
    }

    @Test
    @DisplayName("update no existente retorna null")
    void testUpdateNotExists() {
        when(repository.findById(77L)).thenReturn(Optional.empty());
        assertNull(service.update(77L, new Categoria()));
        verify(repository, never()).save(any());
    }

    @Test
    void testDeleteById() {
        service.deleteById(4L);
        verify(repository).deleteById(4L);
    }
}