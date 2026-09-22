package com.edugo.view;

import com.edugo.controller.ScreenManager;
import com.edugo.data.AppData;
import com.edugo.model.Sentence;
import com.edugo.model.Verb;
import com.edugo.util.Styles;
import com.edugo.util.TTS;
import com.edugo.util.UIHelper;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pantalla del constructor de oraciones.
 *
 * FUENTES DE DATOS:
 *   - Oraciones: AppData.SENTENCES (construidas dinámicamente desde VERBS al inicio).
 *     48 oraciones = 16 verbos × 3 tiempos (Present / Past / Future).
 *   - Las fichas de palabras se construyen en tiempo de ejecución desde el verbo
 *     (baseForm, presentForm, pastForm, futureForm en verbos.txt) más palabras complemento.
 *   - Se asigna un pronombre aleatorio (I/you/he/she/we/they) por oración.
 *   - Añadir un verbo nuevo en TeacherDashboard llama a AppData.rebuildSentences()
 *     para que las nuevas oraciones aparezcan automáticamente.
 */
// Pantalla del constructor de oraciones
public class SentenceBuilderScreen {

    private final ScreenManager nav;

    // Tiempo verbal actual seleccionado por el usuario
    private String currentTense = AppData.PRESENT;
    // Pronombre asignado aleatoriamente para la oración actual (I/you/he/she/we/they)
    private String currentPronoun = "I";
    // Índice de la oración actual dentro de las oraciones filtradas por tiempo
    private int currentIndex = 0;

    // Índices de las palabras elegidas por el usuario (posiciones en wordPool)
    private List<Integer> chosen;
    // Banco de palabras disponibles (correctas + trampas, mezcladas)
    private List<String> wordPool;
    // Palabras correctas de la oración actual (construidas dinámicamente)
    private List<String> currentCorrectWords;
    // Lista de pronombres disponibles para asignación aleatoria (sin 'it' por no aplicar a verbos de acción humana)
    private static final String[] PRONOUNS = {"I", "you", "he", "she", "we", "they"};
    // Generador de números aleatorios para asignación de pronombres
    private java.util.Random random = new java.util.Random();

    private Label counterLabel;
    private Label tenseLabel;
    private Label verbNameLabel;
    private FlowPane slotPane;
    private FlowPane bankPane;
    private Label feedbackLabel;
    private VBox phonBox;
    private Label ipaLabel;
    private Label sentenceLabel;
    private Button checkBtn;
    private Button nextBtn;
    private Button tensePresentBtn;
    private Button tensePastBtn;
    private Button tenseFutureBtn;
    private Label pronounLabel;

    // Constructor de SentenceBuilderScreen
    public SentenceBuilderScreen(ScreenManager nav) { this.nav = nav; }

