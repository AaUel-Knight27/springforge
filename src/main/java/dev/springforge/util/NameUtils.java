package dev.springforge.util;

/**
 * Naming convention utilities for converting between formats:
 * camelCase, PascalCase, snake_case, kebab-case, etc.
 */
public class NameUtils {

    /**
     * Convert to PascalCase: "blog_post" -> "BlogPost", "blog" -> "Blog"
     */
    public static String toPascalCase(String input) {
        if (input == null || input.isEmpty()) return input;
        StringBuilder sb = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : input.toCharArray()) {
            if (c == '_' || c == '-' || c == ' ') {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                sb.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                sb.append(c);
            }
        }
        // Ensure first char is uppercase
        if (!sb.isEmpty()) {
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        }
        return sb.toString();
    }

    /**
     * Convert to camelCase: "BlogPost" -> "blogPost", "blog" -> "blog"
     */
    public static String toCamelCase(String input) {
        String pascal = toPascalCase(input);
        if (pascal == null || pascal.isEmpty()) return pascal;
        return Character.toLowerCase(pascal.charAt(0)) + pascal.substring(1);
    }

    /**
     * Convert to snake_case: "BlogPost" -> "blog_post", "blog" -> "blog"
     */
    public static String toSnakeCase(String input) {
        if (input == null || input.isEmpty()) return input;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) sb.append('_');
                sb.append(Character.toLowerCase(c));
            } else if (c == '-' || c == ' ') {
                sb.append('_');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Convert to kebab-case: "BlogPost" -> "blog-post", "blog" -> "blog"
     */
    public static String toKebabCase(String input) {
        return toSnakeCase(input).replace('_', '-');
    }

    /**
     * Convert to URL path segment: "BlogPost" -> "blog-post"
     */
    public static String toUrlPath(String input) {
        return toKebabCase(input);
    }

    /**
     * Convert to plural form (simple English rules).
     */
    public static String toPlural(String input) {
        if (input == null || input.isEmpty()) return input;
        String lower = input.toLowerCase();
        if (lower.endsWith("s") || lower.endsWith("x") || lower.endsWith("z")
            || lower.endsWith("ch") || lower.endsWith("sh")) {
            return input + "es";
        } else if (lower.endsWith("y") && !isVowel(lower.charAt(lower.length() - 2))) {
            return input.substring(0, input.length() - 1) + "ies";
        } else {
            return input + "s";
        }
    }

    /**
     * Convert to table name: "BlogPost" -> "blog_posts" (snake_case + plural)
     */
    public static String toTableName(String input) {
        return toPlural(toSnakeCase(input));
    }

    /**
     * Get package path from package name: "com.example.blog" -> "com/example/blog"
     */
    public static String toPackagePath(String packageName) {
        return packageName.replace('.', '/');
    }

    private static boolean isVowel(char c) {
        return "aeiou".indexOf(Character.toLowerCase(c)) >= 0;
    }
}
