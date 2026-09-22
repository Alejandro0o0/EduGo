package com.edugo.data;

import com.edugo.model.User;
import com.edugo.model.Verb;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Reads and writes all TXT files from the data/ folder.
 *
 *   usuarios.txt   →  username|password|role|fullName|course
 *   verbos.txt     →  id|english|spanish|imagePath|ipa|pronunciation|presentForm|pastForm|futureForm
 *   progreso.txt   →  username|verbId1,verbId2,...
 *   anuncios.txt   →  date|author|message
 */
// Clase que maneja la lectura y escritura de archivos de datos
public class DataManager {

    // Directorio de datos
    public static final Path DATA_DIR       = Path.of("data");
    // Archivo de usuarios
    public static final Path USUARIOS_FILE  = DATA_DIR.resolve("usuarios.txt");
    // Archivo de verbos
    public static final Path VERBOS_FILE    = DATA_DIR.resolve("verbos.txt");
    // Archivo de progreso de estudiantes
    public static final Path PROGRESO_FILE  = DATA_DIR.resolve("progreso.txt");
    // Archivo de anuncios del profesor
    public static final Path ANUNCIOS_FILE  = DATA_DIR.resolve("anuncios.txt");

    // ── USUARIOS ─────────────────────────────────────────────────────
    // Carga la lista de usuarios desde usuarios.txt
    public static List<User> loadUsers() {
        if (!Files.exists(USUARIOS_FILE)) {
            saveUsers(AppData.DEFAULT_USERS);
            return new ArrayList<>(AppData.DEFAULT_USERS);
        }
        List<User> list = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(USUARIOS_FILE, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                User u = parseUser(line);
                if (u != null) list.add(u);
            }
        } catch (IOException e) { e.printStackTrace(); }
        return list;
    }

    // Guarda la lista de usuarios en usuarios.txt
    public static void saveUsers(List<User> users) {
        ensureDir();
        try (PrintWriter pw = new PrintWriter(
                Files.newBufferedWriter(USUARIOS_FILE, StandardCharsets.UTF_8))) {
            pw.println("# username|password|role|fullName|course");
            for (User u : users) pw.println(userLine(u));
        } catch (IOException e) { e.printStackTrace(); }
    }

    // Parsea una línea de texto a un objeto User
    private static User parseUser(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length < 5) return null;
        User.Role role = switch (p[2].trim().toUpperCase()) {
            case "TEACHER" -> User.Role.TEACHER;
            case "ADMIN"   -> User.Role.ADMIN;
            default        -> User.Role.STUDENT;
        };
        return new User(p[0].trim(), p[1].trim(), role, p[3].trim(), p[4].trim());
    }

    // Convierte un objeto User a una línea de texto
    private static String userLine(User u) {
        return u.getUsername() + "|" + u.getPassword() + "|" +
               u.getRole().name() + "|" + u.getFullName() + "|" + u.getCourse();
    }

    // ── VERBOS ───────────────────────────────────────────────────────
    // Formato: id|english|spanish|imagePath|ipa|pronunciation|presentForm|pastForm|futureForm
    // Las columnas 7-9 (conjugaciones) son nuevas; archivos legados con 6 columnas obtienen valores por defecto.
    // Carga la lista de verbos desde verbos.txt
    public static List<Verb> loadVerbs() {
        if (!Files.exists(VERBOS_FILE)) {
            saveVerbs(AppData.DEFAULT_VERBS);
            return new ArrayList<>(AppData.DEFAULT_VERBS);
        }
        List<Verb> list = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(VERBOS_FILE, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                Verb v = parseVerb(line);
                if (v != null) list.add(v);
            }
        } catch (IOException e) { e.printStackTrace(); }
        return list;
    }

    // Guarda la lista de verbos en verbos.txt
    public static void saveVerbs(List<Verb> verbs) {
        ensureDir();
        try (PrintWriter pw = new PrintWriter(
                Files.newBufferedWriter(VERBOS_FILE, StandardCharsets.UTF_8))) {
            pw.println("# id|english|spanish|imagePath|ipa|pronunciation|presentForm|pastForm|futureForm");
            pw.println("# presentForm = 3rd person singular present (she/he/it)");
            pw.println("# pastForm    = simple past");
            pw.println("# futureForm  = gerund (used in continuous/future sentences)");
            for (Verb v : verbs) pw.println(verbLine(v));
        } catch (IOException e) { e.printStackTrace(); }
    }

    // Parsea una línea de texto a un objeto Verb
    private static Verb parseVerb(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length < 5) return null;
        try {
            int    id   = Integer.parseInt(p[0].trim());
            String en   = p[1].trim();
            String es   = p[2].trim();
            String img  = p[3].trim();
            String ipa  = p[4].trim();
            String pron = p.length >= 6 && !p[5].trim().isEmpty() ? p[5].trim() : en.toLowerCase();
            String pres = p.length >= 7 && !p[6].trim().isEmpty() ? p[6].trim() : en.toLowerCase() + "s";
            String past = p.length >= 8 && !p[7].trim().isEmpty() ? p[7].trim() : en.toLowerCase() + "ed";
            String fut  = p.length >= 9 && !p[8].trim().isEmpty() ? p[8].trim() : en.toLowerCase() + "ing";
            return new Verb(id, en, es, img, ipa, pron, pres, past, fut);
        } catch (NumberFormatException e) { return null; }
    }

    // Convierte un objeto Verb a una línea de texto
    private static String verbLine(Verb v) {
        return v.getId() + "|" + v.getEnglish() + "|" + v.getSpanish() + "|" +
               v.getImagePath() + "|" + v.getIpa() + "|" + v.getPronunciation() + "|" +
               v.getPresentForm() + "|" + v.getPastForm() + "|" + v.getFutureForm();
    }

    // ── PROGRESO ─────────────────────────────────────────────────────
    // Carga el progreso de todos los estudiantes desde progreso.txt
    public static Map<String, Set<Integer>> loadAllProgress() {
        Map<String, Set<Integer>> map = new LinkedHashMap<>();
        if (!Files.exists(PROGRESO_FILE)) return map;
        try (BufferedReader br = Files.newBufferedReader(PROGRESO_FILE, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int bar = line.indexOf('|');
                if (bar < 0) continue;
                String username = line.substring(0, bar).trim();
                String ids      = line.substring(bar + 1).trim();
                Set<Integer> set = new HashSet<>();
                if (!ids.isEmpty()) {
                    for (String tok : ids.split(",")) {
                        tok = tok.trim();
                        if (!tok.isEmpty()) {
                            try { set.add(Integer.parseInt(tok)); }
                            catch (NumberFormatException ignored) {}
                        }
                    }
                }
                map.put(username, set);
            }
        } catch (IOException e) { e.printStackTrace(); }
        return map;
    }

    // Carga el progreso de un estudiante específico
    public static Set<Integer> loadProgress(String username) {
        return loadAllProgress().getOrDefault(username, new HashSet<>());
    }

    // Guarda el progreso de un estudiante en progreso.txt
    public static void saveProgress(String username, Set<Integer> masteredIds) {
        ensureDir();
        Map<String, Set<Integer>> all = loadAllProgress();
        all.put(username, new HashSet<>(masteredIds));
        try (PrintWriter pw = new PrintWriter(
                Files.newBufferedWriter(PROGRESO_FILE, StandardCharsets.UTF_8))) {
            pw.println("# Student progress — username|verbId1,verbId2,...");
            pw.println("# Updated automatically when a student masters a verb in Flashcards.");
            for (Map.Entry<String, Set<Integer>> e : all.entrySet()) {
                String ids = e.getValue().stream()
                    .sorted().map(String::valueOf)
                    .collect(Collectors.joining(","));
                pw.println(e.getKey() + "|" + ids);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ── ANUNCIOS ─────────────────────────────────────────────────────
    // Formato: date|author|message
    // Escrito desde TeacherDashboard, leído en StudentDashboard — completamente respaldado en TXT.
    // Registro que representa un anuncio
    public record Announcement(String date, String author, String message) {}

    /** Carga todos los anuncios desde anuncios.txt (más reciente primero). */
    public static List<Announcement> loadAnnouncements() {
        List<Announcement> list = new ArrayList<>();
        if (!Files.exists(ANUNCIOS_FILE)) {
            ensureDir();
            try (PrintWriter pw = new PrintWriter(
                    Files.newBufferedWriter(ANUNCIOS_FILE, StandardCharsets.UTF_8))) {
                pw.println("# Teacher announcements — date|author|message");
                pw.println("# Written from the Teacher panel, read in the Student dashboard.");
            } catch (IOException e) { e.printStackTrace(); }
            return list;
        }
        try (BufferedReader br = Files.newBufferedReader(ANUNCIOS_FILE, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] p = line.split("\\|", 3);
                if (p.length < 3) continue;
                list.add(new Announcement(p[0].trim(), p[1].trim(), p[2].trim()));
            }
        } catch (IOException e) { e.printStackTrace(); }
        Collections.reverse(list); // most recent first
        return list;
    }

    /** Añade un nuevo anuncio a anuncios.txt. */
    public static void saveAnnouncement(String author, String message) {
        ensureDir();
        if (!Files.exists(ANUNCIOS_FILE)) {
            try (PrintWriter pw = new PrintWriter(
                    Files.newBufferedWriter(ANUNCIOS_FILE, StandardCharsets.UTF_8))) {
                pw.println("# Teacher announcements — date|author|message");
            } catch (IOException e) { e.printStackTrace(); }
        }
        try (PrintWriter pw = new PrintWriter(
                new BufferedWriter(new FileWriter(ANUNCIOS_FILE.toFile(), true)))) {
            pw.println(LocalDate.now() + "|" + author + "|" + message);
        } catch (IOException e) { e.printStackTrace(); }
    }

    /** Elimina un anuncio específico por su índice en la lista de datos (más antiguo=0). */
    public static void deleteAnnouncement(int dataIndex) {
        if (!Files.exists(ANUNCIOS_FILE)) return;
        List<String> comments = new ArrayList<>();
        List<String> data     = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(ANUNCIOS_FILE, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                String t = line.trim();
                if (t.startsWith("#") || t.isEmpty()) comments.add(line);
                else data.add(line);
            }
        } catch (IOException e) { e.printStackTrace(); return; }
        if (dataIndex < 0 || dataIndex >= data.size()) return;
        data.remove(dataIndex);
        try (PrintWriter pw = new PrintWriter(
                Files.newBufferedWriter(ANUNCIOS_FILE, StandardCharsets.UTF_8))) {
            for (String c : comments) pw.println(c);
            for (String d : data)     pw.println(d);
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ── Helper ───────────────────────────────────────────────────────
    // Asegura que el directorio de datos exista
    private static void ensureDir() {
        try { Files.createDirectories(DATA_DIR); }
        catch (IOException e) { e.printStackTrace(); }
    }
}
