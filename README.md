# EduGo — JavaFX

Aplicación de aprendizaje de inglés con interfaz moderna en JavaFX.

## Requisitos
- JDK 21 (o 17+)
- Maven 3.8+
- IntelliJ IDEA (recomendado) o cualquier IDE con soporte Maven

## Estructura del proyecto

```
EduGo/
├── pom.xml
└── src/main/java/com/edugo/
    ├── Main.java                   ← Punto de entrada
    ├── controller/
    │   └── ScreenManager.java      ← Enrutador de pantallas
    ├── data/
    │   ├── AppData.java            ← 16 verbos, 48 oraciones, usuarios
    │   └── AppState.java           ← Estado de sesión (progreso, puntos)
    ├── model/
    │   ├── Verb.java
    │   ├── Sentence.java
    │   └── User.java
    ├── util/
    │   ├── Styles.java             ← Constantes CSS JavaFX
    │   ├── TTS.java                ← Text-to-speech multiplataforma
    │   └── UIHelper.java           ← Widgets reutilizables
    └── view/
        ├── LoginScreen.java
        ├── StudentDashboard.java
        ├── FlashcardsScreen.java   ← Imagen PNG + verificar + Listen
        ├── SentenceBuilderScreen.java ← 3 tiempos, 16 oraciones c/u
        ├── ProgressScreen.java
        ├── TeacherDashboard.java
        └── AdminDashboard.java
```

## Imágenes de verbos

Coloca los archivos PNG en:
```
src/main/resources/images/verbos/
    practice.png
    teach.png
    learn.png
    answer.png
    ask.png
    speak.png
    listen.png
    dance.png
    talk.png
    rub.png
    sleep.png
    write.png
    sing.png
    read.png
    eat.png
    play.png
```
Si una imagen no existe el código muestra un placeholder vacío sin errores.

## Ejecutar desde IntelliJ

1. Abrir la carpeta `EduGo/` como proyecto Maven.
2. Esperar a que IntelliJ descargue las dependencias.
3. Click derecho en `Main.java` → **Run 'Main.main()'**.

## Ejecutar desde consola

```bash
cd EduGo
mvn javafx:run
```

## Cuentas de prueba

| Usuario          | Contraseña      | Rol       |
|------------------|-----------------|-----------|
| andrea.rojas     | estudiante2026  | Student   |
| carla.mendoza    | profesor2026    | Teacher   |
| admin.umss       | admin2026       | Admin     |

## TTS (pronunciación)

- **macOS**: usa el comando `say` (nativo, sin instalar nada)
- **Linux**: requiere `espeak` instalado (`sudo apt install espeak`)
- **Windows**: usa PowerShell + `System.Speech.Synthesis` (nativo)

Si el sistema no soporta TTS el botón Listen muestra el IPA igual, simplemente sin audio.
