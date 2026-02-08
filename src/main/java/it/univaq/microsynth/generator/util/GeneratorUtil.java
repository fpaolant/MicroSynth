package it.univaq.microsynth.generator.util;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Utility class for the code generator, providing methods to get template paths, sanitize strings, and generate operationIds.
 */
public class GeneratorUtil {

    /**
     * Returns the path to the Dockerfile template based on the given type.
     * @param type the type of the Dockerfile template (e.g., "python", "java", "javascript")
     * @return the path to the Dockerfile template
     */
    public static String getDockerTemplate(String type) {
        return switch (type.toLowerCase()) {
            case "python", "python-flask" -> "docker/Dockerfile-python.template";
            case "java", "spring" -> "docker/Dockerfile-spring.template";
            case "javascript", "node-express" -> "docker/Dockerfile-node.template";
            default -> throw new IllegalArgumentException("Unsupported type: " + type);
        };
    }

    /**
     * Returns the path to the .dockerignore template based on the given type.
     * @param type the type of the .dockerignore template (e.g., "python", "java", "javascript")
     * @return the path to the .dockerignore template
     */
    public static String mapGenerator(String type) throws IllegalArgumentException {
        return switch (type.toLowerCase()) {
            case "python" -> "python-flask";
            case "java" -> "spring";
            case "javascript" -> "nodejs-express";
            default -> throw new IllegalArgumentException("Unsupported type: " + type);
        };
    }

    /**
     * Sanitizes the input string by removing leading slashes and replacing non-alphanumeric characters with underscores.
     * @param input the string to sanitize
     * @return the sanitized string
     */
    public static String sanitize(String input) {
        // Rimuove slash iniziali
        input = input.replaceAll("^/+", "");
        // Sostituisce tutti i caratteri non alfanumerici con underscore
        input = input.replaceAll("[^A-Za-z0-9]", "_");
        // Aggiunge "get" se il metodo era get
        return input;
    }

    /**
     * Sanitizes the input string to be used as a Docker service name by converting it to lowercase and replacing non-alphanumeric characters with hyphens.
     * @param s the string to sanitize
     * @return the sanitized Docker service name
     */
    public static String sanitizeDockerServiceName(String s) {
        // solo minuscole, sostituisce tutto ciò che non è alfanumerico con '-'
        return s.toLowerCase().replaceAll("[^a-z0-9]+", "-");
    }

    /**
     * Converts the given path and method to a valid operationId by removing leading slashes, replacing non-alphanumeric characters with spaces, and converting to camelCase.
     * @param path the API path (e.g., "/path-3")
     * @param method the HTTP method (e.g., "post")
     * @return the generated operationId (e.g., "path3Post")
     */
    public static String toOperationId(String path, String method) {
        // /path-3 → path3
        String cleanPath = path
                .replaceAll("^/+", "")
                .replaceAll("[^A-Za-z0-9]", " ");

        String camelPath = Arrays.stream(cleanPath.split("\\s+"))
                .filter(s -> !s.isBlank())
                .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1))
                .collect(Collectors.joining());

        // post → Post
        String camelMethod =
                Character.toUpperCase(method.toLowerCase().charAt(0)) +
                        method.toLowerCase().substring(1);

        // path3Post (lowerCamelCase)
        return Character.toLowerCase(camelPath.charAt(0)) +
                camelPath.substring(1) +
                camelMethod;
    }

    /**
     * Escapes special characters in the input string for safe inclusion in generated code.
     * @param input the string to escape
     * @return the escaped string
     */
    public static String stringEscape(String input) {
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
