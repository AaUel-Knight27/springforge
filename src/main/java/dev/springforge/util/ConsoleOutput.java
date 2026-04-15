package dev.springforge.util;

/**
 * Pretty console output with colors and icons for CLI feedback.
 */
public class ConsoleOutput {

    // ANSI color codes
    private static final String RESET  = "\u001B[0m";
    private static final String RED    = "\u001B[31m";
    private static final String GREEN  = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE   = "\u001B[34m";
    private static final String PURPLE = "\u001B[35m";
    private static final String CYAN   = "\u001B[36m";
    private static final String BOLD   = "\u001B[1m";
    private static final String DIM    = "\u001B[2m";

    public static void banner() {
        System.out.println(BOLD + CYAN);
        System.out.println("   ____            _             _____");
        System.out.println("  / ___| _ __  _ __(_)_ __   __ _|  ___|___  _ __ __ _  ___");
        System.out.println("  \\___ \\| '_ \\| '__| | '_ \\ / _` | |_ / _ \\| '__/ _` |/ _ \\");
        System.out.println("   ___) | |_) | |  | | | | | (_| |  _| (_) | | | (_| |  __/");
        System.out.println("  |____/| .__/|_|  |_|_| |_|\\__, |_|  \\___/|_|  \\__, |\\___|");
        System.out.println("        |_|                  |___/               |___/");
        System.out.println(RESET);
        System.out.println(DIM + "  ⚡ Full-stack microservice accelerator" + RESET);
        System.out.println();
    }

    public static void success(String message) {
        System.out.println(GREEN + "  ✅ " + message + RESET);
    }

    public static void created(String filePath) {
        System.out.println(GREEN + "  📄 Created: " + RESET + DIM + filePath + RESET);
    }

    public static void updated(String filePath) {
        System.out.println(BLUE + "  🔄 Updated: " + RESET + DIM + filePath + RESET);
    }

    public static void info(String message) {
        System.out.println(BLUE + "  ℹ️  " + message + RESET);
    }

    public static void warn(String message) {
        System.out.println(YELLOW + "  ⚠️  " + message + RESET);
    }

    public static void error(String message) {
        System.out.println(RED + "  ❌ " + message + RESET);
    }

    public static void step(String message) {
        System.out.println(PURPLE + "  → " + message + RESET);
    }

    public static void header(String message) {
        System.out.println();
        System.out.println(BOLD + CYAN + "  🚀 " + message + RESET);
        System.out.println(DIM + "  " + "─".repeat(50) + RESET);
    }

    public static void done(String message) {
        System.out.println();
        System.out.println(BOLD + GREEN + "  🎉 " + message + RESET);
        System.out.println();
    }

    public static void command(String cmd) {
        System.out.println(DIM + "  $ " + RESET + YELLOW + cmd + RESET);
    }

    public static void newline() {
        System.out.println();
    }
}
