package dev.springforge.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents a field/column in an entity.
 */
public class FieldDefinition {

    private String name;          // camelCase: "firstName"
    private String columnName;    // snake_case: "first_name"
    private FieldType type;
    private boolean nullable = true;
    private boolean unique = false;

    public FieldDefinition() {}

    public FieldDefinition(String name, String columnName, FieldType type) {
        this.name = name;
        this.columnName = columnName;
        this.type = type;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColumnName() { return columnName; }
    public void setColumnName(String columnName) { this.columnName = columnName; }

    public FieldType getType() { return type; }
    public void setType(FieldType type) { this.type = type; }

    public boolean isNullable() { return nullable; }
    public void setNullable(boolean nullable) { this.nullable = nullable; }

    public boolean isUnique() { return unique; }
    public void setUnique(boolean unique) { this.unique = unique; }

    /**
     * Get the Java type string.
     */
    @JsonIgnore
    public String getJavaType() {
        return type.getJavaType();
    }

    /**
     * Get the SQL column type for PostgreSQL.
     */
    @JsonIgnore
    public String getSqlType() {
        return type.getSqlType();
    }

    /**
     * Get the TypeScript type string.
     */
    @JsonIgnore
    public String getTsType() {
        return type.getTsType();
    }

    /**
     * Field type mappings across Java, SQL, and TypeScript.
     */
    public enum FieldType {
        STRING("String", "VARCHAR(255)", "string"),
        TEXT("String", "TEXT", "string"),
        INTEGER("Integer", "INTEGER", "number"),
        LONG("Long", "BIGINT", "number"),
        DOUBLE("Double", "DOUBLE PRECISION", "number"),
        BOOLEAN("Boolean", "BOOLEAN", "boolean"),
        DATE("LocalDate", "DATE", "string"),
        DATETIME("LocalDateTime", "TIMESTAMP", "string"),
        UUID("UUID", "UUID", "string"),
        DECIMAL("BigDecimal", "DECIMAL(19,2)", "number");

        private final String javaType;
        private final String sqlType;
        private final String tsType;

        FieldType(String javaType, String sqlType, String tsType) {
            this.javaType = javaType;
            this.sqlType = sqlType;
            this.tsType = tsType;
        }

        public String getJavaType() { return javaType; }
        public String getSqlType() { return sqlType; }
        public String getTsType() { return tsType; }

        /**
         * Parse field type from CLI input string.
         */
        public static FieldType fromString(String input) {
            return switch (input.toLowerCase()) {
                case "string", "str", "varchar" -> STRING;
                case "text", "longtext" -> TEXT;
                case "int", "integer" -> INTEGER;
                case "long", "bigint" -> LONG;
                case "double", "float" -> DOUBLE;
                case "bool", "boolean" -> BOOLEAN;
                case "date" -> DATE;
                case "datetime", "timestamp" -> DATETIME;
                case "uuid" -> UUID;
                case "decimal", "bigdecimal", "money" -> DECIMAL;
                default -> STRING;
            };
        }
    }
}
