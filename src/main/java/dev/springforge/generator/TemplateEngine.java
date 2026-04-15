package dev.springforge.generator;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.Map;

import dev.springforge.util.FileUtils;

/**
 * Mustache template engine wrapper.
 * Loads templates from classpath resources/templates/ and renders them with context data.
 */
public class TemplateEngine {

    private final MustacheFactory mf;

    public TemplateEngine() {
        this.mf = new DefaultMustacheFactory("templates");
    }

    /**
     * Render a template to a string.
     * @param templatePath path relative to resources/templates/ (e.g., "backend/entity.java.mustache")
     * @param context      the data map to populate the template
     * @return rendered string
     */
    public String render(String templatePath, Map<String, Object> context) {
        Mustache mustache = mf.compile(templatePath);
        StringWriter writer = new StringWriter();
        mustache.execute(writer, context);
        return writer.toString();
    }

    /**
     * Render a template and write it to a file.
     * @param templatePath path relative to resources/templates/
     * @param context      the data map
     * @param outputPath   where to write the rendered content
     */
    public void renderToFile(String templatePath, Map<String, Object> context, Path outputPath) throws IOException {
        String content = render(templatePath, context);
        FileUtils.writeFile(outputPath, content);
    }

    /**
     * Render a raw template string (not from file).
     */
    public String renderInline(String template, Map<String, Object> context) {
        StringReader reader = new StringReader(template);
        Mustache mustache = mf.compile(reader, "inline");
        StringWriter writer = new StringWriter();
        mustache.execute(writer, context);
        return writer.toString();
    }
}
