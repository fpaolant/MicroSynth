package it.univaq.microsynth.generator.util;

import java.util.Arrays;
import java.util.stream.Collectors;

public class GeneratorUtil {

    public static String getDockerTemplate(String type) {
        return switch (type.toLowerCase()) {
            case "python", "python-flask" -> "docker/Dockerfile-python.template";
            case "java", "spring" -> "docker/Dockerfile-spring.template";
            case "javascript", "node-express" -> "docker/Dockerfile-node.template";
            default -> throw new IllegalArgumentException("Unsupported type: " + type);
        };
    }


    public static String mapGenerator(String type) throws IllegalArgumentException {
        return switch (type.toLowerCase()) {
            case "python" -> "python-flask";
            case "java" -> "spring";
            case "javascript" -> "nodejs-express";
            default -> throw new IllegalArgumentException("Unsupported type: " + type);
        };
    }

    public static String sanitize(String input) {
        // Rimuove slash iniziali
        input = input.replaceAll("^/+", "");
        // Sostituisce tutti i caratteri non alfanumerici con underscore
        input = input.replaceAll("[^A-Za-z0-9]", "_");
        // Aggiunge "get" se il metodo era get
        return input;
    }

    public static String sanitizeDockerServiceName(String s) {
        // solo minuscole, sostituisce tutto ciò che non è alfanumerico con '-'
        return s.toLowerCase().replaceAll("[^a-z0-9]+", "-");
    }

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

    public static String stringEscape(String input) {
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
