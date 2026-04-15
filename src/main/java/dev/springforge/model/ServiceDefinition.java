package dev.springforge.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.*;

/**
 * Represents a microservice within the project.
 */
public class ServiceDefinition {

    private String name;
    private String packageName;
    private int port;
    private String databaseName;
    private List<EntityDefinition> entities = new ArrayList<>();
    private boolean authEnabled = false;

    public ServiceDefinition() {}

    public ServiceDefinition(String name, String packageName, int port) {
        this.name = name;
        this.packageName = packageName;
        this.port = port;
        this.databaseName = name.replace("-", "_") + "_db";
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getDatabaseName() { return databaseName; }
    public void setDatabaseName(String databaseName) { this.databaseName = databaseName; }

    public List<EntityDefinition> getEntities() { return entities; }
    public void setEntities(List<EntityDefinition> entities) { this.entities = entities; }

    public boolean isAuthEnabled() { return authEnabled; }
    public void setAuthEnabled(boolean authEnabled) { this.authEnabled = authEnabled; }

    public void addEntity(EntityDefinition entity) {
        entities.add(entity);
    }

    @JsonIgnore
    public EntityDefinition findEntity(String name) {
        return entities.stream()
                .filter(e -> e.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}
