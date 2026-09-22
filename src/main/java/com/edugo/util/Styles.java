package com.edugo.util;

/**
 * Constantes de color y estilo CSS para toda la app.
 *
 * NOTA TÉCNICA (importante para el docente/inge):
 *   Se usa border en lugar de -fx-effect:dropshadow en CARD porque JavaFX
 *   rasteriza todos los nodos hijos cuando el padre tiene -fx-effect, lo que
 *   hace que las Labels con solo -fx-font-weight:bold (sin -fx-text-fill
 *   explícito) queden invisibles (texto transparente).
 *   La solución: borde sutil en lugar de sombra → texto siempre visible.
 */
public class Styles {

    // ── Paleta de colores (origen: diseño propio, tonos Material/Tailwind) ──
    public static final String BLUE         = "#2563EB";  // azul primario (botones, acentos)
    public static final String BLUE_DARK    = "#1E3A8A";  // azul oscuro (no usado actualmente)
    public static final String BLUE_LIGHT   = "#EFF6FF";  // azul muy claro (fondos suaves)
    public static final String BLUE_BORDER  = "#BFDBFE";  // borde azul suave
    public static final String ORANGE       = "#F97316";  // naranja (logo "Go", puntos)
    public static final String GREEN        = "#16A34A";  // verde (correcto, dominado)
    public static final String GREEN_LIGHT  = "#ECFDF5";  // verde claro (fondos de acierto)
    public static final String PURPLE       = "#5B4FE9";  // morado (pasado)
    public static final String PURPLE_LIGHT = "#EEF0FF";  // morado claro (fondos)
    public static final String RED          = "#DC2626";  // rojo (error, incorrecto)
    public static final String RED_LIGHT    = "#FEF2F2";  // rojo claro (fondo de error)
    public static final String YELLOW_LIGHT = "#FFFBEB";  // amarillo claro (revelación de respuesta)
    public static final String YELLOW_TEXT  = "#92400E";  // marrón dorado (texto sobre fondo amarillo)
    public static final String BG           = "#EEF2F9";  // fondo general de la app
    public static final String CARD_BG      = "#FFFFFF";  // fondo de tarjetas
    public static final String TEXT         = "#1E2937";  // texto principal (casi negro)
    public static final String MUTED        = "#6B7280";  // texto secundario (gris medio)
    public static final String BORDER       = "#E2E8F0";  // bordes neutros
    public static final String SURFACE      = "#F8FAFC";  // superficie secundaria

    // ── TARJETA — borde en lugar de sombra (ver nota técnica arriba) ──
    public static final String CARD =
        "-fx-background-color:" + CARD_BG + ";" +
        "-fx-background-radius:16;" +
        "-fx-border-color:#DDE3EE;" +
        "-fx-border-radius:16;" +
        "-fx-border-width:1.5;";

    // ── Botones ───────────────────────────────────────────────────────
    // BTN_PRIMARY: fondo azul, texto blanco — acción principal
    public static final String BTN_PRIMARY =
        "-fx-background-color:" + BLUE + ";" +
        "-fx-text-fill:white;" +
        "-fx-font-weight:bold;" +
        "-fx-font-size:14px;" +
        "-fx-background-radius:12;" +
        "-fx-cursor:hand;" +
        "-fx-padding:12 20 12 20;";

    // BTN_OUTLINE: fondo blanco con borde — acción secundaria (ej. "Listen")
    public static final String BTN_OUTLINE =
        "-fx-background-color:white;" +
        "-fx-text-fill:" + TEXT + ";" +
        "-fx-font-weight:bold;" +
        "-fx-font-size:13px;" +
        "-fx-background-radius:12;" +
        "-fx-border-color:" + BORDER + ";" +
        "-fx-border-radius:12;" +
        "-fx-border-width:1.5;" +
        "-fx-cursor:hand;" +
        "-fx-padding:11 18 11 18;";

    // BTN_SUCCESS: verde — acción positiva (no muy usado actualmente)
    public static final String BTN_SUCCESS =
        "-fx-background-color:" + GREEN + ";" +
        "-fx-text-fill:white;" +
        "-fx-font-weight:bold;" +
        "-fx-font-size:13px;" +
        "-fx-background-radius:10;" +
        "-fx-cursor:hand;" +
        "-fx-padding:10 16 10 16;";

    // ── Campos de texto ───────────────────────────────────────────────
    public static final String INPUT =
        "-fx-background-color:white;" +
        "-fx-border-color:" + BORDER + ";" +
        "-fx-border-radius:10;" +
        "-fx-background-radius:10;" +
        "-fx-border-width:1.5;" +
        "-fx-padding:10 13 10 13;" +
        "-fx-font-size:14px;" +
        "-fx-text-fill:" + TEXT + ";";

