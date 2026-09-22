package com.edugo.model;

import java.util.List;

// Clase que representa una oración para el constructor de oraciones
public class Sentence {
    // Identificador único de la oración
    private final int id;
    // ID del verbo asociado
    private final int verbId;
    // Tiempo verbal de la oración
    private final String tense;
    // Lista de palabras correctas en orden
    private final List<String> correct;
    // Lista de palabras extra (trampas)
    private final List<String> extra;

    // Constructor de la clase Sentence
    public Sentence(int id, int verbId, String tense, List<String> correct, List<String> extra) {
        this.id = id;
        this.verbId = verbId;
        this.tense = tense;
        this.correct = correct;
        this.extra = extra;
    }

    // Retorna el ID de la oración
    public int getId()               { return id; }
    // Retorna el ID del verbo asociado
    public int getVerbId()           { return verbId; }
    // Retorna el tiempo verbal
    public String getTense()         { return tense; }
    // Retorna la lista de palabras correctas
    public List<String> getCorrect() { return correct; }
    // Retorna la lista de palabras extra
    public List<String> getExtra()   { return extra; }
}
