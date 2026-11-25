package com.curso.pv.producto;

import com.curso.pv.log.ServicioAuditoria;
import com.curso.pv.security.UsuarioActual;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductoServicioXssTest {

    private static final String PAYLOAD =
            "<img src=x onerror=alert(document.cookie)><script>alert(1)</script><b>Promocion 2x1</b>";

    private final ProductoRepositorio productos = mock(ProductoRepositorio.class);
    private final ProductoServicio servicio = new ProductoServicio(
            productos,
            mock(ProductoConsultasInseguras.class),
            mock(CategoriaRepositorio.class),
            mock(CategoriaConsultasInseguras.class),
            mock(ServicioAuditoria.class),
            mock(UsuarioActual.class));

    // PRUEBA ROJA SIN DEFENSA: si se quita SanitizadorHtml de actualizar, se guarda el <img onerror>.
    @Test
    void actualizarSaneaLaDescripcionAntesDeGuardar() {
        when(productos.findById(3L)).thenReturn(Optional.of(new Producto()));
        when(productos.save(any(Producto.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Producto datos = new Producto();
        datos.setDescripcion(PAYLOAD);

        Producto guardado = servicio.actualizar(3L, datos);

        assertFalse(guardado.getDescripcion().contains("onerror"));
        assertFalse(guardado.getDescripcion().contains("<img"));
        assertTrue(guardado.getDescripcion().contains("<b>Promocion 2x1</b>"));
    }

    // PRUEBA ROJA SIN DEFENSA: cubre filas ya almacenadas con payload (seed o BD existente).
    @Test
    void listarSaneaDescripcionesYaAlmacenadas() {
        Producto legado = new Producto();
        legado.setDescripcion(PAYLOAD);
        when(productos.findAll()).thenReturn(List.of(legado));

        List<Producto> resultado = servicio.listar();

        assertFalse(resultado.get(0).getDescripcion().contains("onerror"));
        assertFalse(resultado.get(0).getDescripcion().contains("<script"));
    }
}
