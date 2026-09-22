package com.edugo.view;

import com.edugo.controller.ScreenManager;
import com.edugo.data.AppData;
import com.edugo.data.AppState;
import com.edugo.model.Verb;
import com.edugo.util.Styles;
import com.edugo.util.TTS;
import com.edugo.util.UIHelper;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Pantalla de Flashcards.
 *
 * ORIGEN DE LOS DATOS:
 *   - Los verbos (VERBS) se cargan desde AppData.VERBS, que a su vez lee el
 *     archivo data/verbos.txt al arrancar la app (en Main.start → AppData.init).
 *   - Las imágenes se buscan en la ruta relativa indicada en verbos.txt
 *     (columna imagePath), p.ej. "images/verbos/practice.png".
 *   - Los puntos se acumulan en AppState.flashcardPoints (memoria de sesión)
 *     y se persisten via AppState.markMastered → DataManager.saveProgress →
 *     data/progreso.txt.
 *   - MAX_ATTEMPTS = 3 intentos por verbo antes de revelar la respuesta.
 */
// Pantalla de Flashcards para practicar verbos
public class FlashcardsScreen {

    // Número máximo de intentos antes de revelar la respuesta
    private static final int MAX_ATTEMPTS = 3;

    // Lista de verbos cargada desde data/verbos.txt al arrancar la app
    private static final List<Verb> VERBS = AppData.VERBS;

    // Índice del verbo actual
    private int index;
    // Intentos restantes para el verbo actual
    private int attemptsLeft;
    // Puntos acumulados en la sesión
    private int points;

    private Label counterLabel;
    private Label pointsLabel;
    private Pane progressFill;
    private Pane progressBg;
    private ImageView verbImage;
    private Label verbPlaceholder;

    // Contenedor de la imagen — ahora más grande para ser lo más llamativo
    private StackPane imageContainer;

    private TextField inputField;
    private Label feedbackLabel;
    private VBox phonBox;
    private Label phoneticLabel;
    private Label respellingLabel;
    private Button checkBtn;
    private Button nextBtn;
    private Button listenBtn;

    // Panel raíz que contiene el confeti (se superpone encima de todo)
    private StackPane rootStack;

    private final ScreenManager nav;

    // Constructor de FlashcardsScreen
    public FlashcardsScreen(ScreenManager nav) {
        this.nav = nav;
    }

    // Construye la pantalla de flashcards
    public StackPane build() {
        // Usamos StackPane como raíz para poder superponer el confeti encima
        rootStack = new StackPane();

        VBox root = new VBox();
        root.setStyle("-fx-background-color:" + Styles.BG + ";");
        root.setFillWidth(true);

        HBox topBar = UIHelper.topBar("EduGo", "Flash Cards", () -> nav.show("student"));

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:transparent;-fx-background:transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox body = new VBox(14);
        body.setPadding(new Insets(18));
        body.setStyle("-fx-background-color:" + Styles.BG + ";");

        // ── Fila del contador y puntos ────────────────────────────────
        counterLabel = new Label("Verb 1 of 16");
        counterLabel.setStyle("-fx-font-size:13px;-fx-text-fill:" + Styles.MUTED + ";");
        pointsLabel = new Label("⭐ 0 pts");
        pointsLabel.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:" + Styles.BLUE + ";");
        HBox counterRow = new HBox();
        counterRow.getChildren().addAll(counterLabel, pointsLabel);
        HBox.setHgrow(counterLabel, Priority.ALWAYS);
        counterRow.setAlignment(Pos.CENTER_LEFT);

        // ── Barra de progreso (origen: index / VERBS.size()) ──────────
        Pane[] pb = UIHelper.progressBarMutable();
        progressBg   = pb[0];
        progressFill = pb[1];
        StackPane progressStack = (StackPane) pb[2];

        // ── Tarjeta principal ─────────────────────────────────────────
        VBox card = new VBox(12);
        card.setStyle(Styles.CARD);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER);

