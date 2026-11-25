# ADR 002: Almacenar las contraseñas con BCrypt

- **Estado:** Propuesta
- **Fecha:** 2026-11-17
- **Área:** Cifrado en reposo

## Contexto

Las contraseñas se guardan en texto plano: `CodificadorEnClaro` devuelve la contraseña sin cambios y compara con `equals`. Cualquiera que lea la tabla `usuarios` (Adminer, un respaldo o las credenciales root expuestas en `/api/diagnostico`) obtiene todas las contraseñas y puede reutilizarlas en otros servicios.

## Decisión

Hashear las contraseñas con **BCrypt** (`BCryptPasswordEncoder` de Spring Security) en lugar de `CodificadorEnClaro`, al registrar, crear, actualizar y cambiar la contraseña. Las contraseñas del `seed.sql` se guardan ya hasheadas.

Se hashea y no se cifra porque el sistema nunca necesita recuperar la contraseña, solo compararla.

## Alternativas consideradas

1. **Cifrar con AES:** descartada; es reversible y la llave viviría en el mismo servidor que se quiere proteger.
2. **SHA-256 sin sal:** descartada; es demasiado rápido y vulnerable a tablas precalculadas.
3. **Argon2:** más robusto, pero requiere una dependencia extra; BCrypt ya viene en Spring Security y cumple con sal por usuario y costo configurable.

## Consecuencias

- Una fuga de la base ya no entrega contraseñas utilizables directamente.
- El login es un poco más lento por el costo de BCrypt (intencional).
- Las contraseñas actuales en claro dejan de funcionar: hay que regenerar el seed y recrear la base (`docker compose down -v`).
- BCrypt solo considera los primeros 72 bytes de la contraseña.
