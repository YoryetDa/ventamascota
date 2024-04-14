package com.ventas.ventas.repositories;

import com.ventas.ventas.Models.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Long> {
    @Query("SELECT v FROM Venta v JOIN v.productos p WHERE p.id = :productoId")
    List<Venta> findByProductoId(Long productoId);
}