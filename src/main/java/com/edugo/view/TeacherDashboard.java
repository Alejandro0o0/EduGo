package com.edugo.view;

import com.edugo.controller.ScreenManager;
import com.edugo.data.AppData;
import com.edugo.data.AppState;
import com.edugo.data.DataManager;
import com.edugo.model.User;
import com.edugo.model.Verb;
import com.edugo.util.Styles;
import com.edugo.util.UIHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Teacher Dashboard.
 *
 * ANNOUNCEMENTS: fully backed by data/anuncios.txt.
 *   - Teacher writes a message → saved to anuncios.txt via DataManager.saveAnnouncement().
 *   - List of existing announcements is shown with delete buttons.
 *   - StudentDashboard reads the same file live so students see updates immediately.
 *
 * VERBS: add-verb form now requests presentForm/pastForm/futureForm (conjugations)
 *   which are stored in verbos.txt columns 7-9.
 *   AppData.rebuildSentences() is called after each add/delete so SentenceBuilder
 *   uses the updated conjugations as trap words.
 */
// Panel de control del profesor
public class TeacherDashboard {

    // Construye el panel de control del profesor
    public static VBox build(ScreenManager nav) {
        VBox root = new VBox();
        root.setStyle("-fx-background-color:" + Styles.BG + ";");
        root.setFillWidth(true);

        HBox topBar = UIHelper.topBar("EduGo", "Teacher Panel", () -> {
            AppState.resetSession();
            nav.invalidate("teacher");
            nav.show("login");
        });
        ((Button)((HBox)topBar).getChildren().get(0)).setText("🏠");

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
        Label greet = new Label("Hi, " + name + " · " + course);
        greet.setStyle("-fx-font-size:13px;-fx-text-fill:" + Styles.MUTED + ";");

        Label studentNumLabel = new Label();
        Label verbNumLabel    = new Label();
        refreshStatLabels(studentNumLabel, verbNumLabel);

        VBox s1 = statBox(studentNumLabel, "Active students",  "#0F766E");
        VBox s2 = statBox(verbNumLabel,    "Verbs in catalog", Styles.BLUE);
        HBox stats = new HBox(10, s1, s2);
        HBox.setHgrow(s1, Priority.ALWAYS);
        HBox.setHgrow(s2, Priority.ALWAYS);

        VBox[] verbsPaneRef = {null};
        VBox[] annPaneRef   = {null};
        VBox classPane             = buildClassPane();
        verbsPaneRef[0]            = buildVerbsPane(studentNumLabel, verbNumLabel, verbsPaneRef);
        annPaneRef[0]              = buildAnnouncementsPane(annPaneRef);

        ToggleButton t1 = tabBtn("My class");
        ToggleButton t2 = tabBtn("Verbs");
        ToggleButton t3 = tabBtn("Announcements");
        ToggleGroup tg = new ToggleGroup();
        t1.setToggleGroup(tg); t2.setToggleGroup(tg); t3.setToggleGroup(tg);
        t1.setSelected(true);

        HBox tabBar = new HBox(4, t1, t2, t3);
        tabBar.setStyle("-fx-background-color:#F1F4F9;-fx-background-radius:12;-fx-padding:4;");
        HBox.setHgrow(t1, Priority.ALWAYS);
        HBox.setHgrow(t2, Priority.ALWAYS);
        HBox.setHgrow(t3, Priority.ALWAYS);

        StackPane tabContent = new StackPane(classPane, verbsPaneRef[0], annPaneRef[0]);
        verbsPaneRef[0].setVisible(false);
        annPaneRef[0].setVisible(false);

        t1.setOnAction(e -> { show(classPane, verbsPaneRef[0], annPaneRef[0]); styleTab(t1,t2,t3); });
        t2.setOnAction(e -> { show(verbsPaneRef[0], classPane, annPaneRef[0]); styleTab(t2,t1,t3); });
        t3.setOnAction(e -> { show(annPaneRef[0], classPane, verbsPaneRef[0]); styleTab(t3,t1,t2); });
        styleTab(t1, t2, t3);

        body.getChildren().addAll(greet, stats, tabBar, tabContent);
        scroll.setContent(body);
        root.getChildren().addAll(topBar, scroll);
        return root;
    }

