package com.edugo.data;

import com.edugo.model.Sentence;
import com.edugo.model.User;
import com.edugo.model.Verb;

import java.util.*;

/**
 * In-memory application data for EduGo.
 *
 * SOURCES:
 *   USERS     — loaded from data/usuarios.txt on startup
 *   VERBS     — loaded from data/verbos.txt on startup
 *               Format: id|english|spanish|imagePath|ipa|pronunciation|presentForm|pastForm|futureForm
 *   SENTENCES — loaded dynamically from VERBS at startup (uses presentForm/pastForm/futureForm
 *               stored in verbos.txt as the "trap" words in the sentence builder)
 *   IPA_DICT  — hardcoded dictionary (~90 entries) for IPA pronunciation display
 */
// Datos en memoria de la aplicación EduGo
public class AppData {

    // ── Constantes de tiempos verbales ───────────────────────────────
    public static final String PRESENT = "Simple Present";
    public static final String PAST    = "Simple Past";
    public static final String FUTURE  = "Future with will";

    // ── Listas mutables respaldadas por archivos TXT ────────────────────
    // Lista de usuarios cargada desde usuarios.txt
    public static List<User>     USERS     = new ArrayList<>();
    // Lista de verbos cargada desde verbos.txt
    public static List<Verb>     VERBS     = new ArrayList<>();
    // Lista de oraciones construida dinámicamente
    public static List<Sentence> SENTENCES = new ArrayList<>();

    /** Verbos por defecto — usados solo para inicializar verbos.txt en la primera ejecución. */
    static final List<Verb> DEFAULT_VERBS = List.of(
        new Verb(1,  "Practice", "Practicar",       "images/verbos/practice.png", "/ˈpræk.tɪs/", "prák·tis",  "practices",  "practiced",  "practicing"),
        new Verb(2,  "Teach",    "Enseñar",          "images/verbos/teach.png",    "/tiːtʃ/",     "tích",      "teaches",    "taught",     "teaching"),
        new Verb(3,  "Learn",    "Aprender",         "images/verbos/learn.png",    "/lɜːrn/",     "lérn",      "learns",     "learned",    "learning"),
        new Verb(4,  "Answer",   "Responder",        "images/verbos/answer.png",   "/ˈæn.sər/",   "án·suhr",   "answers",    "answered",   "answering"),
        new Verb(5,  "Ask",      "Preguntar",        "images/verbos/ask.png",      "/æsk/",       "ásk",       "asks",       "asked",      "asking"),
        new Verb(6,  "Speak",    "Hablar",           "images/verbos/speak.png",    "/spiːk/",     "spík",      "speaks",     "spoke",      "speaking"),
        new Verb(7,  "Listen",   "Escuchar",         "images/verbos/listen.png",   "/ˈlɪs.ən/",   "lís·en",    "listens",    "listened",   "listening"),
        new Verb(8,  "Dance",    "Bailar",           "images/verbos/dance.png",    "/dæns/",      "dáns",      "dances",     "danced",     "dancing"),
        new Verb(9,  "Talk",     "Hablar/Conversar", "images/verbos/talk.png",     "/tɔːk/",      "tók",       "talks",      "talked",     "talking"),
        new Verb(10, "Rub",      "Frotar",           "images/verbos/rub.png",      "/rʌb/",       "rúb",       "rubs",       "rubbed",     "rubbing"),
        new Verb(11, "Sleep",    "Dormir",           "images/verbos/sleep.png",    "/sliːp/",     "slíp",      "sleeps",     "slept",      "sleeping"),
        new Verb(12, "Write",    "Escribir",         "images/verbos/write.png",    "/raɪt/",      "ráit",      "writes",     "wrote",      "writing"),
        new Verb(13, "Sing",     "Cantar",           "images/verbos/sing.png",     "/sɪŋ/",       "síng",      "sings",      "sang",       "singing"),
        new Verb(14, "Read",     "Leer",             "images/verbos/read.png",     "/riːd/",      "ríd",       "reads",      "read",       "reading"),
        new Verb(15, "Eat",      "Comer",            "images/verbos/eat.png",      "/iːt/",       "ít",        "eats",       "ate",        "eating"),
        new Verb(16, "Play",     "Jugar",            "images/verbos/play.png",     "/pleɪ/",      "pléi",      "plays",      "played",     "playing")
    );

