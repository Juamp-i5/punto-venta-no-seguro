package com.curso.pv.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.curso.pv.common.ManejadorGlobalDeErrores;
import com.curso.pv.log.ServicioAuditoria;
import com.curso.pv.usuario.Usuario;
import com.curso.pv.usuario.UsuarioConsultasInseguras;
import com.curso.pv.usuario.UsuarioRepositorio;

class RegistroMassAssignmentHttpTest {

    @Test
    void registroConRolAdminQuedaComoVendedor() throws Exception {
        UsuarioRepositorio usuarios = mock(UsuarioRepositorio.class);
        when(usuarios.save(any(Usuario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
        AuthServicio servicio = new AuthServicio(usuarios, mock(UsuarioConsultasInseguras.class),
                mock(ServicioAuditoria.class));

        MockMvc mvc = MockMvcBuilders
                .standaloneSetup(new AuthControlador(mock(AuthenticationManager.class),
                        new HttpSessionSecurityContextRepository(), servicio))
                .setControllerAdvice(new ManejadorGlobalDeErrores())
                .build();

        mvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"atacante\",\"password\":\"123\",\"rol\":\"ADMIN\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).save(guardado.capture());
        assertEquals("VENDEDOR", guardado.getValue().getRol());
    }
}
