package it.univaq.microsynth.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.Map;

import java.io.InputStream;
import java.io.UncheckedIOException;

import java.net.URL;

import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.util.Enumeration;

import java.util.jar.JarFile;
import java.util.jar.JarEntry;

public class TemplateUtils {

    /**
     * Renders a Handlebars template located in the classpath under "templates/" with the provided values.
     *
     * @param templatePath The path to the template file relative to the "templates/" directory.
     * @param values       A map of values to be used for rendering the template.
     * @return The rendered template as a String.
     * @throws IOException If an error occurs while reading the template file or during rendering.
     */
    public static String renderTemplate(String templatePath, Map<String, Object> values) throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/" + templatePath);
        String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        Handlebars handlebars = new Handlebars();
        ObjectMapper mapper = new ObjectMapper();

        handlebars.registerHelper("json", (context, options) ->
                mapper.writeValueAsString(context)
        );

        Template template = handlebars.compileInline(content);
        return template.apply(values);
    }

    /**
     * Renders a Handlebars template and writes the rendered content to a specified output path.
     *
     * @param templateName The name of the template file located in the classpath under "templates/".
     * @param outputPath   The path where the rendered template should be written.
     * @param values       A map of values to be used for rendering the template.
     * @throws IOException If an error occurs while reading the template file, during rendering, or while writing the output file.
     */
    public static void writeRenderedTemplate(String templateName, Path outputPath, Map<String, Object> values) throws IOException {
        String rendered = renderTemplate(templateName, values);
        Files.writeString(outputPath, rendered, StandardCharsets.UTF_8);
        Files.writeString(outputPath, rendered, StandardCharsets.UTF_8);
    }

    public static Path extractTemplates(String generatorName) {
        try {
            Path tempDir = Files.createTempDirectory("openapi-templates-");
            Path generatorDir = tempDir.resolve(generatorName);
            Files.createDirectories(generatorDir);

            String logicalBase = "templates/openapi/custom/" + generatorName + "/";
            String bootBase = "BOOT-INF/classes/" + logicalBase;


            CodeSource codeSource = TemplateUtils.class
                    .getProtectionDomain()
                    .getCodeSource();

            if (codeSource == null) {
                throw new IllegalStateException("Cannot determine code source location");
            }

            URL location = codeSource.getLocation();

            // ===== JAR =====
            if (location.getProtocol().equals("file") && location.getPath().endsWith(".jar")) {

                try (JarFile jarFile = new JarFile(new File(location.toURI()))) {

                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();

                        if (!name.startsWith(bootBase)) {
                            continue;
                        }

                        String relative = name.substring(bootBase.length());
                        if (relative.isEmpty()) continue;

                        Path dest = generatorDir.resolve(relative);

                        if (entry.isDirectory()) {
                            Files.createDirectories(dest);
                        } else {
                            Files.createDirectories(dest.getParent());
                            try (InputStream in = jarFile.getInputStream(entry)) {
                                Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
                            }
                        }
                    }
                }

            } else {
                // ===== IDE / TEST =====
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                URL baseUrl = cl.getResource(logicalBase);

                if (baseUrl == null) {
                    throw new IllegalStateException("Template path not found: " + logicalBase);
                }

                Path sourceDir = Paths.get(baseUrl.toURI());
                Files.walk(sourceDir).forEach(src -> {
                    try {
                        Path dest = generatorDir.resolve(sourceDir.relativize(src).toString());
                        if (Files.isDirectory(src)) {
                            Files.createDirectories(dest);
                        } else {
                            Files.createDirectories(dest.getParent());
                            Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            }

            return tempDir;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to extract OpenAPI templates for generator " + generatorName, e
            );
        }
    }


}
