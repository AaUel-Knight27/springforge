package dev.springforge.generator;

import dev.springforge.model.*;
import dev.springforge.util.ConsoleOutput;
import dev.springforge.util.FileUtils;
import dev.springforge.util.NameUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Generates Spring Boot backend files: Entity, Repository, Service, Controller, DTOs.
 */
public class BackendGenerator {

    private final TemplateEngine engine = new TemplateEngine();

    /**
     * Generate the full service scaffold (Application class, pom.xml, application.yml, directory structure).
     */
    public void generateService(Path projectDir, ForgeConfig config, ServiceDefinition service) throws IOException {
        String serviceDirName = service.getName() + "-service";
        Path serviceDir = projectDir.resolve("services").resolve(serviceDirName);
        String packagePath = NameUtils.toPackagePath(service.getPackageName());
        Path javaDir = serviceDir.resolve("src/main/java").resolve(packagePath);
        Path resourcesDir = serviceDir.resolve("src/main/resources");
        Path migrationDir = resourcesDir.resolve("db/migration");

        // Create directories
        FileUtils.mkdirs(javaDir);
        FileUtils.mkdirs(resourcesDir);
        FileUtils.mkdirs(migrationDir);

        // Build context
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("serviceName", service.getName());
        ctx.put("serviceArtifactId", serviceDirName);
        ctx.put("groupId", config.getGroupId());
        ctx.put("packageName", service.getPackageName());
        ctx.put("appClassName", NameUtils.toPascalCase(service.getName()) + "Application");
        ctx.put("port", service.getPort());
        ctx.put("databaseName", service.getDatabaseName());
        ctx.put("dbHost", config.getDatabase().getHost());
        ctx.put("dbPort", config.getDatabase().getPort());
        ctx.put("dbUsername", config.getDatabase().getUsername());
        ctx.put("dbPassword", config.getDatabase().getPassword());
        ctx.put("javaVersion", config.getJavaVersion());
        ctx.put("springBootVersion", config.getSpringBootVersion());

        // Generate pom.xml
        engine.renderToFile("backend/pom.xml.mustache", ctx, serviceDir.resolve("pom.xml"));
        ConsoleOutput.created("services/" + serviceDirName + "/pom.xml");

        // Generate Application.java
        engine.renderToFile("backend/main-app.java.mustache", ctx, javaDir.resolve(ctx.get("appClassName") + ".java"));
        ConsoleOutput.created("services/" + serviceDirName + "/.../" + ctx.get("appClassName") + ".java");

        // Generate application.yml
        engine.renderToFile("backend/application.yml.mustache", ctx, resourcesDir.resolve("application.yml"));
        ConsoleOutput.created("services/" + serviceDirName + "/src/main/resources/application.yml");
    }

