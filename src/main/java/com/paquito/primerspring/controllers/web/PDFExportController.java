package com.paquito.primerspring.controllers.web;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import com.paquito.primerspring.models.Producto;
import com.paquito.primerspring.services.ProductoServiceManager;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.awt.*;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class PDFExportController {

    private final ProductoServiceManager productoService;

    public PDFExportController(ProductoServiceManager productoService) {
        this.productoService = productoService;
    }

    @GetMapping("/productos/exportar-pdf")
    public void exportarPDF(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        String fechaActual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String cabecera = "Content-Disposition";
        String valor = "attachment; filename=dmtrack_inventario_" + fechaActual + ".pdf";
        response.setHeader(cabecera, valor);

        List<Producto> productos = productoService.findAll();

        // Cálculos de resumen
        int total = productos.size();
        double suma = productos.stream().mapToDouble(Producto::getPrecio).sum();
        double promedio = total > 0 ? suma / total : 0;
        DecimalFormat formato = new DecimalFormat("#,###.00");

        Document documento = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(documento, response.getOutputStream());

        documento.open();

        // Título principal
        Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.BLUE);
        Paragraph titulo = new Paragraph("📦 DMTrack - Reporte de Inventario", tituloFont);
        titulo.setAlignment(Paragraph.ALIGN_CENTER);
        titulo.setSpacingAfter(10);
        documento.add(titulo);

        // Fecha actual
        Font fechaFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, Color.DARK_GRAY);
        Paragraph fecha = new Paragraph("Generado el: " + fechaActual, fechaFont);
        fecha.setAlignment(Paragraph.ALIGN_RIGHT);
        fecha.setSpacingAfter(20);
        documento.add(fecha);

        // Tabla de productos
        PdfPTable tabla = new PdfPTable(5);
        tabla.setWidthPercentage(100f);
        tabla.setSpacingBefore(10);
        tabla.setWidths(new float[]{2.5f, 3f, 1.5f, 1.5f, 2f});

        escribirEncabezado(tabla);
        escribirDatos(tabla, productos);

        documento.add(tabla);

        // Caja de resumen visual
        PdfPTable resumen = new PdfPTable(1);
        resumen.setWidthPercentage(100f);
        resumen.setSpacingBefore(20);

        PdfPCell resumenCelda = new PdfPCell();
        resumenCelda.setBackgroundColor(new Color(240, 240, 240));
        resumenCelda.setPadding(15);

        String resumenTexto = String.format("""
                🧾 RESUMEN GENERAL

                Total de productos: %d
                Suma de precios: $%s
                Promedio de precios: $%s
                """, total, formato.format(suma), formato.format(promedio));

        Paragraph resumenParrafo = new Paragraph(resumenTexto, FontFactory.getFont(FontFactory.HELVETICA, 12));
        resumenCelda.addElement(resumenParrafo);
        resumen.addCell(resumenCelda);

        documento.add(resumen);

        documento.close();
    }

    private void escribirEncabezado(PdfPTable tabla) {
        PdfPCell celda = new PdfPCell();
        celda.setBackgroundColor(new Color(60, 90, 160));
        celda.setPadding(6);
        Font fuente = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);

        String[] encabezados = {"Nombre", "Descripción", "Precio", "Cantidad", "Categoría"};
        for (String encabezado : encabezados) {
            celda.setPhrase(new Phrase(encabezado, fuente));
            tabla.addCell(celda);
        }
    }

    private void escribirDatos(PdfPTable tabla, List<Producto> productos) {
        Font fuente = FontFactory.getFont(FontFactory.HELVETICA, 10);
        boolean alternarColor = false;

        for (Producto p : productos) {
            Color bg = alternarColor ? new Color(245, 245, 245) : Color.WHITE;

            tabla.addCell(celdaDato(p.getNombre(), fuente, bg));
            tabla.addCell(celdaDato(p.getDescripcion(), fuente, bg));
            tabla.addCell(celdaDato(String.valueOf(p.getPrecio()), fuente, bg));
            tabla.addCell(celdaDato(String.valueOf(p.getCantidad()), fuente, bg));
            tabla.addCell(celdaDato(p.getCategoria() != null ? p.getCategoria().getNombre() : "Sin categoría", fuente, bg));

            alternarColor = !alternarColor;
        }
    }

    private PdfPCell celdaDato(String texto, Font fuente, Color fondo) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setBackgroundColor(fondo);
        celda.setPadding(5);
        return celda;
    }
}

