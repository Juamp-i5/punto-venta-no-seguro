# Reporte datos sensibles

Requests de la colección de Postman `Vulnerabilidades` › `Datos sensibles`, contra `http://localhost:5173`.

## 01 · Stack trace en errores

| | |
|---|---|
| Archivo:línea | `common/ManejadorGlobalDeErrores.java:14-24`, `application.yml:4-6` |
| Impacto | Revela clases internas y framework; facilita otros ataques |
| STRIDE / OWASP | Information Disclosure · A10 Mishandling of Exceptional Conditions |
| Severidad | **Media**: sin sesión, pero no entrega secretos |

```json
{ "method": "POST", "url": "http://localhost:5173/api/auth/login",
  "body": "{\"username\":\"x\",\"password\":\"y\"}" }
```

Respuesta: `200` · `{"error":"...BadCredentialsException","stackTrace":"at com.curso.pv..."}`

## 02 · `/api/configuracion` devuelve secretos

| | |
|---|---|
| Archivo:línea | `config/ConfiguracionControlador.java:26-31` |
| Impacto | Claves de la pasarela, contraseña del correo y secreto JWT |
| STRIDE / OWASP | Information Disclosure · A02 Security Misconfiguration |
| Severidad | **Crítica**: sin sesión. |

```json
{ "method": "GET", "url": "http://localhost:5173/api/configuracion" }
```

Respuesta: `[{"clave":"pasarela.api.key","valor":"sk_live_..."},{"clave":"jwt.secret",...}]`

## 03 · `/api/diagnostico` y `/variables` vuelcan el entorno

| | |
|---|---|
| Archivo:línea | `config/ConfiguracionControlador.java:41-79` |
| Impacto | Usuario y contraseña root de MySQL: control total de la base |
| STRIDE / OWASP | Information Disclosure · A02 Security Misconfiguration |
| Severidad | **Crítica**: sin sesión |

```json
{ "method": "GET", "url": "http://localhost:5173/api/diagnostico" }
{ "method": "GET", "url": "http://localhost:5173/api/configuracion/variables" }
```

Respuesta: `{"conexionBaseDatos":{"usuario":"root","password":"root123"},"variablesEntorno":{...}}`

## 04 · El login devuelve la contraseña y el sessionId

| | |
|---|---|
| Archivo:línea | `auth/AuthControlador.java:59-68`, `frontend/src/auth/ContextoAuth.jsx:25` |
| Impacto | Quedan en `localStorage`; un XSS secuestra la cuenta |
| STRIDE / OWASP | Information Disclosure · A04 Cryptographic Failures |
| Severidad | **Alta**: requiere un XSS o acceso al navegador |

```json
{ "method": "POST", "url": "http://localhost:5173/api/auth/login",
  "body": "{\"username\":\"admin\",\"password\":\"admin\"}" }
```

Respuesta: `{"credenciales":"admin","sessionId":"55DFF623..."}`

## 05 · Secretos guardados en el navegador

| | |
|---|---|
| Archivo:línea | `frontend/src/pages/Diagnostico.jsx:49-60` |
| Impacto | Secretos de la pasarela, JWT y correo en el equipo de la caja |
| STRIDE / OWASP | Information Disclosure · A04 Cryptographic Failures |
| Severidad | **Alta**: requiere un XSS o acceso al navegador |

Pasos: abrir `/diagnostico` › DevTools › Application › Local Storage `pv_cfg`, IndexedDB `pv_cache`, cookie `pv_mail` (base64).

## 06 · `/api/usuarios` devuelve contraseñas

| | |
|---|---|
| Archivo:línea | `usuario/Usuario.java:16-17` |
| Impacto | Contraseñas de todos los usuarios, incluidos ADMIN |
| STRIDE / OWASP | Information Disclosure · A04 Cryptographic Failures |
| Severidad | **Crítica**: sin sesión |

```json
{ "method": "GET", "url": "http://localhost:5173/api/usuarios" }
```

Respuesta: `[{"username":"admin","password":"admin","rol":"ADMIN"},...]`

## 07 · Pagos devuelven tarjeta y CVV

| | |
|---|---|
| Archivo:línea | `pago/PagoServicio.java:89-101`, `pago/PagoControlador.java:33-41` |
| Impacto | Tarjetas de clientes: fraude e incumplimiento de PCI DSS |
| STRIDE / OWASP | Information Disclosure · A04 Cryptographic Failures |
| Severidad | **Crítica**: sin sesión |

```json
{ "method": "POST", "url": "http://localhost:5173/api/pagos/procesar",
  "body": "{\"ventaId\":\"4\",\"metodo\":\"TARJETA\",\"titular\":\"a\",\"numeroTarjeta\":\"a\",\"cvv\":\"123\",\"fechaVencimiento\":\"12\",\"monto\":\"12\"}" }
{ "method": "GET", "url": "http://localhost:5173/api/pagos" }
{ "method": "GET", "url": "http://localhost:5173/api/pagos/venta/1" }
```

Respuesta: `{"numeroTarjeta":"a","cvv":"123","apiKeyUtilizada":"sk_live_..."}`

## 08 · Credenciales de la pasarela expuestas

| | |
|---|---|
| Archivo:línea | `pago/PagoControlador.java:48-62` |
| Impacto | Permiten cobrar o reembolsar a nombre de la tienda |
| STRIDE / OWASP | Information Disclosure · A02 Security Misconfiguration |
| Severidad | **Crítica**: sin sesión |

```json
{ "method": "GET", "url": "http://localhost:5173/api/pagos/configuracion" }
```

Respuesta: `{"apiSecret":"..."}` · cabecera `Set-Cookie: pv_gw=sk_live_...`

## 09 · `/api/auth/sesion` expone la sesión

| | |
|---|---|
| Archivo:línea | `auth/AuthControlador.java:100-111` |
| Impacto | Id de sesión y versión de Tomcat; facilita otros ataques |
| STRIDE / OWASP | Information Disclosure · A02 Security Misconfiguration |
| Severidad | **Media**: útil solo combinado con otro ataque |

```json
{ "method": "GET", "url": "http://localhost:5173/api/auth/sesion",
  "header": "Cookie: PVSESSION=55DFF62313D31200809B8A1DD3F586D8" }
```

Respuesta: `{"sessionId":"55DFF623...","servidor":"Apache Tomcat/11.x"}`
