package com.ventas.ventas;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class Venta {
    
    private Long id;
    private List<Producto> productos; // Lista de productos
    private LocalDate fechaVenta;
    
    // Constructor
    public Venta(Long id, List<Producto> productos, LocalDate fechaVenta) {
        this.id = id;
        this.productos = productos;
        this.fechaVenta = fechaVenta;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public List<Producto> getProductos() { // Actualizado para devolver la lista de productos
        return productos;
    }

    public LocalDate getFechaVenta() {
        return fechaVenta;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setProductos(List<Producto> productos) { // Actualizado para aceptar una lista de productos
        this.productos = productos;
    }

    public void setFechaVenta(LocalDate fechaVenta) {
        this.fechaVenta = fechaVenta;
    }

    // Método para obtener el total de la venta sumando los precios de todos los productos
    public BigDecimal getTotalVenta() {
        return productos.stream()
                        .map(Producto::getPrecio)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}