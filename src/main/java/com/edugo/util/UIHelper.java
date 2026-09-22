package com.edugo.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

/**
 * Componentes reutilizables de UI para toda la app.
 *
 * NOTA para el docente:
 *   Todos los métodos aquí son constructores de nodos JavaFX puros (sin datos externos).
 *   Los datos (textos, colores) se pasan como parámetros desde cada pantalla.
 */
// Clase con componentes reutilizables de UI para toda la aplicación
public class UIHelper {

    // ── Barra superior con flecha de retroceso ─────────────────────────
    // title    → nombre de la app (siempre "EduGo")
    // subtitle → nombre de la pantalla actual (ej. "Flash Cards")
    // onBack   → acción del botón "←" (navegar a la pantalla anterior)
    // Crea la barra superior de la aplicación
    public static HBox topBar(String title, String subtitle, Runnable onBack) {
        Button back = new Button("←");
        back.setStyle(
            "-fx-background-color:#F1F4F9;" +
            "-fx-text-fill:" + Styles.BLUE + ";" +
            "-fx-font-size:16px;" +
            "-fx-background-radius:50;" +
            "-fx-min-width:34;-fx-min-height:34;" +
            "-fx-max-width:34;-fx-max-height:34;" +
            "-fx-cursor:hand;");
        back.setOnAction(e -> onBack.run());

        Label lTitle = new Label(title);
        lTitle.setStyle(
            "-fx-font-weight:bold;-fx-font-size:16px;" +
            "-fx-text-fill:" + Styles.TEXT + ";");
        Label lSub = new Label(subtitle);
        lSub.setStyle("-fx-font-size:11px;-fx-text-fill:" + Styles.MUTED + ";");
        VBox titles = new VBox(1, lTitle, lSub);

        HBox bar = new HBox(10, back, titles);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10, 18, 14, 18));
        bar.setStyle(
            "-fx-border-color:#F1F1F4;-fx-border-width:0 0 1 0;" +
            "-fx-background-color:white;");
        return bar;
    }

    // ── Campo de texto con etiqueta encima ────────────────────────────
    // Crea un campo de texto con su etiqueta
    public static VBox labelledField(String labelText, javafx.scene.control.TextField field) {
        Label lbl = new Label(labelText);
        lbl.setStyle(
            "-fx-font-size:12px;-fx-font-weight:bold;" +
            "-fx-text-fill:" + Styles.MUTED + ";");
        field.setStyle(Styles.INPUT);
        VBox box = new VBox(5, lbl, field);
        return box;
    }

    // ── Barra de progreso estática (fracción 0.0–1.0) ─────────────────
    // Usada cuando el % no cambia después de construir el nodo
    // Crea una barra de progreso con un porcentaje fijo
    public static javafx.scene.layout.StackPane progressBar(double progress) {
        Pane bg = new Pane();
        bg.setStyle(Styles.PROGRESS_BAR_BG);
        bg.setPrefHeight(9);
        bg.setMaxHeight(9);

        Pane fill = new Pane();
        fill.setStyle(Styles.PROGRESS_BAR_FILL);
        fill.setPrefHeight(9);
        fill.setMaxHeight(9);

        javafx.scene.layout.StackPane stack = new javafx.scene.layout.StackPane(bg, fill);
        stack.setAlignment(Pos.CENTER_LEFT);
        fill.prefWidthProperty().bind(bg.widthProperty().multiply(progress));
        return stack;
    }

    // ── Barra de progreso mutable (el caller puede rebindear el fill) ──
    // Retorna: [bg, fill, StackPane]
    // Usado en FlashcardsScreen y ProgressScreen donde el % cambia en tiempo real
    // Crea una barra de progreso cuyo relleno se puede actualizar dinámicamente
    public static Pane[] progressBarMutable() {
        Pane bg = new Pane();
        bg.setStyle(Styles.PROGRESS_BAR_BG);
        bg.setPrefHeight(9);
        bg.setMaxHeight(9);
        bg.setMinHeight(9);

        Pane fill = new Pane();
        fill.setStyle(Styles.PROGRESS_BAR_FILL);
        fill.setPrefHeight(9);
        fill.setMaxHeight(9);
        fill.setMinHeight(9);
        fill.prefWidthProperty().bind(bg.widthProperty().multiply(0));

        javafx.scene.layout.StackPane stack = new javafx.scene.layout.StackPane(bg, fill);
        stack.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(stack, Priority.ALWAYS);

        return new Pane[]{bg, fill, stack};
    }

    // ── Título de sección (texto en mayúsculas pequeñas) ──────────────
    // Crea un título de sección con estilo
    public static Label sectionTitle(String text) {
        Label l = new Label(text.toUpperCase());
        l.setStyle(
            "-fx-font-size:11px;-fx-font-weight:bold;" +
            "-fx-text-fill:" + Styles.MUTED + ";" +
            "-fx-padding:16 0 6 0;" +
            "-fx-letter-spacing:.04em;");
        return l;
    }

    // ── Chip simple (solo texto en inglés) ────────────────────────────
    // Usado en lugares donde solo importa el nombre del verbo
    // Crea un chip con texto simple
    public static Label chip(String text) {
        Label l = new Label(text);
        l.setStyle(
            "-fx-background-color:#F1F4F9;" +
            "-fx-background-radius:20;" +
            "-fx-padding:6 12 6 12;" +
            "-fx-font-size:12px;" +
            "-fx-font-weight:bold;" +
            "-fx-text-fill:" + Styles.TEXT + ";");
        return l;
    }

    // ── Chip doble (inglés + español) ─────────────────────────────────
    // Nuevo: muestra "Practice · Practicar" para que el estudiante reconozca
    // inmediatamente el significado. Usado en ProgressScreen.
    // Crea un chip con texto en inglés y español
    public static Label chipDoble(String ingles, String espanol) {
        Label l = new Label(ingles + "  ·  " + espanol);
        l.setStyle(
            "-fx-background-color:" + Styles.BLUE_LIGHT + ";" +
            "-fx-background-radius:20;" +
            "-fx-padding:6 12 6 12;" +
            "-fx-font-size:12px;" +
            "-fx-font-weight:bold;" +
            "-fx-text-fill:" + Styles.BLUE + ";");
        return l;
    }

    // ── Nota vacía (cuando no hay datos que mostrar) ──────────────────
    // Crea una etiqueta para mostrar cuando no hay datos
    public static Label emptyNote(String text) {
        Label l = new Label(text);
        l.setStyle(
            "-fx-font-size:11px;" +
            "-fx-text-fill:" + Styles.MUTED + ";" +
            "-fx-text-alignment:center;" +
            "-fx-padding:14;" +
            "-fx-border-color:#E2E8F0;" +
            "-fx-border-radius:12;" +
            "-fx-border-width:1;" +
            "-fx-border-style:dashed;" +
            "-fx-background-radius:12;");
        l.setWrapText(true);
        l.setMaxWidth(Double.MAX_VALUE);
        return l;
    }

    // ── Tarjeta contenedora (fondo blanco, bordes redondeados) ────────
    // Crea una tarjeta contenedora con estilo
    public static VBox card(javafx.scene.Node... children) {
        VBox card = new VBox(8);
        card.setStyle(Styles.CARD);
        card.setPadding(new Insets(16));
        card.getChildren().addAll(children);
        return card;
    }
}
