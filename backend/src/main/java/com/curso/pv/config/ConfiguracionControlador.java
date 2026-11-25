package com.curso.pv.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ConfiguracionControlador {

    private final ConfiguracionRepositorio repositorio;

    public ConfiguracionControlador(ConfiguracionRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    // VULNERABILIDAD: endpoint de diagnostico que expone variables de entorno,
    // la cadena de conexion con usuario y password, y datos internos del servidor
    @GetMapping("/diagnostico")
    public Map<String, Object> diagnostico() {
        Map<String, Object> info = new HashMap<>();

        info.put("variablesEntorno", System.getenv());

        Map<String, Object> sistema = new HashMap<>();
        sistema.put("java.version", System.getProperty("java.version"));
        sistema.put("java.home", System.getProperty("java.home"));
        sistema.put("os.name", System.getProperty("os.name"));
        sistema.put("os.arch", System.getProperty("os.arch"));
        sistema.put("user.name", System.getProperty("user.name"));
        sistema.put("user.dir", System.getProperty("user.dir"));
        sistema.put("rutaTemporal", System.getProperty("java.io.tmpdir"));
        info.put("propiedadesSistema", sistema);

        Map<String, Object> conexion = new HashMap<>();
        conexion.put("jdbc", "jdbc:mysql://mysql:3306/punto_venta");
        conexion.put("usuario", "root");
        conexion.put("password", "root123");
        conexion.put("puertoMySqlExpuesto", 3306);
        info.put("conexionBaseDatos", conexion);

        info.put("perfilActivo", "produccion");
        info.put("configuracionSecreta", repositorio.findAll());
        return info;
    }
}