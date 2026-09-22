package com.edugo.view;

import com.edugo.controller.ScreenManager;
import com.edugo.data.AppData;
import com.edugo.data.AppState;
import com.edugo.model.User;
import com.edugo.util.Styles;
import com.edugo.util.UIHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

// Pantalla de inicio de sesión de la aplicación
public class LoginScreen {

    // Construye la pantalla de login
    public static VBox build(ScreenManager nav) {
        VBox root = new VBox();
        root.setStyle("-fx-background-color:" + Styles.BG + ";");
        root.setFillWidth(true);

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:transparent;-fx-background:transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox content = new VBox();
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(40, 24, 30, 24));
        content.setSpacing(0);
        content.setStyle("-fx-background-color:" + Styles.BG + ";");

        Text edu = new Text("Edu");
        edu.setStyle("-fx-font-weight:800;-fx-font-size:40px;");
        edu.setFill(Color.web(Styles.BLUE));
        Text go = new Text("Go");
        go.setStyle("-fx-font-weight:800;-fx-font-size:40px;");
        go.setFill(Color.web(Styles.ORANGE));
        TextFlow logo = new TextFlow(edu, go);
        logo.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Label tagline = new Label("Learn English interactively");
        tagline.setStyle(
            "-fx-text-fill:" + Styles.MUTED + ";" +
            "-fx-font-size:13px;" +
            "-fx-font-style:italic;");
        VBox.setMargin(logo,    new Insets(0, 0, 4, 0));
        VBox.setMargin(tagline, new Insets(0, 0, 32, 0));

        VBox card = new VBox(16);
        card.setStyle(Styles.CARD);
        card.setPadding(new Insets(24));

        Label cardTitle = new Label("Sign in");
        cardTitle.setStyle(
            "-fx-font-size:18px;-fx-font-weight:bold;" +
            "-fx-text-fill:" + Styles.TEXT + ";");
        VBox.setMargin(cardTitle, new Insets(0, 0, 4, 0));

        TextField userField = new TextField();
        userField.setPromptText("Username");

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");

        Label errorLabel = new Label();
        errorLabel.setStyle(
            "-fx-background-color:#FEF2F2;" +
            "-fx-text-fill:" + Styles.RED + ";" +
            "-fx-border-color:#FCA5A5;" +
            "-fx-border-radius:10;" +
            "-fx-background-radius:10;" +
            "-fx-font-weight:bold;" +
            "-fx-font-size:12px;" +
            "-fx-padding:9 12 9 12;");
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(Double.MAX_VALUE);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        Button loginBtn = new Button("Sign in  →");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle(Styles.BTN_PRIMARY);

        Runnable doLogin = () -> {
            String user = userField.getText().trim();
            String pass = passField.getText();
            User found = AppData.USERS.stream()
                .filter(u -> u.getUsername().equals(user) && u.getPassword().equals(pass))
                .findFirst().orElse(null);

            if (found == null) {
                errorLabel.setText("Incorrect username or password.");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                userField.setStyle(Styles.INPUT +
                    "-fx-border-color:" + Styles.RED + ";");
            } else {
                AppState.setCurrentUser(found);
                errorLabel.setVisible(false);
                errorLabel.setManaged(false);
                userField.setStyle(Styles.INPUT);
                switch (found.getRole()) {
                    case STUDENT -> nav.show("student");
                    case TEACHER -> nav.show("teacher");
                    case ADMIN   -> nav.show("admin");
                }
            }
        };

        loginBtn.setOnAction(e -> doLogin.run());
        passField.setOnAction(e -> doLogin.run());

        Button createAccountBtn = new Button("Create Account");
        createAccountBtn.setStyle(
            "-fx-background-color:transparent;" +
            "-fx-text-fill:" + Styles.BLUE + ";" +
            "-fx-font-weight:bold;" +
            "-fx-font-size:13px;" +
            "-fx-cursor:hand;" +
            "-fx-padding:8 0 8 0;");
        createAccountBtn.setMaxWidth(Double.MAX_VALUE);
        createAccountBtn.setOnAction(e -> showRegistrationForm(nav, root));

        card.getChildren().addAll(
            cardTitle,
            UIHelper.labelledField("Username", userField),
            UIHelper.labelledField("Password", passField),
            errorLabel,
            loginBtn,
            createAccountBtn
        );

