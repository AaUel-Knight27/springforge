package dev.springforge.model;

import java.util.*;

/**
 * Represents a JPA entity within a service.
 */
public class EntityDefinition {

    private String name;           // PascalCase: "BlogPost"
    private String tableName;      // snake_case plural: "blog_posts"
    private List<FieldDefinition> fields = new ArrayList<>();
    private List<RelationDefinition> relations = new ArrayList<>();

    public EntityDefinition() {}

    public EntityDefinition(String name, String tableName) {
        this.name = name;
        this.tableName = tableName;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public List<FieldDefinition> getFields() { return fields; }
    public void setFields(List<FieldDefinition> fields) { this.fields = fields; }

    public List<RelationDefinition> getRelations() { return relations; }
    public void setRelations(List<RelationDefinition> relations) { this.relations = relations; }

    public void addField(FieldDefinition field) {
        fields.add(field);
    }

    public void addRelation(RelationDefinition relation) {
        relations.add(relation);
    }

    /**
     * Represents a relationship to another entity.
     */
    public static class RelationDefinition {
        private String targetEntity;
        private RelationType type;
        private String mappedBy;

        public RelationDefinition() {}

        public RelationDefinition(String targetEntity, RelationType type) {
            this.targetEntity = targetEntity;
            this.type = type;
        }

        public String getTargetEntity() { return targetEntity; }
        public void setTargetEntity(String targetEntity) { this.targetEntity = targetEntity; }
        public RelationType getType() { return type; }
        public void setType(RelationType type) { this.type = type; }
        public String getMappedBy() { return mappedBy; }
        public void setMappedBy(String mappedBy) { this.mappedBy = mappedBy; }
    }
}
