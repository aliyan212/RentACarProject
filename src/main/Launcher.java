package main;

/**
 * Main application launcher entrypoint.
 * This class serves as the non-Application entrypoint required to bootstrap
 * JavaFX when packaged as a standalone executable Fat JAR.
 */
public class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}
