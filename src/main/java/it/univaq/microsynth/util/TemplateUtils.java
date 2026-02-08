package it.univaq.microsynth.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import org.springframework.core.io.ClassPathResource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

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

}
