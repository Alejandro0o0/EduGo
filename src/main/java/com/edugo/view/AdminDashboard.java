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
import javafx.scene.layout.*;

/**
 * AdminDashboard — all text-fill values are now explicit on every Label
 * so that text is visible regardless of parent container effects.
 */
// Panel de control del administrador
public class AdminDashboard {

    // Construye el panel de control del administrador
    public static VBox build(ScreenManager nav) {
        VBox root = new VBox();
        root.setStyle("-fx-background-color:" + Styles.BG + ";");
        root.setFillWidth(true);

        HBox topBar = UIHelper.topBar("EduGo", "Admin Panel", () -> {
            AppState.resetSession();
            nav.invalidate("admin");
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

        String name = AppState.getCurrentUser() != null
            ? AppState.getCurrentUser().getFullName() : "";
        Label greet = new Label("Hi, " + name);
        greet.setStyle("-fx-font-size:13px;-fx-text-fill:" + Styles.MUTED + ";");

        // ── Stat cards ────────────────────────────────────────────────
        Label userNumLbl = new Label(String.valueOf(AppData.USERS.size()));
        Label verbNumLbl = new Label(String.valueOf(AppData.VERBS.size()));
        VBox s1 = statBox(userNumLbl, "Registered users", Styles.ORANGE);
        VBox s2 = statBox(verbNumLbl, "Verbs in catalog",  Styles.BLUE);
        HBox stats = new HBox(10, s1, s2);
        HBox.setHgrow(s1, Priority.ALWAYS);
        HBox.setHgrow(s2, Priority.ALWAYS);

        // ── Tabs ──────────────────────────────────────────────────────
        VBox[] usersPaneRef = {null};
        usersPaneRef[0]    = buildUsersPane(userNumLbl, usersPaneRef);
        VBox coursesPane   = buildCoursesPane();
        VBox systemPane    = buildSystemPane(verbNumLbl);

        ToggleButton t1 = tabBtn("Users");
        ToggleButton t2 = tabBtn("Courses");
        ToggleButton t3 = tabBtn("System");
        ToggleGroup tg = new ToggleGroup();
        t1.setToggleGroup(tg); t2.setToggleGroup(tg); t3.setToggleGroup(tg);
        t1.setSelected(true);

        HBox tabBar = new HBox(4, t1, t2, t3);
        tabBar.setStyle("-fx-background-color:#F1F4F9;-fx-background-radius:12;-fx-padding:4;");
        HBox.setHgrow(t1, Priority.ALWAYS);
        HBox.setHgrow(t2, Priority.ALWAYS);
        HBox.setHgrow(t3, Priority.ALWAYS);

        StackPane tabContent = new StackPane(usersPaneRef[0], coursesPane, systemPane);
        coursesPane.setVisible(false);
        systemPane.setVisible(false);

        t1.setOnAction(e -> { show(usersPaneRef[0], coursesPane, systemPane); styleTab(t1,t2,t3); });
        t2.setOnAction(e -> { show(coursesPane, usersPaneRef[0], systemPane); styleTab(t2,t1,t3); });
        t3.setOnAction(e -> { show(systemPane, usersPaneRef[0], coursesPane); styleTab(t3,t1,t2); });
        styleTab(t1, t2, t3);

        body.getChildren().addAll(greet, stats, tabBar, tabContent);
        scroll.setContent(body);
        root.getChildren().addAll(topBar, scroll);
        return root;
    }

    // ── Panel de usuarios ───────────────────────────────────────────────
    // Construye el panel de gestión de usuarios
    private static VBox buildUsersPane(Label userNumLbl, VBox[] paneRef) {
        VBox pane = new VBox(10);

        Label header = new Label("Registered users");
        header.setStyle("-fx-font-weight:bold;-fx-font-size:14px;-fx-text-fill:" + Styles.TEXT + ";");

        Label badge = new Label(AppData.USERS.size() + " users");
        badge.setStyle("-fx-background-color:#ECFDF5;-fx-text-fill:#16A34A;" +
            "-fx-background-radius:20;-fx-padding:3 9 3 9;" +
            "-fx-font-size:11px;-fx-font-weight:bold;");

        HBox hdr = new HBox(8, header, badge);
        HBox.setHgrow(header, Priority.ALWAYS);
        hdr.setAlignment(Pos.CENTER_LEFT);

        VBox list = new VBox(0);
        rebuildUserList(list, badge, userNumLbl);

        // ── Create user form ──────────────────────────────────────────
        Label addTitle = new Label("Create new user");
        addTitle.setStyle("-fx-font-weight:bold;-fx-font-size:13px;-fx-text-fill:" + Styles.TEXT + ";");

        TextField fName   = new TextField(); fName.setPromptText("Full name");               fName.setStyle(Styles.INPUT);
        TextField fUser   = new TextField(); fUser.setPromptText("Username (e.g. juan.perez)"); fUser.setStyle(Styles.INPUT);
        TextField fPass   = new TextField(); fPass.setPromptText("Password");                 fPass.setStyle(Styles.INPUT);
        TextField fCourse = new TextField(); fCourse.setPromptText("Course (optional)");      fCourse.setStyle(Styles.INPUT);

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("Student", "Teacher", "Admin");
        roleBox.setValue("Student");
        roleBox.setMaxWidth(Double.MAX_VALUE);
        roleBox.setStyle("-fx-font-size:14px;");

        Label errLbl = new Label();
        errLbl.setStyle("-fx-text-fill:" + Styles.RED + ";-fx-font-size:12px;");
        errLbl.setVisible(false); errLbl.setManaged(false);

        Button createBtn = new Button("Create user");
        createBtn.setStyle(Styles.BTN_PRIMARY);
        createBtn.setMaxWidth(Double.MAX_VALUE);
        createBtn.setOnAction(e -> {
            String fn    = fName.getText().trim();
            String uname = fUser.getText().trim();
            String pass  = fPass.getText().trim();
            if (fn.isEmpty() || uname.isEmpty() || pass.isEmpty()) {
                errLbl.setText("Name, username and password are required.");
                errLbl.setVisible(true); errLbl.setManaged(true); return;
            }
            boolean dup = AppData.USERS.stream().anyMatch(u -> u.getUsername().equals(uname));
            if (dup) {
                errLbl.setText("Username \"" + uname + "\" already exists.");
                errLbl.setVisible(true); errLbl.setManaged(true); return;
            }
            errLbl.setVisible(false); errLbl.setManaged(false);
            User.Role role = switch (roleBox.getValue()) {
                case "Teacher" -> User.Role.TEACHER;
                case "Admin"   -> User.Role.ADMIN;
                default        -> User.Role.STUDENT;
            };
            User nu = new User(uname, pass, role, fn, fCourse.getText().trim());
            AppData.USERS.add(nu);
            DataManager.saveUsers(AppData.USERS);
            rebuildUserList(list, badge, userNumLbl);
            fName.clear(); fUser.clear(); fPass.clear(); fCourse.clear();
            roleBox.setValue("Student");
        });

        pane.getChildren().add(UIHelper.card(hdr, list));
        pane.getChildren().add(UIHelper.card(addTitle, fName, fUser, fPass, fCourse, roleBox, errLbl, createBtn));
        return pane;
    }

    // Reconstruye la lista de usuarios después de cambios
    private static void rebuildUserList(VBox list, Label badge, Label userNumLbl) {
        list.getChildren().clear();
        for (User u : AppData.USERS) {
            String rl = switch (u.getRole()) {
                case STUDENT -> "Student"; case TEACHER -> "Teacher"; case ADMIN -> "Admin";
            };
            String sub = rl + (u.getCourse().isBlank() ? "" : " · " + u.getCourse());
            list.getChildren().add(userRow(initials(u.getFullName()), u.getFullName(), sub));
        }
        badge.setText(AppData.USERS.size() + " users");
        userNumLbl.setText(String.valueOf(AppData.USERS.size()));
    }

    // Crea una fila de información de un usuario
    private static HBox userRow(String initials, String name, String sub) {
        Label avatar = new Label(initials);
        avatar.setStyle(
            "-fx-background-color:#E0E7FF;" +
            "-fx-text-fill:" + Styles.BLUE + ";" +
            "-fx-font-weight:bold;" +
            "-fx-font-size:13px;" +
            "-fx-background-radius:50;" +
            "-fx-alignment:center;" +
            "-fx-min-width:36;-fx-min-height:36;" +
            "-fx-max-width:36;-fx-max-height:36;");

        Label lName = new Label(name);
        lName.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:" + Styles.TEXT + ";");

        Label lSub = new Label(sub);
        lSub.setStyle("-fx-font-size:11px;-fx-text-fill:" + Styles.MUTED + ";");

        VBox info = new VBox(2, lName, lSub);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label activeBadge = new Label("Active");
        activeBadge.setStyle(
            "-fx-background-color:#ECFDF5;-fx-text-fill:#16A34A;" +
            "-fx-background-radius:20;-fx-padding:3 9 3 9;" +
            "-fx-font-size:11px;-fx-font-weight:bold;");

        HBox row = new HBox(12, avatar, info, activeBadge);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 0, 10, 0));
        row.setStyle("-fx-border-color:#F1F4F9;-fx-border-width:0 0 1 0;");
        return row;
    }

