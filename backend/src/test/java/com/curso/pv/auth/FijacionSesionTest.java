package com.curso.pv.auth;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FijacionSesionTest {

    @AfterEach
    void limpiarContexto() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    // PRUEBA ROJA SIN DEFENSA: si se quita request.changeSessionId() del login,
    // esta aserción falla porque el identificador previo se conserva.
    @Test
    void loginRotaElIdentificadorDeUnaSesionPreexistente() {
        AuthenticationManager authenticationManager = autenticacion ->
                new UsernamePasswordAuthenticationToken("cajero", null,
                        List.of(new SimpleGrantedAuthority("ROL_VENDEDOR")));
        HttpSessionSecurityContextRepository repository = new HttpSessionSecurityContextRepository();

        AuthControlador controlador = new AuthControlador(authenticationManager, repository, null);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        String idAntesDelLogin = session.getId();

        controlador.login(Map.of("username", "cajero", "password", "prueba"), request);

        assertNotEquals(idAntesDelLogin, request.getSession(false).getId(),
                "El identificador conocido antes del login no debe seguir autenticando la sesión");
    }

    // PRUEBA VERDE DEL CAMINO FELIZ: confirma que un login válido responde 200
    // y devuelve el mensaje de éxito y el nombre del usuario esperado.
    @Test
    void loginValidoSigueFuncionandoYDevuelveLaRespuestaEsperada() {
        AuthenticationManager authenticationManager = autenticacion ->
                new UsernamePasswordAuthenticationToken("cajero", null,
                        List.of(new SimpleGrantedAuthority("ROL_VENDEDOR")));
        HttpSessionSecurityContextRepository repository = new HttpSessionSecurityContextRepository();

        AuthControlador controlador = new AuthControlador(authenticationManager, repository, null);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);

        var respuesta = controlador.login(Map.of("username", "cajero", "password", "correcta"), request);

        assertEquals(200, respuesta.getStatusCode().value());
        Map<?, ?> cuerpo = (Map<?, ?>) respuesta.getBody();
        assertEquals("Sesion iniciada", cuerpo.get("mensaje"));
        assertEquals("cajero", cuerpo.get("username"));
    }
}
