package com.curso.pv.pago;

import com.curso.pv.log.ServicioAuditoria;
import com.curso.pv.security.UsuarioActual;
import com.curso.pv.venta.VentaRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PagoServicioTest {

    private PagoRepositorio pagos;
    private ServicioAuditoria auditoria;
    private PagoServicio servicio;

    @BeforeEach
    void preparar() {
        pagos = mock(PagoRepositorio.class);
        auditoria = mock(ServicioAuditoria.class);
        UsuarioActual usuarioActual = mock(UsuarioActual.class);

        when(pagos.save(any(Pago.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        servicio = new PagoServicio(pagos, mock(PagoConsultasInseguras.class),
                mock(VentaRepositorio.class), auditoria, usuarioActual);
    }

    @Test
    void laEntidadPagoNoTieneCampoCvv() {
        for (Field campo : Pago.class.getDeclaredFields()) {
            assertFalse(campo.getName().equalsIgnoreCase("cvv"),
                    "La entidad Pago todavia tiene la columna cvv");
        }
    }

    @Test
    void laRespuestaNoDevuelveElCvv() {
        Map<String, Object> respuesta = servicio.procesar(pagoConTarjeta());

        assertFalse(respuesta.containsKey("cvv"), "La respuesta devuelve el CVV");
    }

    @Test
    void noEscribeElCvvEnElLog() {
        Logger logger = (Logger) LoggerFactory.getLogger(PagoServicio.class);
        ListAppender<ILoggingEvent> capturador = new ListAppender<>();
        capturador.start();
        logger.addAppender(capturador);

        servicio.procesar(pagoConTarjeta());
        logger.detachAppender(capturador);

        for (ILoggingEvent evento : capturador.list) {
            assertFalse(evento.getFormattedMessage().contains("987"),
                    "El CVV se escribio en el log");
        }
    }

    @Test
    void noGuardaElCvvEnLaAuditoria() {
        servicio.procesar(pagoConTarjeta());

        ArgumentCaptor<Object> detalles = ArgumentCaptor.forClass(Object.class);
        verify(auditoria).registrar(any(), any(), anyString(), anyString(), anyLong(), detalles.capture());

        assertFalse(String.valueOf(detalles.getValue()).contains("cvv"),
                "El CVV se guardo en la auditoria");
    }

    @Test
    void sigueGuardandoElPago() {
        servicio.procesar(pagoConTarjeta());

        ArgumentCaptor<Pago> guardado = ArgumentCaptor.forClass(Pago.class);
        verify(pagos).save(guardado.capture());

        assertEquals("Cliente Ficticio", guardado.getValue().getTitular());
        assertEquals("APROBADO", guardado.getValue().getEstado());
    }

    private static Map<String, Object> pagoConTarjeta() {
        Map<String, Object> datos = new HashMap<>();
        datos.put("ventaId", 1);
        datos.put("metodo", "TARJETA");
        datos.put("titular", "Cliente Ficticio");
        datos.put("numeroTarjeta", "4111111111111111");
        datos.put("cvv", "987");
        datos.put("fechaVencimiento", "12/30");
        datos.put("monto", "100");
        return datos;
    }
}
