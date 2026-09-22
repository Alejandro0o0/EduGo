package com.edugo.data;

import com.edugo.model.User;

import java.util.HashSet;
import java.util.Set;

/**
 * Holds mutable session state.
 *
 * - setCurrentUser() restores the student's progress from progreso.txt on login.
 * - markMastered() persists immediately to progreso.txt so Teacher/Progress
 *   screens always see up-to-date data.
 */
// Clase que mantiene el estado mutable de la sesión de usuario
public class AppState {
    // Usuario actualmente autenticado
    private static User currentUser;
    // Conjunto de IDs de verbos dominados por el estudiante
    private static final Set<Integer> masteredVerbIds = new HashSet<>();
    // Puntos acumulados en la sesión de flashcards
    private static int flashcardPoints = 0;

    // ── Autenticación ─────────────────────────────────────────────────
    // Retorna el usuario actualmente autenticado
    public static User getCurrentUser() { return currentUser; }

    // Establece el usuario actual y restaura su progreso guardado
    public static void setCurrentUser(User u) {
        currentUser = u;
        masteredVerbIds.clear();
        flashcardPoints = 0;
        // Restaurar progreso guardado cuando un estudiante inicia sesión
        if (u != null && u.getRole() == User.Role.STUDENT) {
            Set<Integer> saved = DataManager.loadProgress(u.getUsername());
            masteredVerbIds.addAll(saved);
            // Dar puntos por verbos ya dominados
            flashcardPoints = saved.size() * 10;
        }
    }

    // ── Progreso ─────────────────────────────────────────────────────
    // Retorna el conjunto de IDs de verbos dominados
    public static Set<Integer> getMasteredVerbIds() { return masteredVerbIds; }

    // Marca un verbo como dominado y lo persiste inmediatamente
    public static void markMastered(int verbId) {
        masteredVerbIds.add(verbId);
        // Persistir inmediatamente para que el panel del profesor y la pantalla de progreso vean datos en tiempo real
        if (currentUser != null) {
            DataManager.saveProgress(currentUser.getUsername(), masteredVerbIds);
        }
    }

    // Retorna los puntos actuales de flashcards
    public static int  getPoints()      { return flashcardPoints; }
    // Añade puntos al total de flashcards
    public static void addPoints(int p) { flashcardPoints += p; }

    // Reinicia la sesión de usuario (cierra sesión)
    public static void resetSession() {
        currentUser = null;
        masteredVerbIds.clear();
        flashcardPoints = 0;
    }
}
