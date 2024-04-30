package com.ventas.ventas.Models;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class VentaTest {
      @Test
    public void testVentaTotalCalculation() {
        Producto producto1 = new Producto();
        producto1.setPrecio(new BigDecimal("15.00"));
        Producto producto2 = new Producto();
        producto2.setPrecio(new BigDecimal("25.00"));

        Venta venta = new Venta();
        venta.setProductos(Arrays.asList(producto1, producto2));

        assertEquals(new BigDecimal("40.00"), venta.getTotalVenta());
    }
}
