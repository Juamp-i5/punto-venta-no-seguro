# Reporte cifrado en reposo

Requests de la colección de Postman `Cifrado en reposo`, contra `http://localhost:5173`. Se confirma en Adminer (`http://localhost:8081`, servidor `mysql`, `root/root123`).

## 01 · Contraseñas guardadas sin hash

| | |
|---|---|
| Archivo:línea | `security/CodificadorEnClaro.java:17-24`, `config/SecurityConfig.java:68`, `auth/AuthServicio.java:53`, `usuario/UsuarioServicio.java:95` |
| Impacto | Quien lea la base obtiene todas las contraseñas y las reutiliza en otros servicios |
| STRIDE / OWASP | Information Disclosure, Spoofing · A04 Cryptographic Failures |
| Severidad | **Crítica**: la base es accesible (credenciales root expuestas) y no hay ninguna protección |

```json
{ "method": "POST", "url": "http://localhost:5173/api/auth/registro",
  "body": "{\"username\":\"as\",\"password\":\"a\",\"nombre\":\"a\",\"email\":\"prueba@puntoventa.local\"}" }
{ "method": "GET", "url": "http://localhost:5173/api/usuarios" }
{ "method": "POST", "url": "http://localhost:5173/api/usuarios/1/password?password=hola" }
```

Respuesta: `{"usuario":{"username":"as","password":"a",...}}` · `GET /api/usuarios` → `{"username":"admin","password":"hola"}`
Adminer: `SELECT username, password FROM usuarios;` → contraseñas en claro.

## 02 · Se guarda el CVV

| | |
|---|---|
| Archivo:línea | `pago/Pago.java:35-36`, `pago/PagoServicio.java:75`, `database/init.sql:129` |
| Impacto | Con PAN + CVV se pueden hacer compras en línea; PCI DSS prohíbe guardarlo aun cifrado |
| STRIDE / OWASP | Information Disclosure · A04 Cryptographic Failures |
| Severidad | **Crítica**: dato de autenticación de la tarjeta, sin protección |

```json
{ "method": "POST", "url": "http://localhost:5173/api/pagos/procesar",
  "body": "{\"ventaId\":2,\"metodo\":\"TARJETA\",\"titular\":\"Cliente Ficticio\",\"numeroTarjeta\":\"4111111111111111\",\"cvv\":\"123\",\"fechaVencimiento\":\"12/30\",\"monto\":\"100\"}" }
{ "method": "GET", "url": "http://localhost:5173/api/pagos/venta/1" }
```

Respuesta: `[{"titular":"MARIA GOMEZ","numeroTarjeta":"4111111111111111","cvv":"123",...}]`
Adminer: `SELECT titular, numero_tarjeta, cvv FROM pagos;`

## 03 · Número de tarjeta completo sin cifrar

| | |
|---|---|
| Archivo:línea | `pago/Pago.java:31-32`, `pago/PagoServicio.java:74`, `database/init.sql:128` |
| Impacto | Robo de tarjetas de clientes, incumplimiento de PCI DSS y multas |
| STRIDE / OWASP | Information Disclosure · A04 Cryptographic Failures |
| Severidad | **Crítica**: tarjetas completas legibles en la base |

Mismos requests que 02. Respuesta: `"numeroTarjeta":"4111111111111111"` (también `5555555555554444`, `4222222222222` del seed).

## 04 · Secretos sin cifrar en la base y en el código

| | |
|---|---|
| Archivo:línea | `database/seed.sql:102-110`, `application.yml:26,51-55`, `pago/PagoServicio.java:24-25`, `docker-compose.yml:11` |
| Impacto | Claves de la pasarela, JWT, correo y root de MySQL legibles por quien vea la base o el repositorio |
| STRIDE / OWASP | Information Disclosure, Elevation of Privilege · A04 Cryptographic Failures |
| Severidad | **Alta**: requiere acceso a la base o al código (aquí ambos están expuestos) |

```json
{ "method": "GET", "url": "http://localhost:5173/api/diagnostico" }
```

Respuesta: `{"configuracionSecreta":[{"clave":"pasarela.secret","valor":"s3cr3t-..."}],"conexionBaseDatos":{"password":"root123"}}`
Adminer: `SELECT clave, valor FROM configuracion;`
