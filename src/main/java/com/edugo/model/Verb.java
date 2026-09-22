package com.edugo.model;

/**
 * Verb model.
 *
 * Fields stored in data/verbos.txt:
 *   id|english|spanish|imagePath|ipa|pronunciation|presentForm|pastForm|futureForm
 *
 * presentForm  → 3rd person singular present (e.g. "practices")
 * pastForm     → simple past (e.g. "practiced")
 * futureForm   → gerund (e.g. "practicing")
 *
 * These three fields are used as "trap" words in SentenceBuilder and are fully
 * editable in data/verbos.txt or via the Teacher/Admin add-verb form.
 *
 * Legacy files with only 6 columns get default values derived from the English base.
 */
// Clase que representa un verbo en el sistema EduGo
public class Verb {
    // Identificador único del verbo
    private final int id;
    // Verbo en inglés
    private final String english;
    // Traducción al español
    private final String spanish;
    // Ruta de la imagen asociada
    private final String imagePath;
    // Pronunciación en alfabeto fonético internacional
    private final String ipa;
    // Pronunciación simplificada
    private final String pronunciation;
    // Tercera persona del singular en presente
    private final String presentForm;
    // Pasado simple
    private final String pastForm;
    // Gerundio
    private final String futureForm;

    // Constructor completo con conjugaciones
    public Verb(int id, String english, String spanish, String imagePath,
                String ipa, String pronunciation,
                String presentForm, String pastForm, String futureForm) {
        this.id            = id;
        this.english       = english;
        this.spanish       = spanish;
        this.imagePath     = imagePath;
        this.ipa           = ipa;
        this.pronunciation = pronunciation;
        this.presentForm   = presentForm;
        this.pastForm      = pastForm;
        this.futureForm    = futureForm;
    }

    /** Constructor legado de 6 campos — genera conjugaciones por defecto. */
    public Verb(int id, String english, String spanish, String imagePath,
                String ipa, String pronunciation) {
        this(id, english, spanish, imagePath, ipa, pronunciation,
             english.toLowerCase() + "s",
             english.toLowerCase() + "ed",
             english.toLowerCase() + "ing");
    }

    // Retorna el ID del verbo
    public int    getId()            { return id; }
    // Retorna el verbo en inglés
    public String getEnglish()       { return english; }
    // Retorna la traducción al español
    public String getSpanish()       { return spanish; }
    // Retorna la ruta de la imagen
    public String getImagePath()     { return imagePath; }
    // Retorna la pronunciación IPA
    public String getIpa()           { return ipa; }
    // Retorna la pronunciación simplificada
    public String getPronunciation() { return pronunciation; }
    // Retorna la tercera persona del presente
    public String getPresentForm()   { return presentForm; }
    // Retorna el pasado simple
    public String getPastForm()      { return pastForm; }
    // Retorna el gerundio
    public String getFutureForm()    { return futureForm; }
}
