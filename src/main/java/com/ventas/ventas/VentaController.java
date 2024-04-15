package com.ventas.ventas;

import com.ventas.ventas.Models.Producto;
import com.ventas.ventas.Models.Venta;
import com.ventas.ventas.repositories.ProductoRepository;
import com.ventas.ventas.repositories.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.lang.Number;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.time.format.DateTimeParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@RestController
@RequestMapping("/api")
public class VentaController {
    private static final Logger logger = LoggerFactory.getLogger(VentaController.class);

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
    // creacion de ventas
    @PostMapping(value = "/ventas", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createVenta(@RequestBody Venta nuevaVenta) {
        System.out.println("Received venta: " + nuevaVenta);

        if (nuevaVenta.getProductos() == null || nuevaVenta.getProductos().isEmpty()) {
            return ResponseEntity.badRequest().body("Venta debe tener al menos un producto.");
        }

        List<Producto> productos = productoRepository.findAllById(
            nuevaVenta.getProductos().stream().map(Producto::getId).collect(Collectors.toList())
        );

        if (productos.size() != nuevaVenta.getProductos().size()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("uno 0 mas productos no encontrados.");
        }

        nuevaVenta.setProductos(productos);
        Venta savedVenta = ventaRepository.save(nuevaVenta);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedVenta);
    }
    //actualizar venta añadir o eliminar productos
    @PutMapping(value = "/ventas/{id}/updateFecha", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> updateFechaVenta(@PathVariable Long id, @RequestBody Map<String, String> body) {
        logger.info("Solicitud recibida para actualizar fecha de la venta con ID: {} con datos: {}", id, body);
        
        if (!body.containsKey("fechaVenta")) {
            logger.error("Falta el campo 'fechaVenta' en el cuerpo de la solicitud.");
            return ResponseEntity.badRequest().body("Falta el campo 'fechaVenta'.");
        }
        
        LocalDate nuevaFecha;
        try {
            nuevaFecha = LocalDate.parse(body.get("fechaVenta"));
            logger.info("Fecha parseada correctamente: {}", nuevaFecha);
        } catch (DateTimeParseException e) {
            logger.error("Error al parsear 'fechaVenta': {}", body.get("fechaVenta"), e);
            return ResponseEntity.badRequest().body("Formato de fecha inválido.");
        }

        return ventaRepository.findById(id).map(ventaExistente -> {
            ventaExistente.setFechaVenta(nuevaFecha);
            ventaRepository.save(ventaExistente);
            logger.info("Fecha de venta actualizada exitosamente para la venta ID: {}", id);
            return ResponseEntity.ok(ventaExistente);
        }).orElseGet(() -> {
            logger.warn("No se encontró la venta con ID: {}", id);
            return ResponseEntity.notFound().build();
        });
    }



    // Endpoint para eliminar una venta
    @DeleteMapping("/ventas/{id}")
    public ResponseEntity<?> deleteVenta(@PathVariable Long id) {
        return ventaRepository.findById(id).map(venta -> {
            // Asegura la eliminación de relaciones en la tabla intermedia si es necesario
            venta.getProductos().clear(); // Limpiar la relación antes de eliminar para evitar problemas de integridad referencial
            ventaRepository.save(venta);  // Guardar el estado de la entidad
            
            ventaRepository.delete(venta); // Eliminar la venta
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping(value = "/ventas/{id}/updateVenta", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> updateVenta(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        logger.info("Solicitud recibida para actualizar la venta con ID: {} con datos: {}", id, body);
        
        return ventaRepository.findById(id).map(ventaExistente -> {
            // Procesamiento y actualización de la fecha si está presente
            if (body.containsKey("fechaVenta")) {
                LocalDate nuevaFecha;
                try {
                    nuevaFecha = LocalDate.parse(body.get("fechaVenta").toString());
                    ventaExistente.setFechaVenta(nuevaFecha);
                } catch (DateTimeParseException e) {
                    return ResponseEntity.badRequest().body("Formato de fecha inválido.");
                }
            }

            // Actualización de los productos si están presentes
            if (body.containsKey("productos")) {
                List<Long> productIds = ((List<?>) body.get("productos")).stream()
                    .map(obj -> Long.valueOf(obj.toString()))
                    .collect(Collectors.toList());
                List<Producto> productosActualizados = productoRepository.findAllById(productIds);
                ventaExistente.setProductos(productosActualizados);
            }

            // Guarda los cambios en la base de datos
            ventaRepository.save(ventaExistente);
            return ResponseEntity.ok(ventaExistente);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }


}
