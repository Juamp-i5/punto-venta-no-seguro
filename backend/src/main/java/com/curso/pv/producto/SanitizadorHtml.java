package com.curso.pv.producto;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * DEFENSA (XSS): deja pasar solo el HTML seguro de una allowlist. Elimina
 * etiquetas como script, img, iframe y atributos como onerror u onload, pero
 * conserva el formato basico (b, strong, p, ul, li, br, etc.).
 */
public final class SanitizadorHtml {

    private static final Safelist PERMITIDO = Safelist.basic();

    private SanitizadorHtml() {
    }

    public static String limpiar(String html) {
        return html == null ? null : Jsoup.clean(html, PERMITIDO);
    }
}
