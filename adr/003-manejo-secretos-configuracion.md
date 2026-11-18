# ADR 003: Sacar los secretos del código a un archivo .env y rotarlos

- **Estado:** Propuesta
- **Fecha:** 2026-11-17
- **Área:** Datos sensibles y configuración

## Contexto

Los secretos están escritos en el código y en archivos versionados: clave y secreto de la pasarela, secreto JWT y contraseña del correo en `application.yml` y `PagoServicio.java`, y `root123` de MySQL en `docker-compose.yml` y `application.yml`. Además la tabla `configuracion` los guarda en claro y varios endpoints los devolvían. Como el repositorio está en GitHub, todos estos valores ya son públicos en el historial de git.

## Decisión

1. Mover todos los secretos a un archivo **`.env`** (ya ignorado en `.gitignore`), que `docker-compose.yml` carga como variables de entorno y `application.yml` lee con `${NOMBRE_VARIABLE}`. Se versiona solo un `.env.example` con valores vacíos.
2. **Rotar** todas las llaves expuestas (pasarela, JWT, correo, contraseña de MySQL), porque quitarlas del código no las borra del historial.

## Alternativas consideradas

1. **Dejar los secretos en `application.yml`:** descartada; cualquiera con acceso al repositorio los obtiene.
2. **Gestor de secretos (Vault, AWS Secrets Manager):** más seguro, pero excesivo para el alcance y la infraestructura del proyecto.
3. **Cifrar los secretos en la base:** descartada; la llave de cifrado tendría que vivir en algún lado y el problema se repite.
4. **Reescribir el historial de git para borrarlos:** no basta; las copias y clones existentes conservan los valores, por eso se rotan.

## Consecuencias

- El código deja de contener secretos; cada entorno define los suyos.
- Cada integrante necesita crear su `.env` a partir de `.env.example` para levantar el proyecto.
- Las llaves antiguas quedan inválidas tras la rotación, aunque sigan visibles en el historial.
- Riesgo residual: el `.env` queda en claro en el equipo que ejecuta la aplicación.