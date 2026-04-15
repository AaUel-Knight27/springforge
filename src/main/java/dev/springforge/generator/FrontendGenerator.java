package dev.springforge.generator;

import dev.springforge.model.*;
import dev.springforge.util.ConsoleOutput;
import dev.springforge.util.FileUtils;
import dev.springforge.util.NameUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Generates Next.js frontend files: API clients, hooks, TypeScript types.
 */
public class FrontendGenerator {

    private final TemplateEngine engine = new TemplateEngine();

    /**
     * Initialize the frontend with Next.js structure and base API client.
     */
    public void initFrontend(Path projectDir, ForgeConfig config) throws IOException {
        Path frontendDir = projectDir.resolve("frontend");

        // Create directory structure
        FileUtils.mkdirs(frontendDir.resolve("src/lib"));
        FileUtils.mkdirs(frontendDir.resolve("src/hooks"));
        FileUtils.mkdirs(frontendDir.resolve("src/types"));
        FileUtils.mkdirs(frontendDir.resolve("src/app/admin"));

        // Generate base API client
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("projectName", config.getProjectName());

        // Collect service ports for API routing
        List<Map<String, Object>> services = new ArrayList<>();
        for (var entry : config.getServices().entrySet()) {
            Map<String, Object> svc = new HashMap<>();
            svc.put("name", entry.getKey());
            svc.put("port", entry.getValue().getPort());
            svc.put("urlPath", NameUtils.toUrlPath(entry.getKey()));
            services.add(svc);
        }
        ctx.put("services", services);

        engine.renderToFile("frontend/api-client.ts.mustache", ctx,
                frontendDir.resolve("src/lib/api.ts"));
        ConsoleOutput.created("frontend/src/lib/api.ts");
    }

    /**
     * Generate API functions, hooks, and types for an entity.
     */
    public void generateEntityApi(Path projectDir, ForgeConfig config, EntityDefinition entity, String serviceName) throws IOException {
        Path frontendDir = projectDir.resolve("frontend");
        String pascal = entity.getName();
        String camel = NameUtils.toCamelCase(pascal);
        String kebab = NameUtils.toKebabCase(pascal);
        String urlPath = NameUtils.toUrlPath(pascal);

        // Ensure directories exist
        FileUtils.mkdirs(frontendDir.resolve("src/lib/api"));
        FileUtils.mkdirs(frontendDir.resolve("src/hooks"));
        FileUtils.mkdirs(frontendDir.resolve("src/types"));

        // Build context
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("entityName", pascal);
        ctx.put("entityNameLower", camel);
        ctx.put("entityNameKebab", kebab);
        ctx.put("urlPath", urlPath);
        ctx.put("serviceName", serviceName);

        ServiceDefinition service = config.getService(serviceName);
        if (service != null) {
            ctx.put("servicePort", service.getPort());
        }

        // Build fields for TypeScript interface
        List<Map<String, Object>> fields = new ArrayList<>();
        for (FieldDefinition field : entity.getFields()) {
            Map<String, Object> f = new HashMap<>();
            f.put("name", field.getName());
            f.put("tsType", field.getTsType());
            f.put("nullable", field.isNullable());
            fields.add(f);
        }
        ctx.put("fields", fields);
        ctx.put("hasFields", !fields.isEmpty());

        // Generate TypeScript types
        engine.renderToFile("frontend/types.ts.mustache", ctx,
                frontendDir.resolve("src/types/" + kebab + ".ts"));
        ConsoleOutput.created("frontend/src/types/" + kebab + ".ts");

        // Generate API resource functions
        engine.renderToFile("frontend/api-resource.ts.mustache", ctx,
                frontendDir.resolve("src/lib/api/" + kebab + ".ts"));
        ConsoleOutput.created("frontend/src/lib/api/" + kebab + ".ts");

        // Generate React hook
        engine.renderToFile("frontend/hook.ts.mustache", ctx,
                frontendDir.resolve("src/hooks/use-" + kebab + ".ts"));
        ConsoleOutput.created("frontend/src/hooks/use-" + kebab + ".ts");
    }
}