    // ── Caja de pronunciación (IPA + respelling) ──────────────────────
    public static final String PHONETIC_BOX =
        "-fx-background-color:" + BLUE_LIGHT + ";" +
        "-fx-border-color:" + BLUE_BORDER + ";" +
        "-fx-border-width:1.5;" +
        "-fx-border-radius:12;" +
        "-fx-background-radius:12;" +
        "-fx-padding:12 16 12 16;";

    // ── Retroalimentación ─────────────────────────────────────────────
    // FEEDBACK_GOOD: fondo verde claro + texto verde → respuesta correcta
    public static final String FEEDBACK_GOOD =
        "-fx-background-color:" + GREEN_LIGHT + ";" +
        "-fx-text-fill:" + GREEN + ";" +
        "-fx-font-weight:bold;" +
        "-fx-font-size:13px;" +
        "-fx-background-radius:12;" +
        "-fx-padding:10 12 10 12;";

    // FEEDBACK_BAD: fondo rojo claro + texto rojo → respuesta incorrecta
    public static final String FEEDBACK_BAD =
        "-fx-background-color:" + RED_LIGHT + ";" +
        "-fx-text-fill:" + RED + ";" +
        "-fx-font-weight:bold;" +
        "-fx-font-size:13px;" +
        "-fx-background-radius:12;" +
        "-fx-padding:10 12 10 12;";

    // FEEDBACK_REVEAL: fondo amarillo + texto marrón → revelar respuesta tras agotar intentos
    public static final String FEEDBACK_REVEAL =
        "-fx-background-color:" + YELLOW_LIGHT + ";" +
        "-fx-text-fill:" + YELLOW_TEXT + ";" +
        "-fx-font-weight:bold;" +
        "-fx-font-size:13px;" +
        "-fx-background-radius:12;" +
        "-fx-padding:10 12 10 12;";

    // ── Chips de palabras (constructor de oraciones) ───────────────────
    // WORD_CHIP: chip disponible en el banco (no seleccionado)
    public static final String WORD_CHIP =
        "-fx-background-color:white;" +
        "-fx-text-fill:" + TEXT + ";" +
        "-fx-border-color:" + BORDER + ";" +
        "-fx-border-radius:10;" +
        "-fx-background-radius:10;" +
        "-fx-border-width:1;" +
        "-fx-padding:8 14 8 14;" +
        "-fx-font-size:14px;" +
        "-fx-font-weight:bold;" +
        "-fx-cursor:hand;";

    // WORD_CHIP_SELECTED: chip ya elegido por el estudiante (fondo azul)
    public static final String WORD_CHIP_SELECTED =
        "-fx-background-color:" + BLUE + ";" +
        "-fx-text-fill:white;" +
        "-fx-border-color:" + BLUE + ";" +
        "-fx-border-radius:10;" +
        "-fx-background-radius:10;" +
        "-fx-border-width:1;" +
        "-fx-padding:8 14 8 14;" +
        "-fx-font-size:14px;" +
        "-fx-font-weight:bold;" +
        "-fx-cursor:hand;";

    // ── Botones de selección de tiempo verbal ─────────────────────────
    // TENSE_BTN_INACTIVE: tiempo no seleccionado
    public static final String TENSE_BTN_INACTIVE =
        "-fx-background-color:white;" +
        "-fx-text-fill:" + MUTED + ";" +
        "-fx-border-color:" + BORDER + ";" +
        "-fx-border-radius:10;" +
        "-fx-background-radius:10;" +
        "-fx-border-width:1;" +
        "-fx-padding:10 8 10 8;" +
        "-fx-font-size:12px;" +
        "-fx-font-weight:bold;" +
        "-fx-cursor:hand;";

    // TENSE_BTN_ACTIVE: tiempo seleccionado actualmente (fondo azul)
    public static final String TENSE_BTN_ACTIVE =
        "-fx-background-color:" + BLUE + ";" +
        "-fx-text-fill:white;" +
        "-fx-border-color:" + BLUE + ";" +
        "-fx-border-radius:10;" +
        "-fx-background-radius:10;" +
        "-fx-border-width:1;" +
        "-fx-padding:10 8 10 8;" +
        "-fx-font-size:12px;" +
        "-fx-font-weight:bold;" +
        "-fx-cursor:hand;";

    // ── Barra de progreso ─────────────────────────────────────────────
    // PROGRESS_BAR_BG: fondo gris claro de la barra
    public static final String PROGRESS_BAR_BG =
        "-fx-background-color:#E5E9F2;" +
        "-fx-background-radius:8;" +
        "-fx-min-height:9;" +
        "-fx-max-height:9;";

    // PROGRESS_BAR_FILL: relleno azul que crece según el % de progreso
    public static final String PROGRESS_BAR_FILL =
        "-fx-background-color:" + BLUE + ";" +
        "-fx-background-radius:8;" +
        "-fx-min-height:9;" +
        "-fx-max-height:9;";
}