        /*
         * Contenedor de imagen — tamaño aumentado a 320×320 px para que sea
         * el elemento más dominante de la pantalla.
         * El gradiente de fondo y el borde dan profundidad visual.
         */
        imageContainer = new StackPane();
        imageContainer.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, " + Styles.BLUE_LIGHT + ", " + Styles.PURPLE_LIGHT + ");" +
            "-fx-background-radius:32;" +
            "-fx-border-color:" + Styles.BLUE_BORDER + ";" +
            "-fx-border-width:3;" +
            "-fx-border-radius:32;" +
            "-fx-min-width:360;-fx-min-height:360;" +
            "-fx-max-width:360;-fx-max-height:360;");
        imageContainer.setPrefSize(360, 360);

        // La imagen en sí — cabe hasta 300×300 dentro del contenedor de 320
        verbImage = new ImageView();
        verbImage.setFitWidth(340);
        verbImage.setFitHeight(340);
        verbImage.setPreserveRatio(true);

        // Emoji de respaldo si la imagen no carga
        verbPlaceholder = new Label();
        verbPlaceholder.setStyle("-fx-font-size:120px;");
        verbPlaceholder.setVisible(false);

        imageContainer.getChildren().addAll(verbImage, verbPlaceholder);

        // Texto de ayuda debajo de la imagen
        Label typeHint = new Label("Escribe el verbo en inglés");
        typeHint.setStyle(
            "-fx-font-size:13px;-fx-text-fill:" + Styles.MUTED + ";" +
            "-fx-font-style:italic;");

        // Campo de texto donde el estudiante escribe su respuesta
        inputField = new TextField();
        inputField.setPromptText("Escribe aquí...");
        inputField.setStyle(Styles.INPUT);
        inputField.setMaxWidth(Double.MAX_VALUE);
        inputField.setOnAction(e -> {
            if (!checkBtn.isDisabled() && checkBtn.isVisible()) verificar();
            else if (nextBtn.isVisible()) siguiente();
        });

        // Etiqueta de retroalimentación (correcto / incorrecto / revelación)
        feedbackLabel = new Label();
        feedbackLabel.setVisible(false);
        feedbackLabel.setManaged(false);
        feedbackLabel.setWrapText(true);
        feedbackLabel.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(imageContainer, typeHint, inputField, feedbackLabel);

        // ── Caja de pronunciación (aparece al pulsar "Listen") ────────
        phonBox = new VBox(4);
        phonBox.setStyle(Styles.PHONETIC_BOX);
        phonBox.setAlignment(Pos.CENTER);

        phoneticLabel = new Label();
        phoneticLabel.setStyle(
            "-fx-font-size:22px;-fx-font-weight:bold;" +
            "-fx-text-fill:" + Styles.BLUE + ";" +
            "-fx-letter-spacing:2px;");

        Label phonHint = new Label("Pronunciación en inglés (IPA)");
        phonHint.setStyle("-fx-font-size:11px;-fx-text-fill:" + Styles.MUTED + ";");

        /*
         * Respelling simplificado (p.ej. "prák·tis") — distinto del IPA para
         * que el estudiante pueda intuir cómo suena sin conocer los símbolos.
         * Origen: columna 6 (pronunciation) de data/verbos.txt.
         */
        respellingLabel = new Label();
        respellingLabel.setStyle("-fx-font-size:11px;-fx-text-fill:" + Styles.MUTED + ";-fx-font-style:italic;");
        phonBox.getChildren().addAll(phoneticLabel, phonHint, respellingLabel);
        phonBox.setVisible(false);
        phonBox.setManaged(false);

        // ── Fila de botones ───────────────────────────────────────────
        listenBtn = new Button("🔊  Listen");
        listenBtn.setStyle(Styles.BTN_OUTLINE);
        listenBtn.setMaxWidth(Double.MAX_VALUE);

        checkBtn = new Button("✓  Check");
        checkBtn.setStyle(Styles.BTN_PRIMARY);
        checkBtn.setMaxWidth(Double.MAX_VALUE);

        nextBtn = new Button("Next  →");
        nextBtn.setStyle(Styles.BTN_PRIMARY);
        nextBtn.setMaxWidth(Double.MAX_VALUE);
        nextBtn.setVisible(false);
        nextBtn.setManaged(false);

        HBox btnRow = new HBox(8, listenBtn, checkBtn, nextBtn);
        HBox.setHgrow(listenBtn, Priority.ALWAYS);
        HBox.setHgrow(checkBtn,  Priority.ALWAYS);
        HBox.setHgrow(nextBtn,   Priority.ALWAYS);
        btnRow.setFillHeight(true);

        listenBtn.setOnAction(e -> escuchar());
        checkBtn.setOnAction(e -> verificar());
        nextBtn.setOnAction(e -> siguiente());

        body.getChildren().addAll(counterRow, progressStack, card, phonBox, btnRow);
        scroll.setContent(body);
        root.getChildren().addAll(topBar, scroll);

        // El VBox principal ocupa todo el StackPane raíz
        rootStack.getChildren().add(root);

        // Estado inicial
        index        = 0;
        attemptsLeft = MAX_ATTEMPTS;
        points       = AppState.getPoints();
        renderVerb();

        return rootStack;
    }

    // ── Renderizar el verbo actual ────────────────────────────────────
    // Muestra el verbo actual en la pantalla
    private void renderVerb() {
        Verb v = VERBS.get(index);

        // Origen del texto: AppData.VERBS → data/verbos.txt columnas 1-2
        counterLabel.setText("Verb " + (index + 1) + " of " + VERBS.size());
        pointsLabel.setText("⭐ " + points + " pts");

        // La barra de progreso refleja cuántos verbos se han visto (no dominado)
        double pct = (double) index / VERBS.size();
        progressFill.prefWidthProperty().unbind();
        progressFill.prefWidthProperty().bind(progressBg.widthProperty().multiply(pct));

        // Restablecer estilos de imagen (por si quedó rotada o escalada)
        imageContainer.setScaleX(1);
        imageContainer.setScaleY(1);
        imageContainer.setRotate(0);
        imageContainer.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, " + Styles.BLUE_LIGHT + ", " + Styles.PURPLE_LIGHT + ");" +
            "-fx-background-radius:32;" +
            "-fx-border-color:" + Styles.BLUE_BORDER + ";" +
            "-fx-border-width:3;" +
            "-fx-border-radius:32;" +
            "-fx-min-width:360;-fx-min-height:360;" +
            "-fx-max-width:360;-fx-max-height:360;");

        cargarImagen(v);

        inputField.clear();
        inputField.setDisable(false);
        inputField.requestFocus();

        feedbackLabel.setVisible(false);
        feedbackLabel.setManaged(false);
        phonBox.setVisible(false);
        phonBox.setManaged(false);

        checkBtn.setVisible(true);
        checkBtn.setManaged(true);
        checkBtn.setDisable(false);
        nextBtn.setVisible(false);
        nextBtn.setManaged(false);

        attemptsLeft = MAX_ATTEMPTS;
    }

    // Carga la imagen desde el path relativo guardado en verbos.txt
    private void cargarImagen(Verb v) {
        try {
            // imagePath viene de la columna 4 de data/verbos.txt (ruta relativa)
            String path = v.getImagePath();
            java.io.File file = new java.io.File(path);
            String url = file.toURI().toString();
            Image img = new Image(url, true);

            img.errorProperty().addListener((obs, old, isErr) -> {
                if (isErr) {
                    verbImage.setVisible(false);
                    verbPlaceholder.setVisible(true);
                }
            });
            img.progressProperty().addListener((obs, old, progress) -> {
                if (progress.doubleValue() >= 1.0 && !img.isError()) {
                    verbImage.setImage(img);
                    verbImage.setVisible(true);
                    verbPlaceholder.setVisible(false);
                }
            });
            // Carga síncrona si la imagen ya estaba en caché
            if (img.getProgress() >= 1.0 && !img.isError()) {
                verbImage.setImage(img);
                verbImage.setVisible(true);
                verbPlaceholder.setVisible(false);
            }
        } catch (Exception ex) {
            verbImage.setVisible(false);
            verbPlaceholder.setVisible(true);
        }
    }

    // ── Escuchar pronunciación ────────────────────────────────────────
    // Reproduce la pronunciación del verbo
    private void escuchar() {
        Verb v = VERBS.get(index);
        // IPA: columna 5 de data/verbos.txt (p.ej. /ˈpræk.tɪs/)
        phoneticLabel.setText(v.getIpa());
        // Respelling: columna 6 de data/verbos.txt (p.ej. prák·tis)
        respellingLabel.setText("se lee: " + v.getPronunciation());
        phonBox.setVisible(true);
        phonBox.setManaged(true);
        // TTS recibe la palabra en inglés (columna 2), NO el IPA, para pronunciación natural
        TTS.speak(v.getEnglish());
    }

    // ── Verificar respuesta ───────────────────────────────────────────
    // Verifica si la respuesta del usuario es correcta
    private void verificar() {
        Verb v = VERBS.get(index);
        String respuesta = inputField.getText().trim();

        if (respuesta.equalsIgnoreCase(v.getEnglish())) {
            // ✅ Respuesta correcta
            AppState.markMastered(v.getId());  // persiste en data/progreso.txt
            points += 10;
            AppState.addPoints(10);
            mostrarFeedback("✅  ¡Correcto!  +10 pts", Styles.FEEDBACK_GOOD);

            inputField.setDisable(true);
            checkBtn.setVisible(false);
            checkBtn.setManaged(false);
            nextBtn.setVisible(true);
            nextBtn.setManaged(true);

            // Animación de celebración: spin + cambio a verde + confeti
            animarAcierto();

        } else {
            // ❌ Respuesta incorrecta
            attemptsLeft--;
            if (attemptsLeft > 0) {
                mostrarFeedback(
                    "❌  Incorrecto, intenta de nuevo. (" + attemptsLeft +
                    " intento" + (attemptsLeft != 1 ? "s" : "") + " restante" +
                    (attemptsLeft != 1 ? "s" : "") + ")",
                    Styles.FEEDBACK_BAD);
                // Pequeña sacudida de la imagen para retroalimentación visual
                animarError();
            } else {
                // Se agotaron los intentos: revelar respuesta
                mostrarFeedback(
                    "Se acabaron los intentos. La respuesta era: \"" + v.getEnglish() + "\"",
                    Styles.FEEDBACK_REVEAL);
                inputField.setDisable(true);
                checkBtn.setVisible(false);
                checkBtn.setManaged(false);
                nextBtn.setVisible(true);
                nextBtn.setManaged(true);
            }
        }
        pointsLabel.setText("⭐ " + points + " pts");
    }

    // ── Siguiente verbo ───────────────────────────────────────────────
    // Avanza al siguiente verbo
    private void siguiente() {
        index++;
        if (index >= VERBS.size()) {
            nav.invalidate("progress");
            nav.show("progress");
            return;
        }
        renderVerb();
    }

    // Muestra un mensaje de retroalimentación
    private void mostrarFeedback(String texto, String estilo) {
        feedbackLabel.setText(texto);
        feedbackLabel.setStyle(estilo);
        feedbackLabel.setVisible(true);
        feedbackLabel.setManaged(true);
    }

    // ── Animación al ACERTAR: rotación completa + pulso verde ─────────
    // Ejecuta la animación de acierto
    private void animarAcierto() {
        // 1) Rotación de 360° en 600ms
        RotateTransition spin = new RotateTransition(Duration.millis(600), imageContainer);
        spin.setFromAngle(0);
        spin.setToAngle(360);
        spin.setInterpolator(Interpolator.EASE_OUT);

        // 2) Escala pulsante (se "infla" un poco al terminar el spin)
        ScaleTransition pulso = new ScaleTransition(Duration.millis(200), imageContainer);
        pulso.setToX(1.08);
        pulso.setToY(1.08);
        ScaleTransition desinflar = new ScaleTransition(Duration.millis(200), imageContainer);
        desinflar.setToX(1.0);
        desinflar.setToY(1.0);

        // 3) Cambiar borde a verde para indicar acierto
        spin.setOnFinished(e -> {
            imageContainer.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " + Styles.GREEN_LIGHT + ", #D1FAE5);" +
                "-fx-background-radius:32;" +
                "-fx-border-color:" + Styles.GREEN + ";" +
                "-fx-border-width:4;" +
                "-fx-border-radius:32;" +
                "-fx-min-width:360;-fx-min-height:360;" +
                "-fx-max-width:360;-fx-max-height:360;");
            new SequentialTransition(pulso, desinflar).play();
            // 4) Lanzar confeti encima de todo
            lanzarConfeti();
        });

        spin.play();
    }

    // ── Animación al ERRAR: sacudida horizontal ───────────────────────
    // Ejecuta la animación de error
    private void animarError() {
        TranslateTransition sacudida = new TranslateTransition(Duration.millis(50), imageContainer);
        sacudida.setFromX(0);
        sacudida.setByX(10);
        sacudida.setCycleCount(6);
        sacudida.setAutoReverse(true);
        sacudida.setOnFinished(e -> imageContainer.setTranslateX(0));
        sacudida.play();
    }

    // ── Confeti: círculos de colores que caen desde arriba ────────────
    // Lanza el efecto de confeti
    private void lanzarConfeti() {
        // Paleta de colores del confeti (vivos pero no chillones)
        String[] COLORES = {
            Styles.BLUE, Styles.ORANGE, Styles.GREEN, Styles.PURPLE,
            "#F59E0B", "#EC4899", "#10B981", "#3B82F6"
        };

        Random rnd = new Random();
        List<Circle> piezas = new ArrayList<>();

        // Crear 40 círculos de confeti distribuidos horizontalmente
        for (int i = 0; i < 40; i++) {
            double radio = 5 + rnd.nextDouble() * 6; // entre 5 y 11 px
            Circle c = new Circle(radio);
            c.setFill(Color.web(COLORES[rnd.nextInt(COLORES.length)]));
            c.setOpacity(0.9);
            // Posición horizontal aleatoria dentro del ancho del panel
            double posX = rnd.nextDouble() * 400 - 200; // ±200 del centro
            c.setTranslateX(posX);
            c.setTranslateY(-150); // Empieza arriba del panel
            piezas.add(c);
        }

        // Agregar todos al rootStack (nivel superior, encima de todo)
        rootStack.getChildren().addAll(piezas);
        // El StackPane no debe interceptar clics — solo decoración
        rootStack.setPickOnBounds(false);

        // Para cada pieza: caída + rotación + desvanecimiento
        for (int i = 0; i < piezas.size(); i++) {
            Circle c = piezas.get(i);
            double retraso = i * 25; // cada pieza sale con un pequeño desfase

            TranslateTransition caida = new TranslateTransition(
                Duration.millis(1200 + rnd.nextInt(600)), c);
            caida.setByY(700); // cae 700px hacia abajo

            RotateTransition giro = new RotateTransition(
                Duration.millis(1200 + rnd.nextInt(600)), c);
            giro.setByAngle(rnd.nextBoolean() ? 360 : -360);

            FadeTransition desvanecer = new FadeTransition(Duration.millis(400), c);
            desvanecer.setDelay(Duration.millis(900));
            desvanecer.setToValue(0);

            ParallelTransition anim = new ParallelTransition(c, caida, giro, desvanecer);
            anim.setDelay(Duration.millis(retraso));
            anim.setOnFinished(e -> rootStack.getChildren().remove(c));
            anim.play();
        }
    }
}