        content.getChildren().addAll(logo, tagline, card);
        scroll.setContent(content);
        root.getChildren().add(scroll);
        return root;
    }

    // Muestra el formulario de registro
    private static void showRegistrationForm(ScreenManager nav, VBox root) {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:transparent;-fx-background:transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox content = new VBox();
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(40, 24, 30, 24));
        content.setSpacing(0);
        content.setStyle("-fx-background-color:" + Styles.BG + ";");

        Text edu = new Text("Edu");
        edu.setStyle("-fx-font-weight:800;-fx-font-size:40px;");
        edu.setFill(Color.web(Styles.BLUE));
        Text go = new Text("Go");
        go.setStyle("-fx-font-weight:800;-fx-font-size:40px;");
        go.setFill(Color.web(Styles.ORANGE));
        TextFlow logo = new TextFlow(edu, go);
        logo.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Label tagline = new Label("Create your account");
        tagline.setStyle(
            "-fx-text-fill:" + Styles.MUTED + ";" +
            "-fx-font-size:13px;" +
            "-fx-font-style:italic;");
        VBox.setMargin(logo,    new Insets(0, 0, 4, 0));
        VBox.setMargin(tagline, new Insets(0, 0, 32, 0));

        VBox card = new VBox(16);
        card.setStyle(Styles.CARD);
        card.setPadding(new Insets(24));

        Label cardTitle = new Label("Sign up");
        cardTitle.setStyle(
            "-fx-font-size:18px;-fx-font-weight:bold;" +
            "-fx-text-fill:" + Styles.TEXT + ";");
        VBox.setMargin(cardTitle, new Insets(0, 0, 4, 0));

        TextField userField = new TextField();
        userField.setPromptText("Username");

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");

        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Full Name");

        TextField courseField = new TextField();
        courseField.setPromptText("Course (optional)");

        Label errorLabel = new Label();
        errorLabel.setStyle(
            "-fx-background-color:#FEF2F2;" +
            "-fx-text-fill:" + Styles.RED + ";" +
            "-fx-border-color:#FCA5A5;" +
            "-fx-border-radius:10;" +
            "-fx-background-radius:10;" +
            "-fx-font-weight:bold;" +
            "-fx-font-size:12px;" +
            "-fx-padding:9 12 9 12;");
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(Double.MAX_VALUE);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        Button registerBtn = new Button("Create Account  →");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setStyle(Styles.BTN_PRIMARY);

        Button backBtn = new Button("← Back to Login");
        backBtn.setStyle(
            "-fx-background-color:transparent;" +
            "-fx-text-fill:" + Styles.MUTED + ";" +
            "-fx-font-weight:bold;" +
            "-fx-font-size:13px;" +
            "-fx-cursor:hand;" +
            "-fx-padding:8 0 8 0;");
        backBtn.setMaxWidth(Double.MAX_VALUE);

        Runnable doRegister = () -> {
            String username = userField.getText().trim();
            String password = passField.getText();
            String fullName = fullNameField.getText().trim();
            String course = courseField.getText().trim();

            if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                errorLabel.setText("Username, password and full name are required.");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                return;
            }

            // Verificar si el usuario ya existe
            boolean exists = com.edugo.data.AppData.USERS.stream()
                .anyMatch(u -> u.getUsername().equals(username));
            if (exists) {
                errorLabel.setText("Username \"" + username + "\" already exists.");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                return;
            }

            // Crear nuevo usuario con rol STUDENT por defecto
            com.edugo.model.User newUser = new com.edugo.model.User(
                username, password, com.edugo.model.User.Role.STUDENT, fullName, course
            );
            com.edugo.data.AppData.USERS.add(newUser);
            com.edugo.data.DataManager.saveUsers(com.edugo.data.AppData.USERS);

            // Mostrar mensaje de éxito y volver al login
            errorLabel.setStyle(
                "-fx-background-color:#ECFDF5;" +
                "-fx-text-fill:#16A34A;" +
                "-fx-border-color:#86EFAC;" +
                "-fx-border-radius:10;" +
                "-fx-background-radius:10;" +
                "-fx-font-weight:bold;" +
                "-fx-font-size:12px;" +
                "-fx-padding:9 12 9 12;");
            errorLabel.setText("Account created successfully! Please login.");
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);

            // Volver al login después de un breve retraso
            javafx.application.Platform.runLater(() -> {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                nav.show("login");
            });
        };

        registerBtn.setOnAction(e -> doRegister.run());
        backBtn.setOnAction(e -> {
            nav.invalidate("login");
            nav.show("login");
        });

        card.getChildren().addAll(
            cardTitle,
            UIHelper.labelledField("Username", userField),
            UIHelper.labelledField("Password", passField),
            UIHelper.labelledField("Full Name", fullNameField),
            UIHelper.labelledField("Course", courseField),
            errorLabel,
            registerBtn,
            backBtn
        );

        content.getChildren().addAll(logo, tagline, card);
        scroll.setContent(content);
        root.getChildren().setAll(scroll);
    }
}
