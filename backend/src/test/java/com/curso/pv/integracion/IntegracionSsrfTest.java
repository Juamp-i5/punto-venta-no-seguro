package com.curso.pv.integracion;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class IntegracionSsrfTest {

    @Test
    void rechazaUrlInternaYNoDevuelveSuContenido() throws Exception {
        HttpServer interno = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        interno.createContext("/", intercambio -> {
            byte[] cuerpo = "SECRETO-INTERNO".getBytes(StandardCharsets.UTF_8);
            intercambio.sendResponseHeaders(200, cuerpo.length);
            intercambio.getResponseBody().write(cuerpo);
            intercambio.close();
        });
        interno.start();

        try {
            MockMvc mvc = MockMvcBuilders
                    .standaloneSetup(new IntegracionControlador(new IntegracionServicio()))
                    .build();

            String urlInterna = "http://localhost:" + interno.getAddress().getPort() + "/";

            mvc.perform(post("/api/integraciones/consultar-url").param("url", urlInterna))
                    .andExpect(jsonPath("$.estado").value("RECHAZADO"))
                    .andExpect(content().string(not(containsString("SECRETO-INTERNO"))));
        } finally {
            interno.stop(0);
        }
    }
}
