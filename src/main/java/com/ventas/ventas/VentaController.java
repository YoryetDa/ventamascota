package com.ventas.ventas;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class VentaController {
    private List<Producto> productos = new ArrayList<>();
    private List<Venta> ventas = new ArrayList<>();

    public VentaController() {
        // Inicializar productos
        productos.add(new Producto(1L, "Alimento para perros", new BigDecimal("1990")));
        productos.add(new Producto(2L, "Collar para gatos", new BigDecimal("9990")));
        productos.add(new Producto(3L, "Juguete mordedor", new BigDecimal("5990")));
        productos.add(new Producto(4L, "Cama para mascotas pequeñas", new BigDecimal("24990")));
        productos.add(new Producto(5L, "Rascador para gatos", new BigDecimal("14990")));
        productos.add(new Producto(6L, "Correa retráctil", new BigDecimal("12990")));
        productos.add(new Producto(7L, "Bolsas de residuos", new BigDecimal("2990")));
        productos.add(new Producto(8L, "Champú para perros", new BigDecimal("8990")));
        productos.add(new Producto(9L, "Arena para gatos", new BigDecimal("11990")));
        productos.add(new Producto(10L, "Pienso hipoalergénico", new BigDecimal("29990")));
        // Ventas con 4 productos
        ventas.add(new Venta(1L, Arrays.asList(productos.get(0), productos.get(1), productos.get(2), productos.get(3)), LocalDate.of(2023, 3, 10)));
        ventas.add(new Venta(2L, Arrays.asList(productos.get(4), productos.get(5), productos.get(6), productos.get(7)), LocalDate.of(2023, 3, 15)));
        ventas.add(new Venta(3L, Arrays.asList(productos.get(8), productos.get(9), productos.get(0), productos.get(1)), LocalDate.of(2023, 3, 20)));

        // Ventas con 2 productos
        ventas.add(new Venta(4L, Arrays.asList(productos.get(2), productos.get(3)), LocalDate.of(2023, 4, 5)));
        ventas.add(new Venta(5L, Arrays.asList(productos.get(4), productos.get(5)), LocalDate.of(2023, 4, 10)));

        // Venta con 1 producto
        ventas.add(new Venta(6L, Collections.singletonList(productos.get(6)), LocalDate.of(2023, 4, 15)));
    }
    // http://localhost:8080/api/ventas/1
    @GetMapping("/ventas/{id}")
    public ResponseEntity<?> getVentaPorId(@PathVariable Long id) {
        Venta ventaEncontrada = ventas.stream()
                                    .filter(venta -> venta.getId().equals(id))
                                    .findFirst()
                                    .orElse(null);

        if (ventaEncontrada == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "La venta con el ID " + id + " no existe.");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        } else {
            BigDecimal totalVenta = ventaEncontrada.getTotalVenta();
            Map<String, Object> response = new HashMap<>();
            response.put("venta", ventaEncontrada);
            response.put("total", totalVenta);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }
    // Endpoint para listar todos los productos http://localhost:8080/api/productos
    @GetMapping("/productos")
    public List<Producto> getProductos() {
        return productos;
    }
    // Endpoint para obtener ventas de un producto específico por ID http://localhost:8080/api/ventas/producto/1
    @GetMapping("/ventas/producto/{productoId}")
    public List<Venta> getVentasPorProductoId(@PathVariable Long productoId) {
        return ventas.stream()
                .filter(venta -> venta.getProductos().stream().anyMatch(producto -> producto.getId().equals(productoId)))
                .collect(Collectors.toList());
    }
    
    // Ventas Anuales http://localhost:8080/api/ventaanual/2023-2023
    @GetMapping("/ventaanual/{anioInicio}-{anioFin}")
    public List<Venta> getVentasAnuales(@PathVariable int anioInicio, @PathVariable int anioFin) {
        return ventas.stream()
                .filter(venta -> venta.getFechaVenta().getYear() >= anioInicio && venta.getFechaVenta().getYear() <= anioFin)
                .collect(Collectors.toList());
    }

    // Ventas Mensuales http://localhost:8080/api/ventamensual/2023-3-2023-5
    @GetMapping("/ventamensual/{anio}-{mesInicio}-{anioFin}-{mesFin}")
    public List<Venta> getVentasMensuales(@PathVariable int anio, @PathVariable int mesInicio, @PathVariable int anioFin, @PathVariable int mesFin) {
        return ventas.stream()
                .filter(venta -> {
                    int ventaAnio = venta.getFechaVenta().getYear();
                    int ventaMes = venta.getFechaVenta().getMonthValue();
                    return (ventaAnio > anio || (ventaAnio == anio && ventaMes >= mesInicio)) &&
                        (ventaAnio < anioFin || (ventaAnio == anioFin && ventaMes <= mesFin));
                })
                .collect(Collectors.toList());
    }
    // Ventas Diarias http://localhost:8080/api/ventadiaria/2023-3-10-2023-5-15
    @GetMapping("/ventadiaria/{anio1}-{mes1}-{dia1}-{anio2}-{mes2}-{dia2}")
    public List<Venta> getVentasDiarias(@PathVariable int anio1, @PathVariable int mes1, @PathVariable int dia1, 
                                        @PathVariable int anio2, @PathVariable int mes2, @PathVariable int dia2) {
        LocalDate inicio = LocalDate.of(anio1, mes1, dia1);
        LocalDate fin = LocalDate.of(anio2, mes2, dia2);
        
        return ventas.stream()
                .filter(venta -> !venta.getFechaVenta().isBefore(inicio) && !venta.getFechaVenta().isAfter(fin))
                .collect(Collectors.toList());
    }
}