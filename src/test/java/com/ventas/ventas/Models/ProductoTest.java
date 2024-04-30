
package com.ventas.ventas.Models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

public class ProductoTest {
     @Test
    public void testProductoCreation() {
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Café");
        producto.setPrecio(new BigDecimal("19.99"));

        assertEquals(1L, producto.getId());
        assertEquals("Café", producto.getNombre());
        assertEquals(new BigDecimal("19.99"), producto.getPrecio());
    }
}
