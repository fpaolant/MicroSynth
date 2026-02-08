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

    public static String renderTemplate(String templatePath, Map<String, Object> values) throws IOException {
        var resource = new ClassPathResource("templates/" + templatePath);
        String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        Handlebars handlebars = new Handlebars();

        ObjectMapper mapper = new ObjectMapper();

        handlebars.registerHelper("json", (context, options) ->
                mapper.writeValueAsString(context)
        );

        Template template = handlebars.compileInline(content);
        return template.apply(values);
    }

    public static void writeRenderedTemplate(String templateName, Path outputPath, Map<String, Object> values) throws IOException {
        String rendered = renderTemplate(templateName, values);
        Files.writeString(outputPath, rendered, StandardCharsets.UTF_8);
        Files.writeString(outputPath, rendered, StandardCharsets.UTF_8);
    }

}
