package com.edugo.view;

import com.edugo.controller.ScreenManager;
import com.edugo.data.AppState;
import com.edugo.data.DataManager;
import com.edugo.model.Verb;
import com.edugo.util.Styles;
import com.edugo.util.UIHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Set;

/**
 * Student progress screen.
 *
 * DATA SOURCES:
 *   - Mastered verbs: DataManager.loadProgress(username) reads data/progreso.txt live.
 *   - Verb catalog: DataManager.loadVerbs() reads data/verbos.txt.
 *   - Session points: AppState.getPoints() (in-memory, lost on app close).
 */
// Pantalla de progreso del estudiante
public class   ProgressScreen {

    // Construye la pantalla de progreso
    public static VBox build(ScreenManager nav) {
        VBox root = new VBox();
        root.setStyle("-fx-background-color:" + Styles.BG + ";");
        root.setFillWidth(true);

        HBox topBar = UIHelper.topBar("EduGo", "My Progress", () -> nav.show("student"));

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:transparent;-fx-background:transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox body = new VBox(4);
        body.setPadding(new Insets(18));
        body.setStyle("-fx-background-color:" + Styles.BG + ";");

        String username = AppState.getCurrentUser() != null
            ? AppState.getCurrentUser().getUsername() : "";

        Set<Integer> mastered  = DataManager.loadProgress(username);
        List<Verb> allVerbs    = DataManager.loadVerbs();
        int total              = allVerbs.size();
        int masteredCount      = mastered.size();
        int pct                = total > 0 ? (masteredCount * 100 / total) : 0;
        int pts                = AppState.getPoints();

        // ── Summary card ──────────────────────────────────────────────
        Label pctLabel = new Label(pct + "%");
        pctLabel.setStyle(
            "-fx-font-size:44px;-fx-font-weight:800;" +
            "-fx-text-fill:" + Styles.BLUE + ";");
        Label pctSub = new Label(masteredCount + " of " + total + " verbs mastered");
        pctSub.setStyle("-fx-font-size:13px;-fx-text-fill:" + Styles.MUTED + ";");

        Label ptsLabel = new Label("⭐  " + pts + " points this session");
        ptsLabel.setStyle(
            "-fx-font-size:12px;-fx-font-weight:bold;" +
            "-fx-text-fill:" + Styles.ORANGE + ";");

        Pane[] pb = UIHelper.progressBarMutable();
        pb[1].prefWidthProperty().bind(pb[0].widthProperty().multiply(pct / 100.0));
        StackPane pbStack = (StackPane) pb[2];

        VBox summaryCard = UIHelper.card(pctLabel, pctSub, pbStack, ptsLabel);
        summaryCard.setAlignment(Pos.CENTER);
        VBox.setMargin(summaryCard, new Insets(0, 0, 8, 0));
        body.getChildren().add(summaryCard);

        // ── Mastered verbs ────────────────────────────────────────────
        body.getChildren().add(UIHelper.sectionTitle("✅  Mastered (" + masteredCount + ")"));
        FlowPane masteredPane = new FlowPane(8, 8);
        List<Verb> masteredVerbs = allVerbs.stream()
            .filter(v -> mastered.contains(v.getId())).toList();

        if (masteredVerbs.isEmpty()) {
            body.getChildren().add(UIHelper.emptyNote("No verbs mastered yet. Keep practicing!"));
        } else {
            for (Verb v : masteredVerbs) {
                masteredPane.getChildren().add(UIHelper.chip(v.getEnglish()));
            }
            body.getChildren().add(masteredPane);
        }

        // ── To review ─────────────────────────────────────────────────
        List<Verb> toReview = allVerbs.stream()
            .filter(v -> !mastered.contains(v.getId())).toList();
        body.getChildren().add(UIHelper.sectionTitle("🔁  To Review (" + toReview.size() + ")"));
        FlowPane reviewPane = new FlowPane(8, 8);

        if (toReview.isEmpty()) {
            body.getChildren().add(UIHelper.emptyNote("🎉  Congratulations! You mastered all verbs."));
        } else {
            for (Verb v : toReview) {
                reviewPane.getChildren().add(UIHelper.chip(v.getEnglish()));
            }
            body.getChildren().add(reviewPane);
        }

        // ── Practice button ───────────────────────────────────────────
        Button practicarBtn = new Button("🚀  Practice again");
        practicarBtn.setStyle(Styles.BTN_PRIMARY);
        practicarBtn.setMaxWidth(Double.MAX_VALUE);
        practicarBtn.setOnAction(e -> {
            nav.invalidate("flashcards");
            nav.show("flashcards");
        });
        VBox.setMargin(practicarBtn, new Insets(12, 0, 0, 0));
        body.getChildren().add(practicarBtn);

        scroll.setContent(body);
        root.getChildren().addAll(topBar, scroll);
        return root;
    }
}
