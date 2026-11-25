# Reporte sesiones y cookies

Request de inicio de sesión contra `http://localhost:5173`, usando una cookie `PVSESSION` obtenida antes de autenticar. Hallazgo limitado a la fijación del identificador de sesión.

## 01 · U2-SES-01 — El ID de sesión no se rotaba al iniciar sesión

| | |
|---|---|
| Archivo:línea | `backend/src/main/java/com/curso/pv/config/SecurityConfig.java:33`; `backend/src/main/java/com/curso/pv/auth/AuthControlador.java:43-57` |
| Impacto | Si un atacante logra que la víctima use un ID de sesión que conoce, podría reutilizarlo después de que la víctima se autentique y actuar con su cuenta en el POS. |
| STRIDE / OWASP | Spoofing · A07 Identification and Authentication Failures (OWASP Top 10:2021) |
| Severidad | **Media**: el acceso de la víctima tendría impacto, pero el atacante primero debe conseguir que use un identificador que conoce. |

### Pasos reproducibles

1. Levantar el POS y abrir Postman en `http://localhost:5173`.
2. Enviar `POST /api/auth/fijar-sesion?sessionId=prueba`. La ruta crea una sesión y la respuesta incluye el ID actual. Guardar también la cookie `PVSESSION` que devuelve el servidor.
3. Enviar el request de login mostrado abajo con esa cookie y credenciales válidas de prueba.
4. Comparar el ID previo con el nuevo ID de `PVSESSION` en `Set-Cookie`. Antes del arreglo el identificador podía conservarse; con el arreglo debe cambiar.

Request de login enviado con el ID de sesión existente:

```json
{
  "method": "POST",
  "url": "http://localhost:5173/api/auth/login",
  "headers": { "Cookie": "PVSESSION=<id-previo-al-login>" },
  "body": "{\"username\":\"<usuario-de-prueba>\",\"password\":\"<contrasena-de-prueba>\"}"
}
```

**Antes del fix:** el ID previo podía seguir siendo el identificador de la sesión autenticada.

**Después del fix (respuesta esperada):** `200` · la cabecera `Set-Cookie` establece un ID `PVSESSION` distinto del enviado. El backend rota el ID en el controlador porque este login personalizado no pasa por el filtro estándar de autenticación de Spring Security.

### Demo rojo → verde

- **Rojo sin defensa:** comentar temporalmente el bloque `request.changeSessionId()` en `AuthControlador.login` y ejecutar desde `backend` `./mvnw -Dtest=FijacionSesionTest#loginRotaElIdentificadorDeUnaSesionPreexistente -DforkCount=0 test`. Resultado comprobado: falla la aserción porque el ID sigue siendo `1`.
- **Verde con defensa:** restaurar el bloque y ejecutar `./mvnw -Dtest=FijacionSesionTest -DforkCount=0 test`. Resultado comprobado: pasan las dos pruebas, `Tests run: 2, Failures: 0, Errors: 0`.
- **Camino feliz:** `loginValidoSigueFuncionandoYDevuelveLaRespuestaEsperada` verifica que unas credenciales válidas sigan produciendo HTTP `200`, el mensaje `Sesion iniciada` y el usuario `cajero`.

La prueba roja es una demostración temporal: no se debe dejar comentada la defensa ni conservar una prueba que falle en la rama final.

**Corrección aplicada:** `SecurityConfig.java` usa `changeSessionId()`; `AuthControlador.java` rota explícitamente el ID preexistente después de autenticar correctamente.

**Riesgo residual:** este hallazgo no cubre la configuración `HttpOnly`, `Secure` o `SameSite` de la cookie, los secretos hardcodeados ni otras vulnerabilidades del POS.
