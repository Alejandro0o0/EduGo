package com.edugo.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Simple TTS helper that uses the OS speech command where available,
 * with no external Maven dependency.
 *
 * macOS  → "say -v Samantha -r 150 <text>"     (English voice forced)
 * Linux  → "espeak -v en-us -s 130 <text>"     (English voice forced)
 * Windows→ PowerShell SpeechSynthesizer, explicitly selecting an English voice
 *
 * It always speaks the real word (e.g. "practice"), never the IPA symbols.
 * The English voice is forced on purpose: if the computer's system language
 * is Spanish, the default voice would try to read the English word using
 * Spanish pronunciation rules and sound wrong. If the forced English voice
 * isn't installed on the machine, it silently falls back to the system's
 * default voice so something is still spoken.
 *
 * All speech runs on a daemon thread so it never blocks the UI.
 */
// Clase helper para Text-to-Speech (conversión de texto a voz)
public class TTS {
    // Pool de hilos para ejecutar TTS sin bloquear la UI
    private static final ExecutorService POOL = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "tts-thread");
        t.setDaemon(true);
        return t;
    });

    // Pronuncia el texto dado usando voz en inglés
    public static void speak(String text) {
        POOL.submit(() -> {
            String os = System.getProperty("os.name", "").toLowerCase();
            if (!runWithEnglishVoice(os, text)) {
                runWithDefaultVoice(os, text);
            }
        });
    }

    /** Intenta pronunciar usando una voz forzada a inglés. Retorna false si falla. */
    private static boolean runWithEnglishVoice(String os, String text) {
        try {
            ProcessBuilder pb;
            if (os.contains("mac")) {
                pb = new ProcessBuilder("say", "-v", "Samantha", "-r", "150", text);
            } else if (os.contains("linux")) {
                pb = new ProcessBuilder("espeak", "-v", "en-us", "-s", "130", text);
            } else {
                String script = String.format(
                    "Add-Type -AssemblyName System.speech;" +
                    "$s=New-Object System.Speech.Synthesis.SpeechSynthesizer;" +
                    "$s.SelectVoiceByHints('NotSet','NotSet',0,[System.Globalization.CultureInfo]::GetCultureInfo('en-US'));" +
                    "$s.Rate=-2;$s.Speak('%s');", text.replace("'", ""));
                pb = new ProcessBuilder("powershell", "-Command", script);
            }
            pb.inheritIO();
            Process proc = pb.start();
            return proc.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /** Fallback: pronunciar con la voz predeterminada del sistema operativo. */
    private static void runWithDefaultVoice(String os, String text) {
        try {
            ProcessBuilder pb;
            if (os.contains("mac")) {
                pb = new ProcessBuilder("say", "-r", "150", text);
            } else if (os.contains("linux")) {
                pb = new ProcessBuilder("espeak", "-s", "130", text);
            } else {
                String script = String.format(
                    "Add-Type -AssemblyName System.speech;" +
                    "$s=New-Object System.Speech.Synthesis.SpeechSynthesizer;" +
                    "$s.Rate=-2;$s.Speak('%s');", text.replace("'", ""));
                pb = new ProcessBuilder("powershell", "-Command", script);
            }
            pb.inheritIO();
            pb.start().waitFor();
        } catch (Exception e) {
            // TTS not available on this machine – silently ignore
        }
    }
}
