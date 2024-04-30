package com.ventas.ventas.controllers;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;

import com.ventas.ventas.Models.Producto;
import com.ventas.ventas.Models.Venta;
import com.ventas.ventas.repositories.ProductoRepository;
import com.ventas.ventas.repositories.VentaRepository;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.time.format.DateTimeParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




@RestController
@RequestMapping("/api")
public class VentaController     {
    private static final Logger logger = LoggerFactory.getLogger(VentaController.class);

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private VentaRepository ventaRepository;

    // Endpoint para listar todos los productos
   @GetMapping("/productos")
    public ResponseEntity<CollectionModel<EntityModel<Producto>>> getProductos() {
        List<EntityModel<Producto>> productos = productoRepository.findAll().stream()
            .map(producto -> EntityModel.of(producto,
                linkTo(methodOn(VentaController.class).getProductos()).withSelfRel(),
                linkTo(methodOn(VentaController.class).getVentasPorProductoId(producto.getId())).withRel("ventas-por-producto")))
            .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(productos,
            linkTo(methodOn(VentaController.class).getProductos()).withSelfRel()));
    }

    // Endpoint para obtener una venta específica por ID
    @GetMapping("/ventas/{id}")
    public ResponseEntity<?> getVentaPorId(@PathVariable Long id) {
        return ventaRepository.findById(id)
            .map(venta -> {
                EntityModel<Venta> resource = EntityModel.of(venta);
                resource.add(linkTo(methodOn(VentaController.class).getVentaPorId(id)).withSelfRel());
                resource.add(linkTo(methodOn(VentaController.class).deleteVenta(id)).withRel("delete-venta"));
                resource.add(linkTo(methodOn(VentaController.class).updateVenta(id, new HashMap<>())).withRel("update-venta"));
                return ResponseEntity.ok(resource);
            })
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Endpoint para obtener ventas de un producto específico por ID
    @GetMapping("/ventas/producto/{productoId}")
    public ResponseEntity<CollectionModel<EntityModel<Venta>>> getVentasPorProductoId(@PathVariable Long productoId) {
        List<EntityModel<Venta>> ventas = ventaRepository.findByProductoId(productoId).stream()
            .map(venta -> EntityModel.of(venta,
                linkTo(methodOn(VentaController.class).getVentasPorProductoId(productoId)).withSelfRel(),
                linkTo(methodOn(VentaController.class).getVentaPorId(venta.getId())).withRel("detalle-venta")))
            .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(ventas,
            linkTo(methodOn(VentaController.class).getVentasPorProductoId(productoId)).withSelfRel()));
    }

    // Endpoint para las ventas anuales
    @GetMapping("/ventaanual/{anioInicio}-{anioFin}")
    public ResponseEntity<CollectionModel<EntityModel<Venta>>> getVentasAnuales(@PathVariable int anioInicio, @PathVariable int anioFin) {
        List<EntityModel<Venta>> ventas = ventaRepository.findAll().stream()
            .filter(venta -> venta.getFechaVenta().getYear() >= anioInicio && venta.getFechaVenta().getYear() <= anioFin)
            .map(venta -> EntityModel.of(venta,
                linkTo(methodOn(VentaController.class).getVentasAnuales(anioInicio, anioFin)).withSelfRel()))
            .collect(Collectors.toList());
    
        return ResponseEntity.ok(CollectionModel.of(ventas,
            linkTo(methodOn(VentaController.class).getVentasAnuales(anioInicio, anioFin)).withSelfRel()));
    }

    // Endpoint para las ventas mensuales
    @GetMapping("/ventamensual/{anio}-{mesInicio}-{anioFin}-{mesFin}")
    public ResponseEntity<CollectionModel<EntityModel<Venta>>> getVentasMensuales(@PathVariable int anio, @PathVariable int mesInicio, @PathVariable int anioFin, @PathVariable int mesFin) {
        List<EntityModel<Venta>> ventas = ventaRepository.findAll().stream()
            .filter(venta -> {
                int ventaAnio = venta.getFechaVenta().getYear();
                int ventaMes = venta.getFechaVenta().getMonthValue();
                return (ventaAnio > anio || (ventaAnio == anio && ventaMes >= mesInicio)) &&
                       (ventaAnio < anioFin || (ventaAnio == anioFin && ventaMes <= mesFin));
            })
            .map(venta -> EntityModel.of(venta,
                linkTo(methodOn(VentaController.class).getVentasMensuales(anio, mesInicio, anioFin, mesFin)).withSelfRel()))
            .collect(Collectors.toList());
    
        return ResponseEntity.ok(CollectionModel.of(ventas,
            linkTo(methodOn(VentaController.class).getVentasMensuales(anio, mesInicio, anioFin, mesFin)).withSelfRel()));
    }

    // Endpoint para las ventas diarias
    @GetMapping("/ventadiaria/{anio1}-{mes1}-{dia1}-{anio2}-{mes2}-{dia2}")
    public ResponseEntity<CollectionModel<EntityModel<Venta>>> getVentasDiarias(@PathVariable int anio1, @PathVariable int mes1, @PathVariable int dia1, @PathVariable int anio2, @PathVariable int mes2, @PathVariable int dia2) {
        LocalDate inicio = LocalDate.of(anio1, mes1, dia1);
        LocalDate fin = LocalDate.of(anio2, mes2, dia2);
    
        List<EntityModel<Venta>> ventas = ventaRepository.findAll().stream()
            .filter(venta -> !venta.getFechaVenta().isBefore(inicio) && !venta.getFechaVenta().isAfter(fin))
            .map(venta -> EntityModel.of(venta,
                linkTo(methodOn(VentaController.class).getVentasDiarias(anio1, mes1, dia1, anio2, mes2, dia2)).withSelfRel()))
            .collect(Collectors.toList());
    
        return ResponseEntity.ok(CollectionModel.of(ventas,
            linkTo(methodOn(VentaController.class).getVentasDiarias(anio1, mes1, dia1, anio2, mes2, dia2)).withSelfRel()));
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("uno o más productos no encontrados.");
        }

        nuevaVenta.setProductos(productos);
        Venta savedVenta = ventaRepository.save(nuevaVenta);

        EntityModel<Venta> resource = EntityModel.of(savedVenta,
            linkTo(methodOn(VentaController.class).createVenta(nuevaVenta)).withSelfRel(),
            linkTo(methodOn(VentaController.class).getVentaPorId(savedVenta.getId())).withRel("detalle-venta"),
            linkTo(methodOn(VentaController.class).deleteVenta(savedVenta.getId())).withRel("delete-venta"),
            linkTo(methodOn(VentaController.class).updateVenta(savedVenta.getId(), new HashMap<>())).withRel("update-venta")
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(resource);
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

        EntityModel<Venta> resource = EntityModel.of(ventaExistente,
            linkTo(methodOn(VentaController.class).updateFechaVenta(id, body)).withSelfRel(),
            linkTo(methodOn(VentaController.class).getVentaPorId(id)).withRel("detalle-venta"),
            linkTo(methodOn(VentaController.class).deleteVenta(id)).withRel("delete-venta")
        );

        return ResponseEntity.ok(resource);
    }).orElseGet(() -> {
        logger.warn("No se encontró la venta con ID: {}", id);
        return ResponseEntity.notFound().build();
    });
    }




     @DeleteMapping("/ventas/{id}")
    public ResponseEntity<?> deleteVenta(@PathVariable Long id) {
        return ventaRepository.findById(id).map(venta -> {
            venta.getProductos().clear(); // Limpiar la relación antes de eliminar para evitar problemas de integridad referencial
            ventaRepository.save(venta); // Guardar el estado de la entidad
            ventaRepository.delete(venta); // Eliminar la venta

            EntityModel<Map<String, String>> resource = EntityModel.of(
                Map.of("message", "Venta eliminada"), // Crear el contenido del cuerpo
                linkTo(methodOn(VentaController.class).getProductos()).withRel("listar-productos"), // Enlace a listar productos
                linkTo(methodOn(VentaController.class).createVenta(new Venta())).withRel("crear-venta") // Enlace para crear una nueva venta
            );

            return ResponseEntity.ok()
                .header(HttpHeaders.LOCATION, linkTo(methodOn(VentaController.class).getProductos()).toUri().toString())
                .body(resource);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping(value = "/ventas/{id}/updateVenta", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> updateVenta(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        logger.info("Solicitud recibida para actualizar la venta con ID: {} con datos: {}", id, body);
        
        return ventaRepository.findById(id).map(ventaExistente -> {
            if (body.containsKey("fechaVenta")) {
                try {
                    LocalDate nuevaFecha = LocalDate.parse(body.get("fechaVenta").toString());
                    ventaExistente.setFechaVenta(nuevaFecha);
                } catch (DateTimeParseException e) {
                    return ResponseEntity.badRequest().body("Formato de fecha inválido.");
                }
            }

            if (body.containsKey("productos")) {
                List<Long> productIds = ((List<?>) body.get("productos")).stream()
                    .map(obj -> Long.valueOf(obj.toString()))
                    .collect(Collectors.toList());
                List<Producto> productosActualizados = productoRepository.findAllById(productIds);
                if (productosActualizados.size() != productIds.size()) {
                    return ResponseEntity.badRequest().body("Algunos productos no fueron encontrados.");
                }
                ventaExistente.setProductos(productosActualizados);
            }

            ventaRepository.save(ventaExistente);

            EntityModel<Venta> resource = EntityModel.of(ventaExistente,
                linkTo(methodOn(VentaController.class).updateVenta(id, body)).withSelfRel(),
                linkTo(methodOn(VentaController.class).getVentaPorId(id)).withRel("detalle-venta"),
                linkTo(methodOn(VentaController.class).deleteVenta(id)).withRel("eliminar-venta"),
                linkTo(methodOn(VentaController.class).getProductos()).withRel("listar-productos"));
            
            return ResponseEntity.ok(resource);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }


}