    // ── Panel de clase ─────────────────────────────────────────────────
    // Construye el panel de información de la clase
    private static VBox buildClassPane() {
        VBox pane = new VBox(8);

        Map<String, Set<Integer>> allProgress = DataManager.loadAllProgress();
        int totalVerbs = DataManager.loadVerbs().size();
        List<User> students = DataManager.loadUsers().stream()
            .filter(u -> u.getRole() == User.Role.STUDENT).toList();

        Label header = new Label("Group 3 — Technical English");
        header.setStyle("-fx-font-weight:bold;-fx-font-size:14px;-fx-text-fill:" + Styles.TEXT + ";");
        Label badge = new Label(students.size() + " student" + (students.size() != 1 ? "s" : ""));
        badge.setStyle(
            "-fx-background-color:#ECFDF5;-fx-text-fill:#16A34A;" +
            "-fx-background-radius:20;-fx-padding:3 9 3 9;" +
            "-fx-font-size:11px;-fx-font-weight:bold;");
        HBox headerRow = new HBox(8, header, badge);
        HBox.setHgrow(header, Priority.ALWAYS);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        VBox studentList = new VBox(0);
        if (students.isEmpty()) {
            Label empty = new Label("No students registered yet.");
            empty.setStyle("-fx-font-size:12px;-fx-text-fill:" + Styles.MUTED + ";-fx-padding:8 0 8 0;");
            studentList.getChildren().add(empty);
        } else {
            for (User u : students) {
                Set<Integer> prog = allProgress.getOrDefault(u.getUsername(), Set.of());
                int mastered = prog.size();
                int pct = totalVerbs > 0 ? (mastered * 100 / totalVerbs) : 0;
                studentList.getChildren().add(
                    studentRow(initials(u.getFullName()), u.getFullName(),
                               mastered + "/" + totalVerbs + " verbs mastered", pct));
            }
        }
        pane.getChildren().add(UIHelper.card(headerRow, studentList));
        return pane;
    }

