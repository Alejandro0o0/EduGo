package com.edugo.view;

import com.edugo.controller.ScreenManager;
import com.edugo.data.AppData;
import com.edugo.data.AppState;
import com.edugo.data.DataManager;
import com.edugo.util.Styles;
import com.edugo.util.UIHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Student dashboard.
 *
 * DATA SOURCES:
 *   - Name/course: AppState.getCurrentUser() (from data/usuarios.txt at login)
 *   - Announcements: DataManager.loadAnnouncements() reads data/anuncios.txt live
 *     (written by the teacher, read here in real time)
 *   - Verb count: AppData.VERBS.size() (from data/verbos.txt)
 */
// Panel de control del estudiante
public class StudentDashboard {

    // Construye el panel de control del estudiante
    public static VBox build(ScreenManager nav) {
        VBox root = new VBox();
        root.setStyle("-fx-background-color:" + Styles.BG + ";");
        root.setFillWidth(true);

        HBox topBar = UIHelper.topBar("EduGo", "Student Dashboard", () -> {
            AppState.resetSession();
            nav.invalidate("student");
            nav.invalidate("flashcards");
            nav.invalidate("builder");
            nav.invalidate("progress");
            nav.show("login");
        });

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:transparent;-fx-background:transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox body = new VBox(12);
        body.setPadding(new Insets(16));
        body.setStyle("-fx-background-color:" + Styles.BG + ";");

        String name   = AppState.getCurrentUser() != null ? AppState.getCurrentUser().getFullName() : "";
        String course = AppState.getCurrentUser() != null ? AppState.getCurrentUser().getCourse()   : "";
        int total = AppData.VERBS.size();

        Label saludo = new Label("Hello, " + name + "!");
        saludo.setStyle(
            "-fx-font-size:18px;-fx-font-weight:bold;" +
            "-fx-text-fill:" + Styles.TEXT + ";");

        Label subSaludo = new Label(course);
        subSaludo.setStyle("-fx-font-size:12px;-fx-text-fill:" + Styles.MUTED + ";");

        // ── Navigation tiles ──────────────────────────────────────────
        body.getChildren().addAll(
            saludo,
            subSaludo,

            tile("🚀", Styles.BLUE_LIGHT, Styles.BLUE,
                 "Flash Cards",
                 "Practice " + total + " verb flashcards",
                 () -> { nav.invalidate("flashcards"); nav.show("flashcards"); }),

            tile("🧩", Styles.PURPLE_LIGHT, Styles.PURPLE,
                 "Sentence Builder",
                 "Arrange words in the correct tense",
                 () -> { nav.invalidate("builder"); nav.show("builder"); }),

            tile("📊", "#ECFDF5", Styles.GREEN,
                 "My Progress",
                 "Mastered verbs and verbs to review",
                 () -> { nav.invalidate("progress"); nav.show("progress"); }),

            UIHelper.sectionTitle("Teacher Announcements")
        );

        // ── Announcements — live from data/anuncios.txt ───────────────
        List<DataManager.Announcement> announcements = DataManager.loadAnnouncements();
        if (announcements.isEmpty()) {
            Label none = new Label("No announcements yet.");
            none.setStyle("-fx-font-size:12px;-fx-text-fill:" + Styles.MUTED + ";-fx-padding:4 0 4 0;");
            body.getChildren().add(none);
        } else {
            for (DataManager.Announcement a : announcements) {
                body.getChildren().add(announcementCard(a));
            }
        }

        scroll.setContent(body);
        root.getChildren().addAll(topBar, scroll);
        return root;
    }

    // Crea una tarjeta de navegación con icono
    private static HBox tile(String icon, String iconBg, String iconColor,
                              String title, String subtitle, Runnable action) {
        Label iconLbl = new Label(icon);
        iconLbl.setStyle(
            "-fx-background-color:" + iconBg + ";" +
            "-fx-background-radius:14;" +
            "-fx-font-size:24px;" +
            "-fx-padding:10;" +
            "-fx-min-width:50;-fx-min-height:50;" +
            "-fx-max-width:50;-fx-max-height:50;" +
            "-fx-alignment:center;");

        Label lTitle = new Label(title);
        lTitle.setStyle("-fx-font-weight:bold;-fx-font-size:14px;-fx-text-fill:" + Styles.TEXT + ";");
        Label lSub = new Label(subtitle);
        lSub.setStyle("-fx-font-size:12px;-fx-text-fill:" + Styles.MUTED + ";");
        VBox txt = new VBox(2, lTitle, lSub);
        HBox.setHgrow(txt, Priority.ALWAYS);

        Label arrow = new Label("›");
        arrow.setStyle("-fx-font-size:20px;-fx-text-fill:" + Styles.MUTED + ";-fx-padding:0 4 0 0;");

        HBox tile = new HBox(14, iconLbl, txt, arrow);
        tile.setAlignment(Pos.CENTER_LEFT);
        tile.setPadding(new Insets(16));
        tile.setStyle(Styles.CARD + "-fx-cursor:hand;");
        tile.setOnMouseClicked(e -> action.run());
        tile.setOnMouseEntered(e -> tile.setStyle(Styles.CARD + "-fx-cursor:hand;-fx-translate-y:-2;"));
        tile.setOnMouseExited(e -> tile.setStyle(Styles.CARD + "-fx-cursor:hand;"));
        return tile;
    }

    /** Renderiza una tarjeta de anuncio desde data/anuncios.txt. */
    private static VBox announcementCard(DataManager.Announcement a) {
        Label title = new Label("📌  " + a.message());
        title.setStyle(
            "-fx-font-weight:bold;-fx-font-size:13px;" +
            "-fx-text-fill:" + Styles.TEXT + ";");
        title.setWrapText(true);
        Label meta = new Label(a.author() + "  ·  " + a.date());
        meta.setStyle("-fx-font-size:11px;-fx-text-fill:" + Styles.MUTED + ";");
        return UIHelper.card(title, meta);
    }
}
