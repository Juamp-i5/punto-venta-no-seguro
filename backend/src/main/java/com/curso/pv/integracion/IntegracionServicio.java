package com.curso.pv.integracion;

import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class IntegracionServicio {

    private static final Logger log = LoggerFactory.getLogger(IntegracionServicio.class);

    private final RestTemplate cliente = new RestTemplate();

    @Value("${app.integraciones.dominios-permitidos:}")
    private List<String> dominiosPermitidos = new ArrayList<>();

    public Map<String, Object> consultarUrl(String url) {
        Map<String, Object> respuesta = new HashMap<>();

        log.info("Consultando URL externa solicitada por el cliente: {}", url);

        if (!destinoPermitido(url)) {
            respuesta.put("url", url);
            respuesta.put("estado", "RECHAZADO");
            respuesta.put("error", "Destino no permitido");
            return respuesta;
        }

        try {
            String cuerpo = cliente.getForObject(url, String.class);

            respuesta.put("url", url);
            respuesta.put("estado", "OK");
            // VULNERABILIDAD: se devuelve el contenido completo de la respuesta interna
            respuesta.put("contenido", cuerpo);
            respuesta.put("longitud", cuerpo != null ? cuerpo.length() : 0);
        } catch (Exception error) {
            respuesta.put("url", url);
            respuesta.put("estado", "ERROR");
            // VULNERABILIDAD: se filtra el detalle del error, que revela la topologia interna
            respuesta.put("error", error.getMessage());
        }

        return respuesta;
    }

    /**
     * VULNERABILIDAD (SSRF con metodo POST): misma vulnerabilidad que consultarUrl,
     * pero permite enviar un cuerpo y cabeceras al servicio interno.
     */
    public Map<String, Object> enviarPost(String url, Map<String, Object> cuerpo) {
        Map<String, Object> respuesta = new HashMap<>();

        log.info("POST a URL externa solicitada por el cliente: {}", url);

        try {
            String resultado = cliente.postForObject(url, cuerpo, String.class);
            respuesta.put("url", url);
            respuesta.put("estado", "OK");
            respuesta.put("contenido", resultado);
        } catch (Exception error) {
            respuesta.put("url", url);
            respuesta.put("estado", "ERROR");
            respuesta.put("error", error.getMessage());
        }

        return respuesta;
    }

    /**
     * VULNERABILIDAD: verifica si un host responde, lo que sirve para escanear
     * puertos y descubrir servicios internos de la red del contenedor.
     */
    public Map<String, Object> verificarHost(String host, Integer puerto) {
        Map<String, Object> respuesta = new HashMap<>();
        String url = "http://" + host + ":" + puerto + "/";

        try {
            cliente.getForObject(url, String.class);
            respuesta.put("host", host);
            respuesta.put("puerto", puerto);
            respuesta.put("abierto", true);
        } catch (Exception error) {
            respuesta.put("host", host);
            respuesta.put("puerto", puerto);
            respuesta.put("abierto", false);
            respuesta.put("error", error.getMessage());
        }

        return respuesta;
    }

    private boolean destinoPermitido(String url) {
        try {
            URI uri = new URI(url);
            String esquema = uri.getScheme();
            String host = uri.getHost();
            if (esquema == null || host == null) {
                return false;
            }
            if (!esquema.equalsIgnoreCase("http") && !esquema.equalsIgnoreCase("https")) {
                return false;
            }
            if (dominiosPermitidos == null || !dominiosPermitidos.contains(host.toLowerCase())) {
                return false;
            }
            for (InetAddress ip : InetAddress.getAllByName(host)) {
                if (ip.isLoopbackAddress()          
                        || ip.isSiteLocalAddress()  
                        || ip.isLinkLocalAddress()  
                        || ip.isAnyLocalAddress()) { 
                    return false;
                }
            }
            return true;
        } catch (Exception error) {
            return false;
        }
    }
}