    // Construye la pantalla del constructor de oraciones
    public VBox build() {
        VBox root = new VBox();
        root.setStyle("-fx-background-color:" + Styles.BG + ";");
        root.setFillWidth(true);

        HBox topBar = UIHelper.topBar("EduGo", "Sentence Builder",
            () -> nav.show("student"));

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:transparent;-fx-background:transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox body = new VBox(12);
        body.setPadding(new Insets(18));
        body.setStyle("-fx-background-color:" + Styles.BG + ";");

        // ── Tense selector ────────────────────────────────────────────
        Label tenseHint = new Label("Choose a tense to practice:");
        tenseHint.setStyle("-fx-font-size:12px;-fx-text-fill:" + Styles.MUTED + ";");
        tensePresentBtn = tenseButton("Present", AppData.PRESENT);
        tensePastBtn    = tenseButton("Past",    AppData.PAST);
        tenseFutureBtn  = tenseButton("Future",  AppData.FUTURE);
        HBox tenseRow = new HBox(8, tensePresentBtn, tensePastBtn, tenseFutureBtn);
        tenseRow.setFillHeight(true);
        HBox.setHgrow(tensePresentBtn, Priority.ALWAYS);
        HBox.setHgrow(tensePastBtn,    Priority.ALWAYS);
        HBox.setHgrow(tenseFutureBtn,  Priority.ALWAYS);

        // ── Pronoun display ─────────────────────────────────────────────
        Label pronounHint = new Label("Pronoun:");
        pronounHint.setStyle("-fx-font-size:12px;-fx-text-fill:" + Styles.MUTED + ";");
        pronounLabel = new Label();
        pronounLabel.setStyle(
            "-fx-background-color:" + Styles.BLUE_LIGHT + ";" +
            "-fx-text-fill:" + Styles.BLUE + ";" +
            "-fx-background-radius:20;" +
            "-fx-padding:6 12 6 12;" +
            "-fx-font-size:14px;" +
            "-fx-font-weight:bold;");

        // ── Counter and tense badge ───────────────────────────────────
        counterLabel = new Label("Sentence 1 of 16");
        counterLabel.setStyle("-fx-font-size:12px;-fx-text-fill:" + Styles.MUTED + ";");
        tenseLabel = new Label(currentTense);
        tenseLabel.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:" + Styles.BLUE + ";");
        HBox infoRow = new HBox(counterLabel, tenseLabel);
        HBox.setHgrow(counterLabel, Priority.ALWAYS);

        // Active verb label
        verbNameLabel = new Label();
        verbNameLabel.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:" + Styles.TEXT + ";");

        // ── Sentence construction area ────────────────────────────────
        Label slotHint = new Label("Your sentence:");
        slotHint.setStyle("-fx-font-size:12px;-fx-text-fill:" + Styles.MUTED + ";");
        slotPane = new FlowPane(8, 8);
        slotPane.setStyle(
            "-fx-background-color:#F8FAFC;" +
            "-fx-border-color:#E2E8F0;-fx-border-width:2;" +
            "-fx-border-style:dashed;-fx-border-radius:12;" +
            "-fx-background-radius:12;-fx-padding:10;-fx-min-height:50;");

        Label bankHint = new Label("Tap words in order:");
        bankHint.setStyle("-fx-font-size:12px;-fx-text-fill:" + Styles.MUTED + ";");
        bankPane = new FlowPane(8, 8);

        feedbackLabel = new Label();
        feedbackLabel.setVisible(false);
        feedbackLabel.setManaged(false);
        feedbackLabel.setWrapText(true);
        feedbackLabel.setMaxWidth(Double.MAX_VALUE);

        // ── Phonetics box (shown on correct answer) ───────────────────
        phonBox = new VBox(6);
        phonBox.setStyle(Styles.PHONETIC_BOX);
        phonBox.setAlignment(Pos.CENTER_LEFT);

        sentenceLabel = new Label();
        sentenceLabel.setStyle(
            "-fx-font-size:14px;-fx-font-weight:bold;" +
            "-fx-text-fill:" + Styles.TEXT + ";-fx-wrap-text:true;");
        sentenceLabel.setWrapText(true);

        Label ipaTitle = new Label("IPA pronunciation (word by word):");
        ipaTitle.setStyle("-fx-font-size:11px;-fx-text-fill:" + Styles.MUTED + ";");
        ipaLabel = new Label();
        ipaLabel.setStyle(
            "-fx-font-size:14px;-fx-font-weight:bold;" +
            "-fx-text-fill:" + Styles.BLUE + ";-fx-wrap-text:true;");
        ipaLabel.setWrapText(true);

        Label phonHint2 = new Label("Listen to the full sentence:");
        phonHint2.setStyle("-fx-font-size:11px;-fx-text-fill:" + Styles.MUTED + ";");

        Button listenBtn = new Button("🔊  Listen");
        listenBtn.setStyle(Styles.BTN_OUTLINE);
        listenBtn.setMaxWidth(Double.MAX_VALUE);
        listenBtn.setOnAction(e -> escucharOracion());

        phonBox.getChildren().addAll(sentenceLabel, ipaTitle, ipaLabel, phonHint2, listenBtn);
        phonBox.setVisible(false);
        phonBox.setManaged(false);

        // ── Action buttons ────────────────────────────────────────────
        checkBtn = new Button("✓  Check sentence");
        checkBtn.setMaxWidth(Double.MAX_VALUE);
        checkBtn.setStyle(Styles.BTN_PRIMARY);
        checkBtn.setOnAction(e -> verificar());

        nextBtn = new Button("Next  →");
        nextBtn.setMaxWidth(Double.MAX_VALUE);
        nextBtn.setStyle(Styles.BTN_PRIMARY);
        nextBtn.setVisible(false);
        nextBtn.setManaged(false);
        nextBtn.setOnAction(e -> siguienteOracion());

        body.getChildren().addAll(
            tenseHint, tenseRow, pronounHint, pronounLabel, infoRow, verbNameLabel,
            slotHint, slotPane, bankHint, bankPane,
            feedbackLabel, phonBox, checkBtn, nextBtn);

        scroll.setContent(body);
        root.getChildren().addAll(topBar, scroll);

        refreshTenseButtons();
        loadSentence();
        return root;
    }

