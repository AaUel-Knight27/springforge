package dev.springforge.cli;

import dev.springforge.generator.ConfigManager;
import dev.springforge.generator.MigrationGenerator;
import dev.springforge.model.*;
import dev.springforge.util.ConsoleOutput;
import dev.springforge.util.FileUtils;
import dev.springforge.util.NameUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.nio.file.Paths;

@Command(
    name = "relation",
    description = "Add a JPA relationship between two entities"
)
public class AddRelationCommand implements Runnable {

    @Parameters(index = "0", description = "Source entity name")
    private String sourceEntity;

    @Parameters(index = "1", description = "Target entity name")
    private String targetEntity;

    @Parameters(index = "2", description = "Relation type: one-to-one, one-to-many, many-to-one, many-to-many")
    private String relationType;

    @Option(names = {"--service", "-s"}, description = "Service name", required = true)
    private String serviceName;

    private final ConfigManager configManager = new ConfigManager();
    private final MigrationGenerator migrationGenerator = new MigrationGenerator();

    @Override
    public void run() {
        try {
            Path projectDir = Paths.get(System.getProperty("user.dir"));
            ForgeConfig config = configManager.loadOrFail(projectDir);

            ConsoleOutput.header("Adding Relation: " + sourceEntity + " → " + targetEntity + " (" + relationType + ")");

            ServiceDefinition service = config.getService(serviceName);
            if (service == null) {
                ConsoleOutput.error("Service '" + serviceName + "' not found.");
                return;
            }

            RelationType type = RelationType.fromString(relationType);
            String sourcePascal = NameUtils.toPascalCase(sourceEntity);
            String targetPascal = NameUtils.toPascalCase(targetEntity);
            String targetCamel = NameUtils.toCamelCase(targetEntity);

            // Find source entity file and add annotation
            String packagePath = NameUtils.toPackagePath(service.getPackageName());
            Path javaDir = projectDir.resolve("src/main/java").resolve(packagePath);

            Path entityFile = javaDir.resolve("entity/" + sourcePascal + ".java");
            if (!FileUtils.exists(entityFile)) {
                ConsoleOutput.error("Entity file not found: " + entityFile);
                return;
            }

            // Build the relationship field and annotation
            String relation = switch (type) {
                case MANY_TO_ONE -> String.format("""
                    
                        @ManyToOne(fetch = FetchType.LAZY)
                        @JoinColumn(name = "%s_id")
                        private %s %s;
                    """, NameUtils.toSnakeCase(targetPascal), targetPascal, targetCamel);

                case ONE_TO_MANY -> String.format("""
                    
                        @OneToMany(mappedBy = "%s", cascade = CascadeType.ALL, orphanRemoval = true)
                        private java.util.List<%s> %ss = new java.util.ArrayList<>();
                    """, NameUtils.toCamelCase(sourcePascal), targetPascal, targetCamel);

                case ONE_TO_ONE -> String.format("""
                    
                        @OneToOne(cascade = CascadeType.ALL)
                        @JoinColumn(name = "%s_id", unique = true)
                        private %s %s;
                    """, NameUtils.toSnakeCase(targetPascal), targetPascal, targetCamel);

                case MANY_TO_MANY -> String.format("""
                    
                        @ManyToMany
                        @JoinTable(
                            name = "%s_%s",
                            joinColumns = @JoinColumn(name = "%s_id"),
                            inverseJoinColumns = @JoinColumn(name = "%s_id")
                        )
                        private java.util.Set<%s> %ss = new java.util.HashSet<>();
                    """, NameUtils.toSnakeCase(sourcePascal), NameUtils.toSnakeCase(targetPascal),
                        NameUtils.toSnakeCase(sourcePascal), NameUtils.toSnakeCase(targetPascal),
                        targetPascal, targetCamel);
            };

            // Insert relation before the constructor
            FileUtils.insertBeforeLine(entityFile, "    // Default constructor", relation);
            ConsoleOutput.updated(sourcePascal + ".java (added " + type.getAnnotation() + " " + targetPascal + ")");

            // Generate migration for foreign key (only for ManyToOne and OneToOne)
            if (type == RelationType.MANY_TO_ONE || type == RelationType.ONE_TO_ONE) {
                int version = config.getAndIncrementMigrationVersion();
                String sourceTable = NameUtils.toTableName(sourcePascal);
                String targetTable = NameUtils.toTableName(targetPascal);
                String columnName = NameUtils.toSnakeCase(targetPascal) + "_id";
                migrationGenerator.generateAddForeignKey(projectDir, service, sourceTable, targetTable, columnName, version);
            }

            // Update entity definition
            EntityDefinition entityDef = service.findEntity(sourcePascal);
            if (entityDef != null) {
                entityDef.addRelation(new EntityDefinition.RelationDefinition(targetPascal, type));
            }
            configManager.save(projectDir, config);

            ConsoleOutput.done("Relation added: " + sourcePascal + " " + type.getAnnotation() + " " + targetPascal);
            ConsoleOutput.newline();

        } catch (Exception e) {
            ConsoleOutput.error("Failed to add relation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
