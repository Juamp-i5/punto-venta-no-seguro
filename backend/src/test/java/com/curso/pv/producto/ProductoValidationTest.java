package com.curso.pv.producto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductoValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rechazaPrecioYStockNegativos() {
        Producto producto = productoValido();
        producto.setPrecio(new BigDecimal("-0.01"));
        producto.setStock(-1);

        var errores = validator.validate(producto);

        assertTrue(errores.stream().anyMatch(error -> error.getPropertyPath().toString().equals("precio")));
        assertTrue(errores.stream().anyMatch(error -> error.getPropertyPath().toString().equals("stock")));
    }

    @Test
    void aceptaPrecioYStockNoNegativos() {
        assertFalse(validator.validate(productoValido()).iterator().hasNext());
    }

    private Producto productoValido() {
        Producto producto = new Producto();
        producto.setSku("SKU-1");
        producto.setNombre("Producto");
        producto.setPrecio(BigDecimal.ZERO);
        producto.setCosto(BigDecimal.ZERO);
        producto.setStock(0);
        producto.setStockMinimo(0);
        return producto;
    }
}