    // Crea un botón para seleccionar un tiempo verbal
    private Button tenseButton(String label, String tense) {
        Button b = new Button(label);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setOnAction(e -> selectTense(tense));
        return b;
    }

    // Selecciona un tiempo verbal (no reinicia el índice de la oración)
    private void selectTense(String tense) {
        currentTense = tense;
        refreshTenseButtons();
        loadSentence();
    }

    // Asigna un pronombre aleatorio para la oración actual
    private void assignRandomPronoun() {
        currentPronoun = PRONOUNS[random.nextInt(PRONOUNS.length)];
        pronounLabel.setText(currentPronoun);
    }

    // Actualiza el estilo de los botones de tiempo verbal
    private void refreshTenseButtons() {
        applyTenseStyle(tensePresentBtn, AppData.PRESENT);
        applyTenseStyle(tensePastBtn,    AppData.PAST);
        applyTenseStyle(tenseFutureBtn,  AppData.FUTURE);
        tenseLabel.setText(currentTense);
        String color = switch (currentTense) {
            case AppData.PAST   -> Styles.PURPLE;
            case AppData.FUTURE -> Styles.ORANGE;
            default             -> Styles.BLUE;
        };
        tenseLabel.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
    }

    // Aplica el estilo apropiado al botón de tiempo verbal
    private void applyTenseStyle(Button btn, String tense) {
        btn.setStyle(currentTense.equals(tense) ? Styles.TENSE_BTN_ACTIVE : Styles.TENSE_BTN_INACTIVE);
    }

    // Filtra las oraciones por el tiempo verbal actual
    private List<Sentence> filtered() {
        return AppData.SENTENCES.stream()
            .filter(s -> s.getTense().equals(currentTense)).toList();
    }

    // Carga la oración actual: asigna pronombre aleatorio, construye palabras correctas y trampas
    private void loadSentence() {
        List<Sentence> list = filtered();
        if (list.isEmpty()) return;
        if (currentIndex >= list.size()) currentIndex = 0;
        Sentence s = list.get(currentIndex);

        Verb v = AppData.VERBS.stream()
            .filter(vb -> vb.getId() == s.getVerbId())
            .findFirst().orElse(null);

        // Asignar un pronombre aleatorio para esta oración
        assignRandomPronoun();

        counterLabel.setText("Sentence " + (currentIndex + 1) + " of " + list.size());
        verbNameLabel.setText(v != null ? "Verb: " + v.getEnglish() : "");

        // Construir las palabras correctas según pronombre y tiempo
        currentCorrectWords = buildCorrectWords(s, v, currentPronoun, currentTense);
        // Construir el banco de palabras con correctas + trampas (sin duplicados)
        wordPool = buildWordPool(currentCorrectWords, buildExtraWords(v, currentTense));
        chosen   = new ArrayList<>();

        feedbackLabel.setVisible(false); feedbackLabel.setManaged(false);
        phonBox.setVisible(false);       phonBox.setManaged(false);
        checkBtn.setVisible(true);       checkBtn.setManaged(true);
        nextBtn.setVisible(false);       nextBtn.setManaged(false);

        renderWords();
    }

