package com.curso.pv.config;

import com.curso.pv.auth.AuthControlador;
import com.curso.pv.auth.AuthServicio;
import com.curso.pv.reporte.ReporteControlador;
import com.curso.pv.reporte.ReporteServicio;
import com.curso.pv.security.CodificadorEnClaro;
import com.curso.pv.security.UsuariosDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = ControlDeAccesoTest.ConfiguracionDePrueba.class)
class ControlDeAccesoTest {

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        FilterChainProxy cadenaDeSeguridad =
                contexto.getBean("springSecurityFilterChain", FilterChainProxy.class);
        mvc = MockMvcBuilders.webAppContextSetup(contexto)
                .addFilters(cadenaDeSeguridad)
                .build();
    }

    @Test
    void sinSesionConfiguracionDevuelve401() throws Exception {
        mvc.perform(get("/api/configuracion"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sinSesionDiagnosticoDevuelve401() throws Exception {
        mvc.perform(get("/api/diagnostico"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sinSesionEstructuraBaseDatosDevuelve401() throws Exception {
        mvc.perform(get("/api/reportes/base-datos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clienteAutenticadoNoAccedeALaApi() throws Exception {
        MockHttpSession sesion = iniciarSesion("cliente", "cliente");

        mvc.perform(get("/api/configuracion").session(sesion))
                .andExpect(status().isForbidden());
    }

    private MockHttpSession iniciarSesion(String username, String password) throws Exception {
        String cuerpo = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";

        MvcResult resultado = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession sesion = (MockHttpSession) resultado.getRequest().getSession(false);
        assertNotNull(sesion, "El login debe crear una sesion");
        return sesion;
    }

    @Configuration
    @EnableWebMvc
    @Import({SecurityConfig.class, AuthControlador.class, ConfiguracionControlador.class,
            ReporteControlador.class})
    static class ConfiguracionDePrueba {

        @Bean
        UsuariosDetailsService usuariosDetailsService() {
            UsuariosDetailsService servicio = mock(UsuariosDetailsService.class);
            when(servicio.loadUserByUsername("vendedor")).thenReturn(
                    User.withUsername("vendedor")
                            .password("vendedor")
                            .authorities(CodificadorEnClaro.autoridadesDe("VENDEDOR"))
                            .build());
            when(servicio.loadUserByUsername("cliente")).thenReturn(
                    User.withUsername("cliente")
                            .password("cliente")
                            .authorities(CodificadorEnClaro.autoridadesDe("CLIENTE"))
                            .build());
            return servicio;
        }

        @Bean
        ConfiguracionRepositorio configuracionRepositorio() {
            ConfiguracionRepositorio repositorio = mock(ConfiguracionRepositorio.class);
            when(repositorio.findAll()).thenReturn(List.of());
            return repositorio;
        }

        @Bean
        ConfiguracionConsultasInseguras configuracionConsultasInseguras() {
            return mock(ConfiguracionConsultasInseguras.class);
        }

        @Bean
        ReporteServicio reporteServicio() {
            return mock(ReporteServicio.class);
        }

        @Bean
        AuthServicio authServicio() {
            return mock(AuthServicio.class);
        }
    }
}
