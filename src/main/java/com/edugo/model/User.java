package com.edugo.model;

// Clase que representa un usuario del sistema EduGo
public class User {
    // Enumeración de roles posibles en el sistema
    public enum Role { STUDENT, TEACHER, ADMIN }

    // Nombre de usuario único para iniciar sesión
    private final String username;
    // Contraseña del usuario
    private final String password;
    // Rol del usuario (estudiante, profesor o administrador)
    private final Role role;
    // Nombre completo del usuario
    private final String fullName;
    // Curso o grupo al que pertenece el usuario
    private final String course;

    // Constructor de la clase User
    public User(String username, String password, Role role, String fullName, String course) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.course = course;
    }

    // Retorna el nombre de usuario
    public String getUsername() { return username; }
    // Retorna la contraseña
    public String getPassword() { return password; }
    // Retorna el rol del usuario
    public Role getRole()       { return role; }
    // Retorna el nombre completo
    public String getFullName() { return fullName; }
    // Retorna el curso del usuario
    public String getCourse()   { return course; }
}
