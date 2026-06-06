package com.paquito.primerspring.controllers.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.paquito.primerspring.models.Producto;
import com.paquito.primerspring.services.ProductoServiceManager;
import com.paquito.primerspring.services.CategoriaServiceManager;
import com.paquito.primerspring.services.WeatherService;
import com.paquito.primerspring.utils.WeatherUtils;

import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

import java.util.*;

@Controller
@RequestMapping("/productos")
public class ProductoViewController {

    private final ProductoServiceManager productoService;
    private final CategoriaServiceManager categoriaService;
    private final WeatherService weatherService;

    public ProductoViewController(ProductoServiceManager productoService,
                                  CategoriaServiceManager categoriaService,
                                  WeatherService weatherService) {
        this.productoService = productoService;
        this.categoriaService = categoriaService;
        this.weatherService = weatherService;
    }

    @GetMapping
    public String listarProductos(@RequestParam(name = "city", required = false) String city,
                                  @RequestParam(name = "forceRain", required = false) Boolean forceRain,
                                  Model model,
                                  @ModelAttribute("mensaje") String mensaje,
                                  @ModelAttribute("error") String error) {

        List<Producto> productos = productoService.findAll();

        // --- Stats básicos ---
        int totalProductos = productos.size();
        double sumaPrecios = productos.stream().mapToDouble(Producto::getPrecio).sum();
        double promedioPrecios = totalProductos > 0 ? sumaPrecios / totalProductos : 0;

        model.addAttribute("titulo", "Lista de productos");
        model.addAttribute("productos", productos);
        model.addAttribute("mensaje", mensaje);
        model.addAttribute("error", error);
        model.addAttribute("totalProductos", totalProductos);
        model.addAttribute("sumaPrecios", sumaPrecios);
        model.addAttribute("promedioPrecios", promedioPrecios);

        // -------- Ubicación (con fallback Bogotá) ----------
        double lat = WeatherService.DEF_LAT;
        double lon = WeatherService.DEF_LON;
        String where = WeatherService.DEF_NAME;

        if (city != null && !city.isBlank()) {
            var results = weatherService.buscarCiudad(city);
            if (results != null && !results.isEmpty()) {
                var first = results.get(0);
                Object olat = first.get("lat");
                Object olon = first.get("lon");
                if (olat instanceof Number && olon instanceof Number) {
                    lat = ((Number) olat).doubleValue();
                    lon = ((Number) olon).doubleValue();
                    where = first.get("name") + ", " + first.get("country");
                }
            }
        }

        // -------- Clima actual ----------
        String wxCond = "N/D";
        String wxTime = "--";
        Object wxTemp = "--";
        Object wxWind = "--";
        Integer code = null;

        Map<String, Object> clima = weatherService.obtenerClima(lat, lon);
        if (clima != null) {
            Object t = clima.get("temperature");
            Object w = clima.get("windspeed");
            Object wc = clima.get("weathercode");
            Object tm = clima.get("time");

            if (t != null) wxTemp = t;
            if (w != null) wxWind = w;
            if (wc instanceof Number n) code = n.intValue();
            if (tm != null) wxTime = String.valueOf(tm);

            wxCond = WeatherUtils.getCondition(code);
        }

        model.addAttribute("wxTemp", wxTemp);
        model.addAttribute("wxWind", wxWind);
        model.addAttribute("wxCond", wxCond);
        model.addAttribute("wxTime", wxTime);
        model.addAttribute("wxWhere", where);
        model.addAttribute("forceRain", forceRain != null && forceRain);

        // -------- Pronóstico 3 días (prob. lluvia y viento) ----------
        List<Map<String, Object>> daily = weatherService.pronosticoDiario(lat, lon, 3);
        if (daily == null) daily = Collections.emptyList();
        model.addAttribute("daily", daily);

        // Día 0 (hoy) para reglas
        Integer prob0 = null; // %
        Integer wind0 = null; // km/h
        if (!daily.isEmpty()) {
            Map<String, Object> d0 = daily.get(0);
            if (d0 != null) {
                Object p0 = d0.get("prob");
                Object w0 = d0.get("wind");
                if (p0 instanceof Number p) prob0 = p.intValue();
                if (w0 instanceof Number wmax) wind0 = wmax.intValue();
            }
        }

        // -------- Serie horaria de temperatura (24h) ----------
        Map<String, List<?>> hourly = weatherService.horarioTemperatura(lat, lon, 24);
        List<?> hourLabels = hourly.getOrDefault("times", java.util.Collections.emptyList());
        List<?> hourTemps  = hourly.getOrDefault("temps", java.util.Collections.emptyList());
        model.addAttribute("hourLabels", hourLabels);
        model.addAttribute("hourTemps",  hourTemps);

        // -------- Riesgo por producto (score) ----------
        boolean rainy = WeatherUtils.isRainy(code);
        if (Boolean.TRUE.equals(forceRain)) rainy = true; // modo demo

        int climateSeverity = 0;
        if (rainy) climateSeverity += 2;
        if (prob0 != null && prob0 >= 70) climateSeverity += 1;
        if (wind0 != null) {
            if (wind0 >= 60) climateSeverity += 2;
            else if (wind0 >= 40) climateSeverity += 1;
        }

        Map<Long, String> riesgoPorProducto = new HashMap<>();
        for (Producto p : productos) {
            int score = climateSeverity;

            String cat = (p.getCategoria() != null && p.getCategoria().getNombre() != null)
                    ? p.getCategoria().getNombre()
                    : "";

            // Pesos por categoría
            if (cat.equalsIgnoreCase("Electrónica") || cat.equalsIgnoreCase("Tecnología")) {
                score += 2;
            } else if (cat.equalsIgnoreCase("Mobiliario")) {
                score += 1;
            }

            String riesgo;
            if (score >= 3)      riesgo = "Alto";
            else if (score >= 2) riesgo = "Medio";
            else                 riesgo = "Bajo";

            Long id = (p.getId() != null) ? p.getId() : -System.identityHashCode(p) * 1L;
            riesgoPorProducto.put(id, riesgo);
        }
        model.addAttribute("riesgoPorProducto", riesgoPorProducto);

        return "productos";
    }

    // ------- CRUD -------

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("titulo", "Nuevo Producto");
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", categoriaService.findAll());
        return "form-producto";
    }

    @PostMapping("/guardar")
    public String guardarProducto(@Valid @ModelAttribute Producto producto, BindingResult result,
                                  Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("categorias", categoriaService.findAll());
            return "form-producto";
        }
        boolean esNuevo = (producto.getId() == null);
        productoService.save(producto);
        redirect.addFlashAttribute("mensaje",
                esNuevo ? "Producto creado exitosamente." : "Producto actualizado exitosamente.");
        return "redirect:/productos";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Producto producto = productoService.findById(id);
        if (producto == null) return "redirect:/productos";
        model.addAttribute("titulo", "Editar Producto");
        model.addAttribute("producto", producto);
        model.addAttribute("categorias", categoriaService.findAll());
        return "form-producto";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            productoService.deleteById(id);
            redirect.addFlashAttribute("mensaje", "Producto eliminado correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "No se pudo eliminar el producto.");
        }
        return "redirect:/productos";
    }
}