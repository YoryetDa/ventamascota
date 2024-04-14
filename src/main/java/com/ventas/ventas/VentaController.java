package com.ventas.ventas;

import com.ventas.ventas.Models.Producto;
import com.ventas.ventas.Models.Venta;
import com.ventas.ventas.repositories.ProductoRepository;
import com.ventas.ventas.repositories.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class VentaController {
    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private VentaRepository ventaRepository;

    // Endpoint para listar todos los productos
    @GetMapping("/productos")
    public List<Producto> getProductos() {
        return productoRepository.findAll();
    }

    // Endpoint para obtener una venta específica por ID
    @GetMapping("/ventas/{id}")
    public ResponseEntity<?> getVentaPorId(@PathVariable Long id) {
        return ventaRepository.findById(id)
            .map(venta -> {
                BigDecimal totalVenta = venta.getTotalVenta();
                Map<String, Object> response = new HashMap<>();
                response.put("venta", venta);
                response.put("total", totalVenta);
                return ResponseEntity.ok(response);
            })
            .orElse(new ResponseEntity<>(Map.of("error", "La venta con el ID " + id + " no existe."), HttpStatus.NOT_FOUND));
    }

    // Endpoint para obtener ventas de un producto específico por ID
    @GetMapping("/ventas/producto/{productoId}")
    public List<Venta> getVentasPorProductoId(@PathVariable Long productoId) {
        return ventaRepository.findByProductoId(productoId);
    }

    // Endpoint para las ventas anuales
    @GetMapping("/ventaanual/{anioInicio}-{anioFin}")
    public List<Venta> getVentasAnuales(@PathVariable int anioInicio, @PathVariable int anioFin) {
        return ventaRepository.findAll().stream()
                .filter(venta -> venta.getFechaVenta().getYear() >= anioInicio && venta.getFechaVenta().getYear() <= anioFin)
                .collect(Collectors.toList());
    }

    // Endpoint para las ventas mensuales
    @GetMapping("/ventamensual/{anio}-{mesInicio}-{anioFin}-{mesFin}")
    public List<Venta> getVentasMensuales(@PathVariable int anio, @PathVariable int mesInicio, @PathVariable int anioFin, @PathVariable int mesFin) {
        return ventaRepository.findAll().stream()
                .filter(venta -> {
                    int ventaAnio = venta.getFechaVenta().getYear();
                    int ventaMes = venta.getFechaVenta().getMonthValue();
                    return (ventaAnio > anio || (ventaAnio == anio && ventaMes >= mesInicio)) &&
                           (ventaAnio < anioFin || (ventaAnio == anioFin && ventaMes <= mesFin));
                })
                .collect(Collectors.toList());
    }

    // Endpoint para las ventas diarias
    @GetMapping("/ventadiaria/{anio1}-{mes1}-{dia1}-{anio2}-{mes2}-{dia2}")
    public List<Venta> getVentasDiarias(@PathVariable int anio1, @PathVariable int mes1, @PathVariable int dia1, 
                                        @PathVariable int anio2, @PathVariable int mes2, @PathVariable int dia2) {
        LocalDate inicio = LocalDate.of(anio1, mes1, dia1);
        LocalDate fin = LocalDate.of(anio2, mes2, dia2);
        
        return ventaRepository.findAll().stream()
                .filter(venta -> !venta.getFechaVenta().isBefore(inicio) && !venta.getFechaVenta().isAfter(fin))
                .collect(Collectors.toList());
    }

    // Endpoint para crear una nueva venta
    @PostMapping("/ventas")
    public ResponseEntity<Venta> createVenta(@RequestBody Venta nuevaVenta) {
        Venta savedVenta = ventaRepository.save(nuevaVenta);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedVenta);
    }

    // Endpoint para actualizar una venta existente
    @PutMapping("/ventas/{id}")
    public ResponseEntity<Venta> updateVenta(@PathVariable Long id, @RequestBody Venta ventaDetalles) {
        return ventaRepository.findById(id)
            .map(venta -> {
                venta.setProductos(ventaDetalles.getProductos());
                venta.setFechaVenta(ventaDetalles.getFechaVenta());
                ventaRepository.save(venta);
                return ResponseEntity.ok(venta);
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Endpoint para eliminar una venta
    @DeleteMapping("/ventas/{id}")
    public ResponseEntity<?> deleteVenta(@PathVariable Long id) {
        return ventaRepository.findById(id)
            .map(venta -> {
                ventaRepository.delete(venta);
                return ResponseEntity.ok().build();
            })
            .orElse(ResponseEntity.notFound().build());
    }
}
