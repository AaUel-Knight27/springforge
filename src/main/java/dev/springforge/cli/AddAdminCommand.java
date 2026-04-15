package dev.springforge.cli;

import dev.springforge.generator.ConfigManager;
import dev.springforge.generator.TemplateEngine;
import dev.springforge.model.*;
import dev.springforge.util.ConsoleOutput;
import dev.springforge.util.FileUtils;
import dev.springforge.util.NameUtils;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Command(
    name = "admin",
    description = "Generate Django-like admin panel with CRUD for all entities"
)
public class AddAdminCommand implements Runnable {

    private final ConfigManager configManager = new ConfigManager();
    private final TemplateEngine engine = new TemplateEngine();

    @Override
    public void run() {
        try {
            Path projectDir = Paths.get(System.getProperty("user.dir"));
            ForgeConfig config = configManager.loadOrFail(projectDir);

            ConsoleOutput.header("Generating Admin Panel");

            Path frontendDir = projectDir.resolve("frontend");
            Path adminDir = frontendDir.resolve("src/app/admin");
            FileUtils.mkdirs(adminDir);

            // Collect all entities across all services
            List<Map<String, Object>> allEntities = new ArrayList<>();
            for (var entry : config.getServices().entrySet()) {
                String serviceName = entry.getKey();
                ServiceDefinition service = entry.getValue();
                for (EntityDefinition entity : service.getEntities()) {
                    Map<String, Object> entityInfo = new HashMap<>();
                    entityInfo.put("entityName", entity.getName());
                    entityInfo.put("entityNameLower", NameUtils.toCamelCase(entity.getName()));
                    entityInfo.put("entityNameKebab", NameUtils.toKebabCase(entity.getName()));
                    entityInfo.put("urlPath", NameUtils.toUrlPath(entity.getName()));
                    entityInfo.put("serviceName", serviceName);

                    List<Map<String, Object>> fields = new ArrayList<>();
                    for (FieldDefinition field : entity.getFields()) {
                        Map<String, Object> f = new HashMap<>();
                        f.put("name", field.getName());
                        f.put("nameCapitalized", NameUtils.toPascalCase(field.getName()));
                        f.put("tsType", field.getTsType());
                        f.put("javaType", field.getJavaType());
                        fields.add(f);
                    }
                    entityInfo.put("fields", fields);
                    entityInfo.put("hasFields", !fields.isEmpty());
                    allEntities.add(entityInfo);
                }
            }

            if (allEntities.isEmpty()) {
                ConsoleOutput.warn("No entities found. Add entities first, then run 'forge add admin' again.");
                return;
            }

            // Generate admin layout
            Map<String, Object> layoutCtx = new HashMap<>();
            layoutCtx.put("entities", allEntities);
            layoutCtx.put("projectName", config.getProjectName());
            layoutCtx.put("hasAuth", config.isAuthEnabled());

            engine.renderToFile("frontend/admin/layout.tsx.mustache", layoutCtx,
                    adminDir.resolve("layout.tsx"));
            ConsoleOutput.created("admin/layout.tsx");

            // Generate admin dashboard page
            engine.renderToFile("frontend/admin/page.tsx.mustache", layoutCtx,
                    adminDir.resolve("page.tsx"));
            ConsoleOutput.created("admin/page.tsx");

            // Generate per-entity admin pages
            for (Map<String, Object> entityInfo : allEntities) {
                String kebab = (String) entityInfo.get("entityNameKebab");
                Path entityDir = adminDir.resolve(kebab);
                FileUtils.mkdirs(entityDir);

                // Data table page
                engine.renderToFile("frontend/admin/data-table.tsx.mustache", entityInfo,
                        entityDir.resolve("page.tsx"));
                ConsoleOutput.created("admin/" + kebab + "/page.tsx (data table)");

                // Form page (create/edit)
                engine.renderToFile("frontend/admin/form.tsx.mustache", entityInfo,
                        entityDir.resolve("form.tsx"));
                ConsoleOutput.created("admin/" + kebab + "/form.tsx");
            }

            // Update config
            config.setAdminEnabled(true);
            config.setFrontendEnabled(true);
            configManager.save(projectDir, config);

            ConsoleOutput.done("Admin panel generated!");
            ConsoleOutput.info("Access at: http://localhost:3000/admin");
            ConsoleOutput.info("Entities registered: " + allEntities.size());
            ConsoleOutput.newline();

        } catch (Exception e) {
            ConsoleOutput.error("Failed to generate admin panel: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
