package com.curso.pv.producto;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.curso.pv.common.ManejadorGlobalDeErrores;

class ProductoPrecioNegativoHttpTest {

    @Test
    void productoConPrecioNegativoDevuelve400() throws Exception {
        ProductoServicio servicio = mock(ProductoServicio.class);
        MockMvc mvc = MockMvcBuilders
                .standaloneSetup(new ProductoControlador(servicio))
                .setControllerAdvice(new ManejadorGlobalDeErrores())
                .build();

        mvc.perform(put("/api/productos/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"CD-001\",\"nombre\":\"Arroz 5kg\","
                                + "\"precio\":-1,\"costo\":18.50,\"stock\":35,\"stockMinimo\":10}"))
                .andExpect(status().isBadRequest());

        verify(servicio, never()).actualizar(anyLong(), any(Producto.class));
    }
}