    /** Usuarios por defecto — usados solo para inicializar usuarios.txt en la primera ejecución. */
    static final List<User> DEFAULT_USERS = List.of(
        new User("andrea.rojas",  "estudiante2026", User.Role.STUDENT,  "Andrea Rojas",         "Group 3 - Technical English"),
        new User("carla.mendoza", "profesor2026",   User.Role.TEACHER,  "Carla Mendoza Vargas", "Group 3 - Technical English"),
        new User("admin.umss",    "admin2026",       User.Role.ADMIN,    "Administrador UMSS",   "")
    );

    /**
     * Llamado una vez desde Main.start().
     * Carga USERS y VERBS desde archivos TXT, luego construye SENTENCES dinámicamente
     * usando los campos de conjugación (presentForm/pastForm/futureForm) almacenados en verbos.txt.
     */
    // Inicializa los datos de la aplicación
    public static void init() {
        USERS = DataManager.loadUsers();
        VERBS = DataManager.loadVerbs();
        rebuildSentences();
    }

    /** Complement phrase per verb index (matches order in VERBS / verbos.txt). */
    private static final String[] SENTENCE_COMPLEMENTS = {
        "English every day",
        "math at school",
        "new words every day",
        "every question",
        "a question",
        "English fluently",
        "to music",
        "at the party",
        "to his teacher",
        "her hands",
        "early every night",
        "a letter",
        "very well",
        "a book every week",
        "breakfast every morning",
        "soccer on Saturdays",
    };

    /** Full complement tokens for a verb — same phrases used when building all tenses. */
    public static List<String> complementTokensForVerb(int verbId) {
        int index = verbIndexById(verbId);
        String phrase = (index >= 0 && index < SENTENCE_COMPLEMENTS.length)
            ? SENTENCE_COMPLEMENTS[index]
            : "every day";
        return Arrays.asList(phrase.split(" "));
    }

    private static int verbIndexById(int verbId) {
        for (int i = 0; i < VERBS.size(); i++) {
            if (VERBS.get(i).getId() == verbId) return i;
        }
        return -1;
    }

    /**
     * Reconstruye la lista SENTENCES desde la lista VERBS actual.
     * Cada verbo produce 3 oraciones (Present / Past / Future).
     * Las palabras "trampa" (extra) vienen de presentForm, pastForm, futureForm en verbos.txt.
     * Llamar este método después de añadir/remover un verbo.
     */
    // Reconstruye la lista de oraciones desde los verbos actuales
    public static void rebuildSentences() {
        SENTENCES = new ArrayList<>();
        int sentId = 1;
        String[][] templates = {
            // { subject, object/complement }
            {"I",    SENTENCE_COMPLEMENTS[0]},
            {"She",  SENTENCE_COMPLEMENTS[1]},
            {"They", SENTENCE_COMPLEMENTS[2]},
            {"He",   SENTENCE_COMPLEMENTS[3]},
            {"We",   SENTENCE_COMPLEMENTS[4]},
            {"She",  SENTENCE_COMPLEMENTS[5]},
            {"I",    SENTENCE_COMPLEMENTS[6]},
            {"They", SENTENCE_COMPLEMENTS[7]},
            {"He",   SENTENCE_COMPLEMENTS[8]},
            {"She",  SENTENCE_COMPLEMENTS[9]},
            {"I",    SENTENCE_COMPLEMENTS[10]},
            {"We",   SENTENCE_COMPLEMENTS[11]},
            {"He",   SENTENCE_COMPLEMENTS[12]},
            {"They", SENTENCE_COMPLEMENTS[13]},
            {"She",  SENTENCE_COMPLEMENTS[14]},
            {"I",    SENTENCE_COMPLEMENTS[15]},
        };

        for (int i = 0; i < VERBS.size(); i++) {
            Verb v    = VERBS.get(i);
            String en = v.getEnglish();  // base form (e.g. "Practice")
            String pr = v.getPresentForm();  // from verbos.txt col 7
            String pa = v.getPastForm();     // from verbos.txt col 8
            String fu = v.getFutureForm();   // from verbos.txt col 9

            // Use template if available, otherwise generic fallback
            String subj = i < templates.length ? templates[i][0] : "I";
            String obj  = i < SENTENCE_COMPLEMENTS.length ? SENTENCE_COMPLEMENTS[i] : "every day";
            List<String> objToks = Arrays.asList(obj.split(" "));

            // ── PRESENT ───────────────────────────────────────────────
            // Subject + presentForm + object
            List<String> presWords = new ArrayList<>();
            presWords.add(subj);
            presWords.add(pr);
            presWords.addAll(objToks);
            List<String> presExtra = Arrays.asList(pa, "will", fu);
            SENTENCES.add(new Sentence(sentId++, v.getId(), PRESENT, presWords, presExtra));

            // ── PAST ──────────────────────────────────────────────────
            // Subject + pastForm + full object complement
            List<String> pastWords = new ArrayList<>();
            pastWords.add(subj);
            pastWords.add(pa);
            pastWords.addAll(objToks);
            List<String> pastExtra = Arrays.asList(pr, "will", fu);
            SENTENCES.add(new Sentence(sentId++, v.getId(), PAST, pastWords, pastExtra));

            // ── FUTURE ────────────────────────────────────────────────
            // Subject + will + base + object
            List<String> futWords = new ArrayList<>();
            futWords.add(subj);
            futWords.add("will");
            futWords.add(en.toLowerCase());
            futWords.addAll(objToks);
            List<String> futExtra = Arrays.asList(pa, pr, fu);
            SENTENCES.add(new Sentence(sentId++, v.getId(), FUTURE, futWords, futExtra));
        }
    }