    /**
     * Construye la secuencia completa de palabras correctas desde cero:
     *   PRESENT → pronombre + baseForm (I/you/we/they) o presentForm (he/she) + complemento
     *   PAST    → pronombre + pastForm + complemento (sin "will")
     *   FUTURE  → pronombre + will + baseForm + complemento
     * La forma verbal se selecciona según el pronombre y el tiempo para asegurar concordancia gramatical.
     */
    private List<String> buildCorrectWords(Sentence s, Verb v, String pronoun, String tense) {
        if (v == null) return new ArrayList<>(s.getCorrect());

        List<String> words = new ArrayList<>();
        words.add(pronoun);

        String baseForm = v.getEnglish().toLowerCase();
        List<String> complement = AppData.complementTokensForVerb(v.getId());

        switch (tense) {
            case AppData.PRESENT -> {
                // Para tercera persona singular (he/she), usar presentForm; para otros, baseForm
                words.add(isThirdPersonSingular(pronoun) ? v.getPresentForm() : baseForm);
                words.addAll(complement);
            }
            case AppData.PAST -> {
                // Pasado usa pastForm para todos los pronombres
                words.add(v.getPastForm());
                words.addAll(complement);
            }
            case AppData.FUTURE -> {
                // Futuro usa "will" + baseForm para todos los pronombres
                words.add("will");
                words.add(baseForm);
                words.addAll(complement);
            }
            default -> words.addAll(s.getCorrect());
        }
        return words;
    }

    /**
     * Construye el banco de palabras: todas las palabras correctas siempre son fichas,
     * las palabras trampa se añaden sin eliminar ninguna palabra correcta.
     * Elimina duplicados para que cada palabra aparezca solo una vez antes de mezclar.
     */
    private List<String> buildWordPool(List<String> correct, List<String> extra) {
        List<String> pool = new ArrayList<>(correct);
        for (String word : extra) {
            if (!pool.contains(word)) {
                pool.add(word);
            }
        }
        Collections.shuffle(pool);
        return pool;
    }

    /**
     * Genera palabras trampa (formas verbales incorrectas) según el tiempo:
     *   PRESENT → pastForm, "will", futureForm (distractores)
     *   PAST    → presentForm, "will", futureForm (distractores)
     *   FUTURE  → pastForm, presentForm, futureForm (sin "will" porque es correcto)
     * "will" solo aparece como distractor en Present y Past, no en Future.
     */
    private List<String> buildExtraWords(Verb v, String tense) {
        String present = v.getPresentForm();
        String past = v.getPastForm();
        String future = v.getFutureForm();

        return switch (tense) {
            case AppData.PRESENT -> List.of(past, "will", future);
            case AppData.PAST    -> List.of(present, "will", future);
            case AppData.FUTURE  -> List.of(past, present, future);
            default              -> List.of();
        };
    }

    // Determina si el pronombre es tercera persona singular (he/she) para seleccionar presentForm
    private boolean isThirdPersonSingular(String pronoun) {
        return pronoun.equals("he") || pronoun.equals("she");
    }

