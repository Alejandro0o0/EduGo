package com.edugo;

import com.edugo.controller.ScreenManager;
import com.edugo.data.AppData;
import com.edugo.view.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * EduGo entry point.
 *
 * HOW TO RUN:
 *   mvn javafx:run
 *   or from IntelliJ: right-click Main.java → Run 'Main.main()'
 *
 * STARTUP FLOW:
 *   1. AppData.init()    → loads users and verbs from data/TXT files,
 *                          then builds SENTENCES from verb conjugations
 *   2. ScreenManager     → lazy-registered screens (built on first navigation)
 *   3. nav.show("login") → shows the login screen
 *
 * REGISTERED SCREENS:
 *   "login"      → LoginScreen
 *   "student"    → StudentDashboard
 *   "teacher"    → TeacherDashboard
 *   "admin"      → AdminDashboard
 *   "flashcards" → FlashcardsScreen
 *   "builder"    → SentenceBuilderScreen
 *   "progress"   → ProgressScreen
 *
 * FIXED SIZE: 420 × 720 px (simulates a mobile screen on desktop)
 */
// Punto de entrada principal de la aplicación EduGo
public class Main extends Application {

    // Ancho fijo de la ventana (simula pantalla móvil)
    public static final double WINDOW_WIDTH  = 420;
    // Alto fijo de la ventana (simula pantalla móvil)
    public static final double WINDOW_HEIGHT = 720;

    // Método de inicio de la aplicación JavaFX
    @Override
    public void start(Stage stage) {
        AppData.init();

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color:#EEF2F9;");

        ScreenManager nav = new ScreenManager(root);

        nav.register("login",      () -> LoginScreen.build(nav));
        nav.register("student",    () -> StudentDashboard.build(nav));
        nav.register("teacher",    () -> TeacherDashboard.build(nav));
        nav.register("admin",      () -> AdminDashboard.build(nav));
        nav.register("flashcards", () -> new FlashcardsScreen(nav).build());
        nav.register("builder",    () -> new SentenceBuilderScreen(nav).build());
        nav.register("progress",   () -> ProgressScreen.build(nav));

        // Always rebuild screens that read live data from TXT files
        nav.registerOnShow("teacher",  () -> nav.invalidate("teacher"));
        nav.registerOnShow("progress", () -> nav.invalidate("progress"));
        nav.registerOnShow("admin",    () -> nav.invalidate("admin"));
        // Student dashboard reads anuncios.txt live → rebuild on each visit
        nav.registerOnShow("student",  () -> nav.invalidate("student"));

        nav.show("login");

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        stage.setTitle("EduGo – Learn English");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    // Método main que lanza la aplicación
    public static void main(String[] args) { launch(args); }
}