    /**
     * Generate entity + full CRUD stack.
     */
    public void generateEntity(Path projectDir, ForgeConfig config, ServiceDefinition service, EntityDefinition entity) throws IOException {
        String serviceDirName = service.getName() + "-service";
        Path serviceDir = projectDir.resolve("services").resolve(serviceDirName);
        String packagePath = NameUtils.toPackagePath(service.getPackageName());
        Path javaDir = serviceDir.resolve("src/main/java").resolve(packagePath);

        String pascal = entity.getName();
        String camel = NameUtils.toCamelCase(pascal);

        // Build common context
        Map<String, Object> ctx = buildEntityContext(config, service, entity);

        // Create subdirectories
        FileUtils.mkdirs(javaDir.resolve("entity"));
        FileUtils.mkdirs(javaDir.resolve("repository"));
        FileUtils.mkdirs(javaDir.resolve("service"));
        FileUtils.mkdirs(javaDir.resolve("controller"));
        FileUtils.mkdirs(javaDir.resolve("dto"));
        FileUtils.mkdirs(javaDir.resolve("exception"));

        // Generate Entity
        engine.renderToFile("backend/entity.java.mustache", ctx,
                javaDir.resolve("entity/" + pascal + ".java"));
        ConsoleOutput.created(pascal + ".java (Entity)");

        // Generate Repository
        engine.renderToFile("backend/repository.java.mustache", ctx,
                javaDir.resolve("repository/" + pascal + "Repository.java"));
        ConsoleOutput.created(pascal + "Repository.java");

        // Generate Service
        engine.renderToFile("backend/service.java.mustache", ctx,
                javaDir.resolve("service/" + pascal + "Service.java"));
        ConsoleOutput.created(pascal + "Service.java");

        // Generate Controller
        engine.renderToFile("backend/controller.java.mustache", ctx,
                javaDir.resolve("controller/" + pascal + "Controller.java"));
        ConsoleOutput.created(pascal + "Controller.java");

        // Generate Request DTO
        engine.renderToFile("backend/dto-request.java.mustache", ctx,
                javaDir.resolve("dto/" + pascal + "Request.java"));
        ConsoleOutput.created(pascal + "Request.java (DTO)");

        // Generate Response DTO
        engine.renderToFile("backend/dto-response.java.mustache", ctx,
                javaDir.resolve("dto/" + pascal + "Response.java"));
        ConsoleOutput.created(pascal + "Response.java (DTO)");

        // Generate ResourceNotFoundException if not already present
        Path exceptionFile = javaDir.resolve("exception/ResourceNotFoundException.java");
        if (!FileUtils.exists(exceptionFile)) {
            engine.renderToFile("backend/exception.java.mustache", ctx, exceptionFile);
            ConsoleOutput.created("ResourceNotFoundException.java");
        }
    }

    /**
     * Add a module reference to the parent POM.
     */
    public void addModuleToParentPom(Path projectDir, String serviceName) throws IOException {
        Path pomPath = projectDir.resolve("pom.xml");
        if (!FileUtils.exists(pomPath)) return;

        String moduleLine = "        <module>services/" + serviceName + "-service</module>";
        String marker = "<!-- forge:modules -->";

        FileUtils.insertAfterLine(pomPath, marker, moduleLine);
        ConsoleOutput.updated("pom.xml (added module: " + serviceName + "-service)");
    }

    /**
     * Build a Mustache context map for entity generation.
     */
    private Map<String, Object> buildEntityContext(ForgeConfig config, ServiceDefinition service, EntityDefinition entity) {
        Map<String, Object> ctx = new HashMap<>();
        String pascal = entity.getName();
        String camel = NameUtils.toCamelCase(pascal);

        ctx.put("packageName", service.getPackageName());
        ctx.put("entityName", pascal);
        ctx.put("entityNameLower", camel);
        ctx.put("tableName", entity.getTableName());
        ctx.put("urlPath", NameUtils.toUrlPath(pascal));

        // Build fields list with metadata
        List<Map<String, Object>> fields = new ArrayList<>();
        Set<String> imports = new TreeSet<>();

        for (FieldDefinition field : entity.getFields()) {
            Map<String, Object> f = new HashMap<>();
            f.put("name", field.getName());
            f.put("nameCapitalized", NameUtils.toPascalCase(field.getName()));
            f.put("columnName", field.getColumnName());
            f.put("javaType", field.getJavaType());
            f.put("sqlType", field.getSqlType());
            f.put("tsType", field.getTsType());
            f.put("nullable", field.isNullable());
            f.put("unique", field.isUnique());
            f.put("isText", field.getType() == FieldDefinition.FieldType.TEXT);
            f.put("isDate", field.getType() == FieldDefinition.FieldType.DATE);
            f.put("isDateTime", field.getType() == FieldDefinition.FieldType.DATETIME);

            fields.add(f);

            // Collect needed imports
            switch (field.getType()) {
                case DATE -> imports.add("java.time.LocalDate");
                case DATETIME -> imports.add("java.time.LocalDateTime");
                case UUID -> imports.add("java.util.UUID");
                case DECIMAL -> imports.add("java.math.BigDecimal");
                default -> {}
            }
        }

        ctx.put("fields", fields);
        ctx.put("hasFields", !fields.isEmpty());

        List<Map<String, String>> importList = imports.stream()
                .map(i -> Map.of("import", i))
                .toList();
        ctx.put("extraImports", importList);
        ctx.put("hasExtraImports", !importList.isEmpty());

        return ctx;
    }
}