    // ── Panel de verbos ─────────────────────────────────────────────────
    // Construye el panel de gestión de verbos
    private static VBox buildVerbsPane(Label studentLbl, Label verbLbl, VBox[] paneRef) {
        VBox pane = new VBox(10);

        Label countLbl = new Label(DataManager.loadVerbs().size() + " verbs available for practice.");
        countLbl.setStyle("-fx-font-size:12px;-fx-text-fill:" + Styles.MUTED + ";");

        FlowPane chips = new FlowPane(8, 8);
        rebuildVerbChips(chips, studentLbl, verbLbl, countLbl);

        Label addTitle = new Label("Add new verb");
        addTitle.setStyle("-fx-font-weight:bold;-fx-font-size:13px;-fx-text-fill:" + Styles.TEXT + ";");

        TextField fEn   = new TextField(); fEn.setPromptText("English (e.g. Jump)");          fEn.setStyle(Styles.INPUT);
        TextField fEs   = new TextField(); fEs.setPromptText("Spanish (e.g. Saltar)");         fEs.setStyle(Styles.INPUT);
        TextField fIpa  = new TextField(); fIpa.setPromptText("IPA (e.g. /dʒʌmp/)");          fIpa.setStyle(Styles.INPUT);
        TextField fPron = new TextField(); fPron.setPromptText("Simplified pronunciation (e.g. dyump)"); fPron.setStyle(Styles.INPUT);
        // Conjugation fields — stored in verbos.txt cols 7-9
        TextField fPres = new TextField(); fPres.setPromptText("3rd person present (e.g. jumps)");  fPres.setStyle(Styles.INPUT);
        TextField fPast = new TextField(); fPast.setPromptText("Simple past (e.g. jumped)");        fPast.setStyle(Styles.INPUT);
        TextField fFut  = new TextField(); fFut.setPromptText("Gerund / future (e.g. jumping)");    fFut.setStyle(Styles.INPUT);

        // Image picker
        TextField fImg = new TextField();
        fImg.setPromptText("No image selected (optional)");
        fImg.setStyle(Styles.INPUT);
        fImg.setEditable(false);
        fImg.setFocusTraversable(false);
        HBox.setHgrow(fImg, Priority.ALWAYS);

        ImageView imgPreview = new ImageView();
        imgPreview.setFitWidth(36); imgPreview.setFitHeight(36); imgPreview.setPreserveRatio(true);
        imgPreview.setVisible(false); imgPreview.setManaged(false);
        StackPane previewBox = new StackPane(imgPreview);
        previewBox.setMinSize(40, 40); previewBox.setMaxSize(40, 40);
        previewBox.setStyle("-fx-background-color:#F1F4F9;-fx-background-radius:8;");

        Button chooseImgBtn = new Button("📁 Choose from project images");
        chooseImgBtn.setStyle(Styles.BTN_OUTLINE);

        Button clearImgBtn = new Button("×");
        clearImgBtn.setStyle(
            "-fx-background-color:transparent;-fx-text-fill:" + Styles.RED + ";" +
            "-fx-font-weight:bold;-fx-font-size:15px;-fx-cursor:hand;-fx-padding:0 4 0 4;");
        clearImgBtn.setVisible(false); clearImgBtn.setManaged(false);

        Runnable clearImg = () -> {
            fImg.clear();
            imgPreview.setImage(null);
            imgPreview.setVisible(false); imgPreview.setManaged(false);
            clearImgBtn.setVisible(false); clearImgBtn.setManaged(false);
        };

        chooseImgBtn.setOnAction(e -> {
            try {
                File imagesRoot = new File("images").getCanonicalFile();
                if (!imagesRoot.exists()) imagesRoot.mkdirs();
                File verbosDir = new File(imagesRoot, "verbos");
                FileChooser fc = new FileChooser();
                fc.setTitle("Select an image from the project's images folder");
                fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image files", "*.png","*.jpg","*.jpeg","*.gif"));
                fc.setInitialDirectory(verbosDir.isDirectory() ? verbosDir : imagesRoot);
                Window owner = chooseImgBtn.getScene() != null ? chooseImgBtn.getScene().getWindow() : null;
                File selected = fc.showOpenDialog(owner);
                if (selected == null) return;
                File selCan = selected.getCanonicalFile();
                String root = imagesRoot.getPath();
                if (!selCan.getPath().equals(root) && !selCan.getPath().startsWith(root + File.separator)) {
                    Alert warn = new Alert(Alert.AlertType.WARNING,
                        "You can only select images already inside the project's \"images\" folder.", ButtonType.OK);
                    warn.setHeaderText(null); warn.showAndWait(); return;
                }
                Path projectRoot = new File(".").getCanonicalFile().toPath();
                String relPath = projectRoot.relativize(selCan.toPath()).toString().replace(File.separatorChar, '/');
                fImg.setText(relPath);
                Image preview = new Image(selCan.toURI().toString(), 36, 36, true, true);
                imgPreview.setImage(preview);
                imgPreview.setVisible(true); imgPreview.setManaged(true);
                clearImgBtn.setVisible(true); clearImgBtn.setManaged(true);
            } catch (IOException ex) {
                Alert err = new Alert(Alert.AlertType.ERROR, "Could not read image:\n" + ex.getMessage(), ButtonType.OK);
                err.setHeaderText(null); err.showAndWait();
            }
        });
        clearImgBtn.setOnAction(e -> clearImg.run());
        HBox imgRow = new HBox(8, previewBox, fImg, chooseImgBtn, clearImgBtn);
        imgRow.setAlignment(Pos.CENTER_LEFT);

        Label errLbl = new Label();
        errLbl.setStyle("-fx-text-fill:" + Styles.RED + ";-fx-font-size:12px;");
        errLbl.setVisible(false); errLbl.setManaged(false);

        Button addBtn = new Button("+ Add verb");
        addBtn.setStyle(Styles.BTN_PRIMARY);
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> {
            String en   = fEn.getText().trim();
            String es   = fEs.getText().trim();
            String ipa  = fIpa.getText().trim();
            String pron = fPron.getText().trim();
            String pres = fPres.getText().trim();
            String past = fPast.getText().trim();
            String fut  = fFut.getText().trim();
            String img  = fImg.getText().trim();
            if (en.isEmpty() || es.isEmpty()) {
                errLbl.setText("English and Spanish fields are required.");
                errLbl.setVisible(true); errLbl.setManaged(true); return;
            }
            errLbl.setVisible(false); errLbl.setManaged(false);
            if (ipa.isEmpty())  ipa  = "/" + en.toLowerCase() + "/";
            if (pron.isEmpty()) pron = en.toLowerCase();
            if (pres.isEmpty()) pres = en.toLowerCase() + "s";
            if (past.isEmpty()) past = en.toLowerCase() + "ed";
            if (fut.isEmpty())  fut  = en.toLowerCase() + "ing";
            if (img.isEmpty())  img  = "images/verbos/" + en.toLowerCase() + ".png";
            Verb nv = new Verb(AppData.nextVerbId(), en, es, img, ipa, pron, pres, past, fut);
            AppData.VERBS.add(nv);
            DataManager.saveVerbs(AppData.VERBS);
            AppData.rebuildSentences(); // update sentence builder with new verb
            countLbl.setText(DataManager.loadVerbs().size() + " verbs available for practice.");
            rebuildVerbChips(chips, studentLbl, verbLbl, countLbl);
            refreshStatLabels(studentLbl, verbLbl);
            fEn.clear(); fEs.clear(); fIpa.clear(); fPron.clear();
            fPres.clear(); fPast.clear(); fFut.clear();
            clearImg.run();
        });

