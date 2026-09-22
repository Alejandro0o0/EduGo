package com.edugo.controller;

import javafx.scene.layout.StackPane;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Lightweight screen router.
 * Each screen is created lazily on first navigation and cached.
 *
 * registerOnShow hooks run BEFORE the screen is retrieved from cache,
 * so calling invalidate(name) inside the hook forces a fresh rebuild.
 */
// Enrutador ligero de pantallas para la aplicación
public class ScreenManager {

    // Contenedor raíz donde se muestran las pantallas
    private final StackPane root;
    // Caché de pantallas ya construidas
    private final Map<String, javafx.scene.Node>          cache    = new HashMap<>();
    // Fábricas para construir pantallas bajo demanda
    private final Map<String, Supplier<javafx.scene.Node>> factories = new HashMap<>();
    // Hooks que se ejecutan antes de mostrar una pantalla
    private final Map<String, Runnable>                   onShow   = new HashMap<>();

    // Constructor del ScreenManager
    public ScreenManager(StackPane root) { this.root = root; }

    // Registra una pantalla con su fábrica constructora
    public void register(String name, Supplier<javafx.scene.Node> factory) {
        factories.put(name, factory);
    }

    // Registra un hook que se ejecuta antes de mostrar la pantalla
    public void registerOnShow(String name, Runnable hook) {
        onShow.put(name, hook);
    }

    // Muestra una pantalla específica
    public void show(String name) {
        // Ejecutar hook pre-show PRIMERO (permite invalidate antes de buscar en caché)
        Runnable hook = onShow.get(name);
        if (hook != null) hook.run();

        // Ahora construir (o obtener de caché) el nodo
        javafx.scene.Node node = cache.computeIfAbsent(name, k -> factories.get(k).get());
        root.getChildren().setAll(node);
    }

    /** Fuerza la recreación de una pantalla en la siguiente llamada a show(). */
    public void invalidate(String name) {
        cache.remove(name);
    }
}
