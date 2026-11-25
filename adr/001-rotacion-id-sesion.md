# ADR 001: Rotar el identificador de sesión al iniciar sesión

- **Estado:** Aceptada
- **Fecha:** 2026-11-17
- **Área:** Autenticación y gestión de sesiones

## Contexto

El endpoint personalizado de login autentica al usuario y guarda el contexto de seguridad en una sesión HTTP. Si ya existía una sesión antes de autenticar, su identificador podía conservarse. Un atacante que consiguiera fijar un identificador conocido antes del login podría intentar reutilizarlo después de que la víctima se autenticara.

Spring Security puede rotar el identificador de sesión tras autenticarse, pero el login de esta aplicación se implementa en un controlador propio y no utiliza el filtro de formulario estándar. Por eso la estrategia configurada en la cadena de seguridad por sí sola no cubre ese flujo personalizado.

## Decisión

Rotar el identificador de la sesión existente después de autenticar correctamente al usuario y antes de guardar el contexto de seguridad. También se configura `changeSessionId` como estrategia de protección de Spring Security para los flujos de autenticación gestionados por sus filtros.

La rotación se aplica solo si ya existe una sesión. Si no existe, el repositorio de contexto crea la sesión necesaria para persistir la autenticación.

## Alternativas consideradas

1. **Conservar el identificador actual:** descartada porque permite que un identificador conocido antes del login siga asociado a una sesión autenticada.
2. **Depender únicamente de la estrategia de Spring Security:** descartada porque el controlador personalizado autentica manualmente y no pasa por el filtro estándar que aplica esa estrategia.
3. **Invalidar siempre la sesión y crear otra:** no elegida porque `changeSessionId` rota el identificador sin descartar los atributos válidos de la sesión existente.

## Consecuencias

- Un identificador de sesión anterior al login deja de servir como identificador de la sesión autenticada.
- Los atributos existentes en la sesión se conservan durante la rotación.
- `FijacionSesionTest` comprueba que el ID cambia en el flujo del controlador personalizado y que el login válido conserva su respuesta exitosa.
- Se verificó el ciclo rojo → verde: sin la rotación explícita falla la prueba de ID; con la defensa activa pasan ambas pruebas.
- Esta decisión reduce el riesgo de fijación de sesión, pero no protege por sí sola la cookie contra lectura desde JavaScript, envío por HTTP o solicitudes desde otros sitios.

## Referencias

- `backend/src/main/java/com/curso/pv/auth/AuthControlador.java`
- `backend/src/main/java/com/curso/pv/config/SecurityConfig.java`
- `backend/src/test/java/com/curso/pv/auth/FijacionSesionTest.java`
