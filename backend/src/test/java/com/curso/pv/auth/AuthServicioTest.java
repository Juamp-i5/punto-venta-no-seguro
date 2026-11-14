package com.curso.pv.auth;

import com.curso.pv.log.ServicioAuditoria;
import com.curso.pv.usuario.Usuario;
import com.curso.pv.usuario.UsuarioConsultasInseguras;
import com.curso.pv.usuario.UsuarioRepositorio;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServicioTest {

    @Test
    void registrarSiempreAsignaRolVendedor() {
        UsuarioRepositorio usuarios = mock(UsuarioRepositorio.class);
        UsuarioConsultasInseguras consultas = mock(UsuarioConsultasInseguras.class);
        ServicioAuditoria auditoria = mock(ServicioAuditoria.class);
        when(consultas.porUsername("nuevo")).thenReturn(null);
        when(usuarios.save(any(Usuario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        AuthServicio servicio = new AuthServicio(usuarios, consultas, auditoria);
        RegistroRequestDTO datos = new RegistroRequestDTO("nuevo", "clave", null, null);

        var respuesta = servicio.registrar(datos, mock(HttpServletRequest.class));

        Usuario guardado = (Usuario) respuesta.get("usuario");
        assertEquals("VENDEDOR", guardado.getRol());
    }
}
