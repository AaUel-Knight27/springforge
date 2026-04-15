package dev.springforge.generator;

import dev.springforge.model.EntityDefinition;
import dev.springforge.model.FieldDefinition;
import dev.springforge.model.ServiceDefinition;
import dev.springforge.util.ConsoleOutput;
import dev.springforge.util.NameUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Generates Flyway SQL migration files.
 */
public class MigrationGenerator {

    private final TemplateEngine engine = new TemplateEngine();

    /**
     * Generate a CREATE TABLE migration.
     */
    public void generateCreateTable(Path projectDir, ServiceDefinition service, EntityDefinition entity, int version) throws IOException {
        String serviceDirName = service.getName() + "-service";
        Path migrationDir = projectDir.resolve("services").resolve(serviceDirName)
                .resolve("src/main/resources/db/migration");

        String fileName = String.format("V%d__create_%s.sql", version, entity.getTableName());

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("tableName", entity.getTableName());

        List<Map<String, Object>> columns = new ArrayList<>();
        List<FieldDefinition> fields = entity.getFields();
        for (int i = 0; i < fields.size(); i++) {
            FieldDefinition field = fields.get(i);
            Map<String, Object> col = new HashMap<>();
            col.put("columnName", field.getColumnName());
            col.put("sqlType", field.getSqlType());
            col.put("nullable", field.isNullable());
            col.put("unique", field.isUnique());
            col.put("last", i == fields.size() - 1);
            columns.add(col);
        }
        ctx.put("columns", columns);
        ctx.put("hasColumns", !columns.isEmpty());

        engine.renderToFile("migration/create-table.sql.mustache", ctx,
                migrationDir.resolve(fileName));
        ConsoleOutput.created("db/migration/" + fileName);
    }

    /**
     * Generate an ALTER TABLE migration to add a foreign key.
     */
    public void generateAddForeignKey(Path projectDir, ServiceDefinition service,
                                       String tableName, String targetTable,
                                       String columnName, int version) throws IOException {
        String serviceDirName = service.getName() + "-service";
        Path migrationDir = projectDir.resolve("services").resolve(serviceDirName)
                .resolve("src/main/resources/db/migration");

        String fileName = String.format("V%d__add_%s_to_%s.sql", version, columnName, tableName);

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("tableName", tableName);
        ctx.put("columnName", columnName);
        ctx.put("targetTable", targetTable);

        engine.renderToFile("migration/add-relation.sql.mustache", ctx,
                migrationDir.resolve(fileName));
        ConsoleOutput.created("db/migration/" + fileName);
    }
}