        pane.getChildren().add(UIHelper.card(countLbl, chips));
        pane.getChildren().add(UIHelper.card(addTitle, fEn, fEs, fIpa, fPron, fPres, fPast, fFut, imgRow, errLbl, addBtn));
        return pane;
    }

    // Reconstruye los chips de verbos después de cambios
    private static void rebuildVerbChips(FlowPane chips, Label studentLbl, Label verbLbl, Label countLbl) {
        chips.getChildren().clear();
        for (Verb v : AppData.VERBS) {
            Label nameLbl = new Label(v.getEnglish());
            nameLbl.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:" + Styles.TEXT + ";");
            Button del = new Button("×");
            del.setStyle(
                "-fx-background-color:transparent;-fx-text-fill:" + Styles.RED + ";" +
                "-fx-font-weight:bold;-fx-font-size:13px;-fx-cursor:hand;-fx-padding:0 0 0 4;");
            del.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Remove \"" + v.getEnglish() + "\" from the verb list?",
                    ButtonType.YES, ButtonType.NO);
                confirm.setHeaderText(null);
                confirm.showAndWait().ifPresent(bt -> {
                    if (bt == ButtonType.YES) {
                        AppData.VERBS.remove(v);
                        DataManager.saveVerbs(AppData.VERBS);
                        AppData.rebuildSentences();
                        rebuildVerbChips(chips, studentLbl, verbLbl, countLbl);
                        countLbl.setText(DataManager.loadVerbs().size() + " verbs available for practice.");
                        refreshStatLabels(studentLbl, verbLbl);
                    }
                });
            });
            HBox chip = new HBox(4, nameLbl, del);
            chip.setAlignment(Pos.CENTER_LEFT);
            chip.setStyle("-fx-background-color:#F1F4F9;-fx-background-radius:20;-fx-padding:5 10 5 10;");
            chips.getChildren().add(chip);
        }
    }

    // ── Panel de anuncios — completamente respaldado en TXT ─────────────
    // Construye el panel de gestión de anuncios
    private static VBox buildAnnouncementsPane(VBox[] selfRef) {
        VBox pane = new VBox(8);
        String author = AppState.getCurrentUser() != null
            ? AppState.getCurrentUser().getFullName() : "Teacher";

        Label hint = new Label("Write an announcement for your students:");
        hint.setStyle("-fx-font-size:12px;-fx-text-fill:" + Styles.MUTED + ";");

        TextField field = new TextField();
        field.setPromptText("e.g. Practice the verbs before the exam");
        field.setStyle(Styles.INPUT);

        Label errLbl = new Label();
        errLbl.setStyle("-fx-text-fill:" + Styles.RED + ";-fx-font-size:12px;");
        errLbl.setVisible(false); errLbl.setManaged(false);

        // List of current announcements (refreshed on each send/delete)
        VBox listBox = new VBox(6);
        refreshAnnouncementList(listBox, selfRef);

        Button sendBtn = new Button("📢  Send announcement");
        sendBtn.setStyle(Styles.BTN_PRIMARY);
        sendBtn.setMaxWidth(Double.MAX_VALUE);
        sendBtn.setOnAction(e -> {
            String msg = field.getText().trim();
            if (msg.isEmpty()) {
                errLbl.setText("Please write a message first.");
                errLbl.setVisible(true); errLbl.setManaged(true); return;
            }
            errLbl.setVisible(false); errLbl.setManaged(false);
            // Save to data/anuncios.txt — students will read it from StudentDashboard
            DataManager.saveAnnouncement(author, msg);
            field.clear();
            refreshAnnouncementList(listBox, selfRef);
        });

        pane.getChildren().addAll(UIHelper.card(hint, field, errLbl, sendBtn),
                                  UIHelper.card(new Label("Sent announcements:") {{
                                      setStyle("-fx-font-weight:bold;-fx-font-size:13px;-fx-text-fill:" + Styles.TEXT + ";");
                                  }}, listBox));
        return pane;
    }

    /** Reconstruye la lista de anuncios mostrados en el panel del profesor. */
    private static void refreshAnnouncementList(VBox listBox, VBox[] selfRef) {
        listBox.getChildren().clear();
        List<DataManager.Announcement> list = DataManager.loadAnnouncements();
        if (list.isEmpty()) {
            Label none = new Label("No announcements sent yet.");
            none.setStyle("-fx-font-size:12px;-fx-text-fill:" + Styles.MUTED + ";");
            listBox.getChildren().add(none);
            return;
        }
        // list is most-recent-first; raw file index = reversed position
        int rawSize = list.size();
        for (int i = 0; i < list.size(); i++) {
            DataManager.Announcement a = list.get(i);
            final int rawIndex = rawSize - 1 - i; // position in the file (oldest=0)

            Label msg = new Label("📌 " + a.message());
            msg.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:" + Styles.TEXT + ";");
            msg.setWrapText(true);
            HBox.setHgrow(msg, Priority.ALWAYS);

            Label meta = new Label(a.author() + "  ·  " + a.date());
            meta.setStyle("-fx-font-size:11px;-fx-text-fill:" + Styles.MUTED + ";");

            Button del = new Button("×");
            del.setStyle(
                "-fx-background-color:transparent;-fx-text-fill:" + Styles.RED + ";" +
                "-fx-font-weight:bold;-fx-font-size:14px;-fx-cursor:hand;-fx-padding:0 4 0 4;");
            del.setOnAction(e -> {
                DataManager.deleteAnnouncement(rawIndex);
                refreshAnnouncementList(listBox, selfRef);
            });

            HBox row = new HBox(8, new VBox(2, msg, meta), del);
            HBox.setHgrow(row.getChildren().get(0), Priority.ALWAYS);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-border-color:#F1F4F9;-fx-border-width:0 0 1 0;-fx-padding:6 0 6 0;");
            listBox.getChildren().add(row);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────
    // Actualiza las etiquetas de estadísticas
    private static void refreshStatLabels(Label studentLbl, Label verbLbl) {
        long sc = DataManager.loadUsers().stream()
            .filter(u -> u.getRole() == User.Role.STUDENT).count();
        studentLbl.setText(String.valueOf(sc));
        verbLbl.setText(String.valueOf(DataManager.loadVerbs().size()));
    }

    // Crea una fila de información de un estudiante
    private static HBox studentRow(String initials, String name, String sub, int pct) {
        Label avatar = new Label(initials);
        avatar.setStyle(
            "-fx-background-color:#E0E7FF;-fx-text-fill:" + Styles.BLUE + ";" +
            "-fx-font-weight:bold;-fx-font-size:13px;-fx-background-radius:50;" +
            "-fx-alignment:center;-fx-min-width:36;-fx-min-height:36;" +
            "-fx-max-width:36;-fx-max-height:36;");
        Label lName = new Label(name);
        lName.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:" + Styles.TEXT + ";");
        Label lSub = new Label(sub);
        lSub.setStyle("-fx-font-size:11px;-fx-text-fill:" + Styles.MUTED + ";");
        VBox info = new VBox(2, lName, lSub);
        HBox.setHgrow(info, Priority.ALWAYS);
        String bc = pct >= 75 ? "#ECFDF5" : pct >= 40 ? "#FFFBEB" : "#FEF2F2";
        String tc = pct >= 75 ? "#16A34A" : pct >= 40 ? "#D97706" : "#DC2626";
        Label badge = new Label(pct + "%");
        badge.setStyle("-fx-background-color:" + bc + ";-fx-text-fill:" + tc + ";" +
            "-fx-background-radius:20;-fx-padding:3 9 3 9;-fx-font-size:11px;-fx-font-weight:bold;");
        HBox row = new HBox(12, avatar, info, badge);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 0, 10, 0));
        row.setStyle("-fx-border-color:#F1F4F9;-fx-border-width:0 0 1 0;");
        return row;
    }

    // Crea una caja de estadísticas
    private static VBox statBox(Label numLabel, String label, String color) {
        numLabel.setStyle("-fx-font-size:22px;-fx-font-weight:800;-fx-text-fill:" + color + ";");
        Label l = new Label(label);
        l.setStyle("-fx-font-size:11px;-fx-text-fill:" + Styles.MUTED + ";");
        VBox box = new VBox(2, numLabel, l);
        box.setStyle(Styles.CARD);
        box.setPadding(new Insets(14));
        box.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    // Crea un botón de pestaña
    private static ToggleButton tabBtn(String text) {
        ToggleButton b = new ToggleButton(text);
        b.setMaxWidth(Double.MAX_VALUE);
        return b;
    }

    // Muestra el panel visible y oculta los demás
    private static void show(VBox visible, VBox... others) {
        visible.setVisible(true);
        for (VBox o : others) o.setVisible(false);
    }

    // Aplica estilo a la pestaña seleccionada
    private static void styleTab(ToggleButton sel, ToggleButton... others) {
        sel.setStyle(
            "-fx-background-color:white;-fx-text-fill:" + Styles.TEXT + ";" +
            "-fx-background-radius:9;-fx-font-size:12px;-fx-font-weight:bold;" +
            "-fx-padding:8 4 8 4;-fx-cursor:hand;");
        for (ToggleButton o : others)
            o.setStyle(
                "-fx-background-color:transparent;-fx-text-fill:" + Styles.MUTED + ";" +
                "-fx-background-radius:9;-fx-font-size:12px;-fx-font-weight:bold;" +
                "-fx-padding:8 4 8 4;-fx-cursor:hand;");
    }

    // Obtiene las iniciales de un nombre
    private static String initials(String name) {
        String[] p = name.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(2, p.length); i++) sb.append(p[i].charAt(0));
        return sb.toString().toUpperCase();
    }
}
