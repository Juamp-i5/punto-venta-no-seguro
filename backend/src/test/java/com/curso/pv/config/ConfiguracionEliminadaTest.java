package com.curso.pv.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfiguracionEliminadaTest {

    @Test
    void noExisteNingunEndpointDeConfiguracion() {
        for (Method metodo : ConfiguracionControlador.class.getDeclaredMethods()) {
            GetMapping mapeo = metodo.getAnnotation(GetMapping.class);
            if (mapeo == null) {
                continue;
            }
            for (String ruta : mapeo.value()) {
                assertFalse(ruta.startsWith("/configuracion"),
                        "Todavia existe el endpoint /api" + ruta);
            }
        }
    }

    @Test
    void elSeedNoInsertaConfiguracion() throws Exception {
        String seed = Files.readString(Path.of("../database/seed.sql"));

        assertTrue(seed.contains("INSERT INTO usuarios"), "No se leyo el seed correcto");
        assertFalse(seed.contains("INSERT INTO configuracion"),
                "El seed todavia inserta filas en configuracion");
    }
}