    /** Retorna el siguiente ID libre para un verbo. */
    public static int nextVerbId() {
        return VERBS.stream().mapToInt(Verb::getId).max().orElse(0) + 1;
    }

    // ── Diccionario IPA ────────────────────────────────────────────────
    // Diccionario de pronunciación IPA
    public static final Map<String, String> IPA_DICT = new HashMap<>();
    static {
        IPA_DICT.put("i",         "/aɪ/");
        IPA_DICT.put("she",       "/ʃiː/");
        IPA_DICT.put("he",        "/hiː/");
        IPA_DICT.put("they",      "/ðeɪ/");
        IPA_DICT.put("we",        "/wiː/");
        IPA_DICT.put("will",      "/wɪl/");
        IPA_DICT.put("a",         "/ə/");
        IPA_DICT.put("the",       "/ðə/");
        IPA_DICT.put("to",        "/tə/");
        IPA_DICT.put("at",        "/æt/");
        IPA_DICT.put("her",       "/hɜːr/");
        IPA_DICT.put("his",       "/hɪz/");
        IPA_DICT.put("on",        "/ɒn/");
        IPA_DICT.put("every",     "/ˈev.ri/");
        IPA_DICT.put("day",       "/deɪ/");
        IPA_DICT.put("yesterday", "/ˈjes.tər.deɪ/");
        IPA_DICT.put("tomorrow",  "/təˈmɒr.oʊ/");
        IPA_DICT.put("tonight",   "/təˈnaɪt/");
        IPA_DICT.put("early",     "/ˈɜːr.li/");
        IPA_DICT.put("last",      "/læst/");
        IPA_DICT.put("night",     "/naɪt/");
        IPA_DICT.put("week",      "/wiːk/");
        IPA_DICT.put("morning",   "/ˈmɔːr.nɪŋ/");
        IPA_DICT.put("english",   "/ˈɪŋ.ɡlɪʃ/");
        IPA_DICT.put("math",      "/mæθ/");
        IPA_DICT.put("school",    "/skuːl/");
        IPA_DICT.put("question",  "/ˈkwes.tʃən/");
        IPA_DICT.put("music",     "/ˈmjuː.zɪk/");
        IPA_DICT.put("words",     "/wɜːrdz/");
        IPA_DICT.put("new",       "/njuː/");
        IPA_DICT.put("letter",    "/ˈlet.ər/");
        IPA_DICT.put("book",      "/bʊk/");
        IPA_DICT.put("breakfast", "/ˈbrek.fəst/");
        IPA_DICT.put("soccer",    "/ˈsɒk.ər/");
        IPA_DICT.put("saturdays", "/ˈsæt.ər.deɪz/");
        IPA_DICT.put("party",     "/ˈpɑːr.ti/");
        IPA_DICT.put("teacher",   "/ˈtiː.tʃər/");
        IPA_DICT.put("hands",     "/hændz/");
        IPA_DICT.put("very",      "/ˈver.i/");
        IPA_DICT.put("well",      "/wel/");
        IPA_DICT.put("fluently",  "/ˈfluː.ənt.li/");
        // present forms
        IPA_DICT.put("practice",  "/ˈpræk.tɪs/");
        IPA_DICT.put("practices", "/ˈpræk.tɪ.sɪz/");
        IPA_DICT.put("teach",     "/tiːtʃ/");
        IPA_DICT.put("teaches",   "/ˈtiː.tʃɪz/");
        IPA_DICT.put("learn",     "/lɜːrn/");
        IPA_DICT.put("learns",    "/lɜːrnz/");
        IPA_DICT.put("answer",    "/ˈæn.sər/");
        IPA_DICT.put("answers",   "/ˈæn.sərz/");
        IPA_DICT.put("ask",       "/æsk/");
        IPA_DICT.put("asks",      "/æsks/");
        IPA_DICT.put("speak",     "/spiːk/");
        IPA_DICT.put("speaks",    "/spiːks/");
        IPA_DICT.put("listen",    "/ˈlɪs.ən/");
        IPA_DICT.put("listens",   "/ˈlɪs.ənz/");
        IPA_DICT.put("dance",     "/dæns/");
        IPA_DICT.put("dances",    "/ˈdæn.sɪz/");
        IPA_DICT.put("talk",      "/tɔːk/");
        IPA_DICT.put("talks",     "/tɔːks/");
        IPA_DICT.put("rub",       "/rʌb/");
        IPA_DICT.put("rubs",      "/rʌbz/");
        IPA_DICT.put("sleep",     "/sliːp/");
        IPA_DICT.put("sleeps",    "/sliːps/");
        IPA_DICT.put("write",     "/raɪt/");
        IPA_DICT.put("writes",    "/raɪts/");
        IPA_DICT.put("sing",      "/sɪŋ/");
        IPA_DICT.put("sings",     "/sɪŋz/");
        IPA_DICT.put("read",      "/riːd/");
        IPA_DICT.put("reads",     "/riːdz/");
        IPA_DICT.put("eat",       "/iːt/");
        IPA_DICT.put("eats",      "/iːts/");
        IPA_DICT.put("play",      "/pleɪ/");
        IPA_DICT.put("plays",     "/pleɪz/");
        // past forms
        IPA_DICT.put("practiced", "/ˈpræk.tɪst/");
        IPA_DICT.put("taught",    "/tɔːt/");
        IPA_DICT.put("learned",   "/lɜːrnd/");
        IPA_DICT.put("answered",  "/ˈæn.sərd/");
        IPA_DICT.put("asked",     "/æskt/");
        IPA_DICT.put("spoke",     "/spoʊk/");
        IPA_DICT.put("listened",  "/ˈlɪs.ənd/");
        IPA_DICT.put("danced",    "/dænst/");
        IPA_DICT.put("talked",    "/tɔːkt/");
        IPA_DICT.put("rubbed",    "/rʌbd/");
        IPA_DICT.put("slept",     "/slept/");
        IPA_DICT.put("wrote",     "/roʊt/");
        IPA_DICT.put("sang",      "/sæŋ/");
        IPA_DICT.put("ate",       "/eɪt/");
        IPA_DICT.put("played",    "/pleɪd/");
        // gerunds
        IPA_DICT.put("practicing",  "/ˈpræk.tɪ.sɪŋ/");
        IPA_DICT.put("teaching",    "/ˈtiː.tʃɪŋ/");
        IPA_DICT.put("learning",    "/ˈlɜːr.nɪŋ/");
        IPA_DICT.put("answering",   "/ˈæn.sər.ɪŋ/");
        IPA_DICT.put("asking",      "/ˈæs.kɪŋ/");
        IPA_DICT.put("speaking",    "/ˈspiː.kɪŋ/");
        IPA_DICT.put("listening",   "/ˈlɪs.ən.ɪŋ/");
        IPA_DICT.put("dancing",     "/ˈdæn.sɪŋ/");
        IPA_DICT.put("talking",     "/ˈtɔː.kɪŋ/");
        IPA_DICT.put("rubbing",     "/ˈrʌb.ɪŋ/");
        IPA_DICT.put("sleeping",    "/ˈsliː.pɪŋ/");
        IPA_DICT.put("writing",     "/ˈraɪ.tɪŋ/");
        IPA_DICT.put("singing",     "/ˈsɪŋ.ɪŋ/");
        IPA_DICT.put("reading",     "/ˈriː.dɪŋ/");
        IPA_DICT.put("eating",      "/ˈiː.tɪŋ/");
        IPA_DICT.put("playing",     "/ˈpleɪ.ɪŋ/");
    }

    // Genera la representación IPA de una oración
    public static String sentenceIpa(List<String> words) {
        StringJoiner sj = new StringJoiner("  ");
        for (String w : words) {
            String ipa = IPA_DICT.getOrDefault(w.toLowerCase(), "/" + w.toLowerCase() + "/");
            sj.add(ipa);
        }
        return sj.toString();
    }
}