    // ── Panel de cursos ────────────────────────────────────────────────
    // Construye el panel de información de cursos
    private static VBox buildCoursesPane() {
        VBox pane = new VBox(8);

        Label name = new Label("Group 3");
        name.setStyle("-fx-font-weight:bold;-fx-font-size:14px;-fx-text-fill:" + Styles.TEXT + ";");

        Label badge = new Label("Technical English");
        badge.setStyle("-fx-background-color:#ECFDF5;-fx-text-fill:#16A34A;" +
            "-fx-background-radius:20;-fx-padding:3 9 3 9;" +
            "-fx-font-size:11px;-fx-font-weight:bold;");

        HBox row = new HBox(8, name, badge);
        row.setAlignment(Pos.CENTER_LEFT);

        Label teacher = new Label("Teacher: Carla Mendoza Vargas");
        teacher.setStyle("-fx-font-size:12px;-fx-text-fill:" + Styles.MUTED + ";");

        pane.getChildren().add(UIHelper.card(row, teacher));
        return pane;
    }

    // ── Panel del sistema ───────────────────────────────────────────────
    // Construye el panel de configuración del sistema
    private static VBox buildSystemPane(Label verbNumLbl) {
        VBox pane = new VBox(8);

        Label verbTitle = new Label("Verb catalog");
        verbTitle.setStyle("-fx-font-weight:bold;-fx-font-size:14px;-fx-text-fill:" + Styles.TEXT + ";");

        Label verbCount = new Label(AppData.VERBS.size() + " verbs available for all courses.");
        verbCount.setStyle("-fx-font-size:12px;-fx-text-fill:" + Styles.MUTED + ";");

        FlowPane chips = new FlowPane(8, 8);
        for (Verb v : AppData.VERBS) chips.getChildren().add(UIHelper.chip(v.getEnglish()));

        Label rolesTitle = new Label("Roles and permissions");
        rolesTitle.setStyle("-fx-font-weight:bold;-fx-font-size:14px;-fx-text-fill:" + Styles.TEXT + ";");

        VBox toggles = new VBox(0,
            toggleRow("Teachers can create announcements", true),
            toggleRow("Students can change course",        false),
            toggleRow("Pronunciation audio enabled",       true));

        pane.getChildren().addAll(
            UIHelper.card(verbTitle, verbCount, chips),
            UIHelper.card(rolesTitle, toggles));
        return pane;
    }

