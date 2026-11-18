package com.curso.pv.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ConfiguracionControladorTest {

    private static final String[] CLAVES_SECRETAS = {
            "pasarela.api.key", "pasarela.secret", "correo.password", "jwt.secret"
    };

    private static final String[] VALORES_SECRETOS = {
            "sk_live_9f8a7b6c5d4e3f2a1b0c9d8e7f6a5b4c",
            "s3cr3t-pasarela-que-nunca-deberia-estar-aqui",
            "correo-admin-2024",
            "clave-jwt-insegura-de-larga-utilizada-para-firmar"
    };

    private static final String[] CLAVES_PUBLICAS = {
            "tienda.nombre", "tienda.direccion", "impuesto.porcentaje"
    };

    private ConfiguracionControlador controlador;

    @BeforeEach
    void preparar() {
        ConfiguracionRepositorio repositorio = mock(ConfiguracionRepositorio.class);
        ConfiguracionConsultasInseguras consultas = mock(ConfiguracionConsultasInseguras.class);

        // Mismas filas que database/seed.sql
        List<Configuracion> filas = new ArrayList<>();
        filas.add(fila("pasarela.api.key", "sk_live_9f8a7b6c5d4e3f2a1b0c9d8e7f6a5b4c"));
        filas.add(fila("pasarela.secret", "s3cr3t-pasarela-que-nunca-deberia-estar-aqui"));
        filas.add(fila("correo.admin", "admin@puntoventa.local"));
        filas.add(fila("correo.password", "correo-admin-2024"));
        filas.add(fila("impuesto.porcentaje", "19.00"));
        filas.add(fila("tienda.nombre", "Supermercado El Vendedor"));
        filas.add(fila("tienda.direccion", "Av. Principal 1234, Ciudad Internacional"));
        filas.add(fila("jwt.secret", "clave-jwt-insegura-de-larga-utilizada-para-firmar"));
        filas.add(fila("session.timeout.dias", "30"));
        when(repositorio.findAll()).thenReturn(filas);

        controlador = new ConfiguracionControlador(repositorio, consultas);
    }

    @Test
    void noDevuelveClavesSecretas() {
        for (Configuracion c : controlador.listar()) {
            for (String secreta : CLAVES_SECRETAS) {
                assertFalse(c.getClave().equals(secreta),
                        "Se expuso la clave secreta: " + secreta);
            }
        }
    }

    @Test
    void noDevuelveValoresSecretos() {
        for (Configuracion c : controlador.listar()) {
            for (String secreto : VALORES_SECRETOS) {
                assertFalse(c.getValor().equals(secreto),
                        "Se expuso el valor secreto de " + c.getClave());
            }
        }
    }

    @Test
    void sigueDevolviendoAjustesPublicosDeLaTienda() {
        List<Configuracion> respuesta = controlador.listar();

        for (String publica : CLAVES_PUBLICAS) {
            boolean encontrada = false;
            for (Configuracion c : respuesta) {
                if (c.getClave().equals(publica)) {
                    encontrada = true;
                }
            }
            assertTrue(encontrada, "Falta el ajuste publico: " + publica);
        }
    }

    private static Configuracion fila(String clave, String valor) {
        Configuracion c = new Configuracion();
        c.setClave(clave);
        c.setValor(valor);
        return c;
    }
}
