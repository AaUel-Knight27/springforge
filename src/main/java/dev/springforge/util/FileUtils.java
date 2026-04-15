package dev.springforge.util;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;

public class FileUtils {

    /**
     * Write content to a file, creating parent directories if needed.
     */
    public static void writeFile(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }

    /**
     * Read entire file to string.
     */
    public static String readFile(Path path) throws IOException {
        return Files.readString(path);
    }

    /**
     * Check if a file exists.
     */
    public static boolean exists(Path path) {
        return Files.exists(path);
    }

    /**
     * Create directories recursively.
     */
    public static void mkdirs(Path path) throws IOException {
        Files.createDirectories(path);
    }

    /**
     * Find first file matching a glob pattern in directory tree.
     */
    public static Path findFile(Path root, String glob) throws IOException {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
        try (Stream<Path> walk = Files.walk(root)) {
            return walk.filter(p -> matcher.matches(p.getFileName()))
                       .findFirst()
                       .orElse(null);
        }
    }

    /**
     * Copy directory tree recursively.
     */
    public static void copyDirectory(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Files.createDirectories(target.resolve(source.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, target.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Insert content after a specific line in a file.
     */
    public static void insertAfterLine(Path file, String marker, String insertion) throws IOException {
        String content = readFile(file);
        int idx = content.indexOf(marker);
        if (idx >= 0) {
            int lineEnd = content.indexOf('\n', idx);
            if (lineEnd < 0) lineEnd = content.length();
            String updated = content.substring(0, lineEnd + 1) + insertion + "\n" + content.substring(lineEnd + 1);
            writeFile(file, updated);
        }
    }

    /**
     * Insert content before a specific line in a file.
     */
    public static void insertBeforeLine(Path file, String marker, String insertion) throws IOException {
        String content = readFile(file);
        int idx = content.indexOf(marker);
        if (idx >= 0) {
            String updated = content.substring(0, idx) + insertion + "\n" + content.substring(idx);
            writeFile(file, updated);
        }
    }

    /**
     * Replace a marker in a file with new content.
     */
    public static void replaceInFile(Path file, String marker, String replacement) throws IOException {
        String content = readFile(file);
        if (content.contains(marker)) {
            writeFile(file, content.replace(marker, replacement));
        }
    }
}