    // Crea una fila con un toggle switch
    private static HBox toggleRow(String label, boolean on) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size:13px;-fx-text-fill:" + Styles.TEXT + ";");
        HBox.setHgrow(lbl, Priority.ALWAYS);

        final boolean[] state = {on};
        Label knob = new Label();
        knob.setStyle(knobStyle(on));
        knob.setOnMouseClicked(e -> { state[0] = !state[0]; knob.setStyle(knobStyle(state[0])); });

        HBox row = new HBox(lbl, knob);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 0, 8, 0));
        row.setStyle("-fx-border-color:#F1F4F9;-fx-border-width:0 0 1 0;");
        return row;
    }

    // Retorna el estilo del knob del toggle según su estado
    private static String knobStyle(boolean on) {
        return "-fx-background-color:" + (on ? Styles.GREEN : "#E2E8F0") + ";" +
               "-fx-background-radius:20;" +
               "-fx-min-width:40;-fx-min-height:22;" +
               "-fx-max-width:40;-fx-max-height:22;" +
               "-fx-cursor:hand;";
    }

    // ── Helpers compartidos ────────────────────────────────────────────
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
            "-fx-background-color:white;" +
            "-fx-text-fill:" + Styles.TEXT + ";" +
            "-fx-background-radius:9;" +
            "-fx-font-size:12px;-fx-font-weight:bold;" +
            "-fx-padding:8 4 8 4;-fx-cursor:hand;");
        for (ToggleButton o : others)
            o.setStyle(
                "-fx-background-color:transparent;" +
                "-fx-text-fill:" + Styles.MUTED + ";" +
                "-fx-background-radius:9;" +
                "-fx-font-size:12px;-fx-font-weight:bold;" +
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