    // Renderiza las palabras: las elegidas van al área de construcción, las disponibles al banco
    private void renderWords() {
        slotPane.getChildren().clear();
        bankPane.getChildren().clear();

        for (int i = 0; i < chosen.size(); i++) {
            final int idx = i;
            Button chip = new Button(wordPool.get(chosen.get(i)));
            chip.setStyle(Styles.WORD_CHIP_SELECTED);
            chip.setOnAction(e -> removeWord(idx));
            slotPane.getChildren().add(chip);
        }

        for (int i = 0; i < wordPool.size(); i++) {
            if (chosen.contains(i)) continue;
            final int idx = i;
            Button chip = new Button(wordPool.get(i));
            chip.setStyle(Styles.WORD_CHIP);
            chip.setOnAction(e -> addWord(idx));
            bankPane.getChildren().add(chip);
        }
    }

    // Añade una palabra a la oración
    private void addWord(int idx)   { chosen.add(idx); resetFeedback(); renderWords(); }
    // Remueve una palabra de la oración
    private void removeWord(int ci) { chosen.remove(ci); resetFeedback(); renderWords(); }

    // Reinicia el estado de retroalimentación
    private void resetFeedback() {
        feedbackLabel.setVisible(false); feedbackLabel.setManaged(false);
        phonBox.setVisible(false);       phonBox.setManaged(false);
        checkBtn.setVisible(true);       checkBtn.setManaged(true);
        nextBtn.setVisible(false);       nextBtn.setManaged(false);
    }

    // Verifica si la oración construida coincide exactamente con las palabras correctas
    private void verificar() {
        List<String> correctWords = currentCorrectWords;
        List<String> built = chosen.stream().map(i -> wordPool.get(i)).toList();

        if (built.equals(correctWords)) {
            mostrarFeedback("✅  Correct sentence!", Styles.FEEDBACK_GOOD);
            checkBtn.setVisible(false); checkBtn.setManaged(false);
            nextBtn.setVisible(true);   nextBtn.setManaged(true);

            String plain = String.join(" ", correctWords);
            String ipa   = AppData.sentenceIpa(correctWords);
            sentenceLabel.setText("\"" + plain + "\"");
            ipaLabel.setText(ipa);
            phonBox.setVisible(true);
            phonBox.setManaged(true);
            animarAcierto();
        } else {
            mostrarFeedback("❌  That's not the right order. Try again!", Styles.FEEDBACK_BAD);
            animarError();
        }
    }

    // Reproduce la pronunciación de la oración correcta usando TTS
    private void escucharOracion() {
        TTS.speak(String.join(" ", currentCorrectWords));
    }

    // Avanza a la siguiente oración; al completar todas, reinicia al inicio
    private void siguienteOracion() {
        currentIndex++;
        List<Sentence> list = filtered();
        if (currentIndex >= list.size()) {
            currentIndex = 0;
            loadSentence();
            mostrarFeedback(
                "🎉  You completed all " + list.size() + " sentences in " + currentTense + "! Starting over.",
                Styles.FEEDBACK_GOOD);
            return;
        }
        loadSentence();
    }

    // Muestra un mensaje de retroalimentación
    private void mostrarFeedback(String texto, String estilo) {
        feedbackLabel.setText(texto);
        feedbackLabel.setStyle(estilo);
        feedbackLabel.setVisible(true);
        feedbackLabel.setManaged(true);
    }

    // Ejecuta la animación de acierto
    private void animarAcierto() {
        ScaleTransition p = new ScaleTransition(Duration.millis(150), slotPane);
        p.setToX(1.03); p.setToY(1.03);
        ScaleTransition v = new ScaleTransition(Duration.millis(150), slotPane);
        v.setToX(1.0); v.setToY(1.0);
        new SequentialTransition(p, v).play();
    }

    // Ejecuta la animación de error
    private void animarError() {
        TranslateTransition s = new TranslateTransition(Duration.millis(50), bankPane);
        s.setByX(8); s.setCycleCount(6); s.setAutoReverse(true);
        s.setOnFinished(e -> bankPane.setTranslateX(0));
        s.play();
    }
}
