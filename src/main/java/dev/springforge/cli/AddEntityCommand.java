package dev.springforge.cli;

import dev.springforge.generator.BackendGenerator;
import dev.springforge.generator.ConfigManager;
import dev.springforge.generator.FrontendGenerator;
import dev.springforge.generator.MigrationGenerator;
import dev.springforge.model.*;
import dev.springforge.model.FieldDefinition.FieldType;
import dev.springforge.util.ConsoleOutput;
import dev.springforge.util.NameUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Command(
    name = "entity",
    description = "Add a new entity with full CRUD stack (Entity, Repository, Service, Controller, DTOs, Migration)"
)
public class AddEntityCommand implements Runnable {

    @Parameters(index = "0", description = "Entity name in PascalCase (e.g., BlogPost)")
    private String entityName;

    @Parameters(index = "1..*", arity = "0..*", description = "Fields in format name:type (e.g., title:string content:text published:boolean)")
    private List<String> fieldSpecs;

    @Option(names = {"--service", "-s"}, description = "Target service name", required = true)
    private String serviceName;

    private final ConfigManager configManager = new ConfigManager();
    private final BackendGenerator backendGenerator = new BackendGenerator();
    private final MigrationGenerator migrationGenerator = new MigrationGenerator();
    private final FrontendGenerator frontendGenerator = new FrontendGenerator();

    @Override
    public void run() {
        try {
            Path projectDir = Paths.get(System.getProperty("user.dir"));
            ForgeConfig config = configManager.loadOrFail(projectDir);

            ConsoleOutput.header("Adding Entity: " + entityName + " → " + serviceName);

            // Validate service exists
            ServiceDefinition service = config.getService(serviceName);
            if (service == null) {
                ConsoleOutput.error("Service '" + serviceName + "' not found. Create it first:");
                ConsoleOutput.command("forge add service " + serviceName);
                return;
            }

            // Check if entity already exists
            if (service.findEntity(entityName) != null) {
                ConsoleOutput.warn("Entity '" + entityName + "' already exists in service '" + serviceName + "'.");
                return;
            }

            // Parse entity name
            String pascalName = NameUtils.toPascalCase(entityName);
            String tableName = NameUtils.toTableName(entityName);

            // Build entity definition
            EntityDefinition entity = new EntityDefinition(pascalName, tableName);

            // Parse field specs
            if (fieldSpecs != null) {
                for (String spec : fieldSpecs) {
                    String[] parts = spec.split(":");
                    if (parts.length < 2) {
                        ConsoleOutput.warn("Skipping invalid field spec: " + spec + " (expected name:type[:validator])");
                        continue;
                    }
                    String fieldName = NameUtils.toCamelCase(parts[0]);
                    String columnName = NameUtils.toSnakeCase(parts[0]);
                    FieldType fieldType = FieldType.fromString(parts[1]);
                    
                    FieldDefinition fd = new FieldDefinition(fieldName, columnName, fieldType);
                    
                    // Parse optional validators
                    for (int i = 2; i < parts.length; i++) {
                        fd.addValidator(parts[i].toLowerCase());
                    }
                    
                    entity.addField(fd);
                    ConsoleOutput.info("Field: " + fieldName + " (" + fieldType.getJavaType() + " → " + fieldType.getSqlType() + ") " + (parts.length > 2 ? "+ validators" : ""));
                }
            }

            // Generate backend files
            ConsoleOutput.step("Generating backend code...");
            backendGenerator.generateEntity(projectDir, config, service, entity);

            // Generate Flyway migration
            ConsoleOutput.step("Generating database migration...");
            int version = config.getAndIncrementMigrationVersion();
            migrationGenerator.generateCreateTable(projectDir, service, entity, version);

            // Generate frontend API + hooks (if frontend is set up)
            ConsoleOutput.step("Generating frontend API layer...");
            frontendGenerator.generateEntityApi(projectDir, config, entity, serviceName);

            // Register entity in config
            service.addEntity(entity);
            configManager.save(projectDir, config);

            // Summary
            ConsoleOutput.done("Entity '" + pascalName + "' created with full CRUD stack!");
            ConsoleOutput.newline();
            ConsoleOutput.info("Generated endpoints:");
            String basePath = "/api/" + NameUtils.toUrlPath(pascalName);
            ConsoleOutput.info("  GET    " + basePath + "         → List all");
            ConsoleOutput.info("  GET    " + basePath + "/{id}    → Get by ID");
            ConsoleOutput.info("  POST   " + basePath + "         → Create");
            ConsoleOutput.info("  PUT    " + basePath + "/{id}    → Update");
            ConsoleOutput.info("  DELETE " + basePath + "/{id}    → Delete");
            ConsoleOutput.newline();

        } catch (Exception e) {
            ConsoleOutput.error("Failed to add entity: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
