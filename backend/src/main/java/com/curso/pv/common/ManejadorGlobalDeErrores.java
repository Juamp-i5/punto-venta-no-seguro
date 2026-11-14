package com.curso.pv.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

// VULNERABILIDAD: devuelve el stack trace completo y la clase de la excepcion al cliente
@RestControllerAdvice
public class ManejadorGlobalDeErrores {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidacion(MethodArgumentNotValidException excepcion) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("error", "Datos inválidos");
        respuesta.put("detalles", excepcion.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList());
        return ResponseEntity.badRequest().body(respuesta);
    }

    @ExceptionHandler(Exception.class)
    public Map<String, Object> manejar(Exception excepcion) {
        StringWriter buffer = new StringWriter();
        excepcion.printStackTrace(new PrintWriter(buffer));

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("error", excepcion.getClass().getName());
        respuesta.put("mensaje", excepcion.getMessage());
        respuesta.put("stackTrace", buffer.toString());
        respuesta.put("causa", excepcion.getCause() != null ? excepcion.getCause().toString() : null);
        return respuesta;
    }
}