package dev.springforge.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.*;

/**
 * Represents the forge.config.yml project configuration.
 * This file tracks all project metadata, services, entities, and state.
 */
public class ForgeConfig {

    private String projectName;
    private String basePackage;
    private int nextMigrationVersion = 1;
    private Map<String, ServiceDefinition> services = new LinkedHashMap<>();
    private DatabaseConfig database = new DatabaseConfig();
    private boolean authEnabled = false;
    private String authService;
    private boolean adminEnabled = false;
    private boolean frontendEnabled = false;

    // Getters and setters
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getBasePackage() { return basePackage; }
    public void setBasePackage(String basePackage) { this.basePackage = basePackage; }

    public int getNextMigrationVersion() { return nextMigrationVersion; }
    public void setNextMigrationVersion(int nextMigrationVersion) { this.nextMigrationVersion = nextMigrationVersion; }

    @JsonIgnore
    public int getAndIncrementMigrationVersion() {
        return nextMigrationVersion++;
    }

    public Map<String, ServiceDefinition> getServices() { return services; }
    public void setServices(Map<String, ServiceDefinition> services) { this.services = services; }

    public DatabaseConfig getDatabase() { return database; }
    public void setDatabase(DatabaseConfig database) { this.database = database; }

    public boolean isAuthEnabled() { return authEnabled; }
    public void setAuthEnabled(boolean authEnabled) { this.authEnabled = authEnabled; }

    public String getAuthService() { return authService; }
    public void setAuthService(String authService) { this.authService = authService; }

    public boolean isAdminEnabled() { return adminEnabled; }
    public void setAdminEnabled(boolean adminEnabled) { this.adminEnabled = adminEnabled; }

    public boolean isFrontendEnabled() { return frontendEnabled; }
    public void setFrontendEnabled(boolean frontendEnabled) { this.frontendEnabled = frontendEnabled; }

    @JsonIgnore
    public void addService(String name, ServiceDefinition service) {
        services.put(name, service);
    }

    @JsonIgnore
    public ServiceDefinition getService(String name) {
        return services.get(name);
    }

    /**
     * Database configuration sub-object.
     */
    public static class DatabaseConfig {
        private String type = "postgresql";
        private String host = "localhost";
        private int port = 5432;
        private String username = "postgres";
        private String password = "postgres";
        private boolean dockerManaged = false;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public boolean isDockerManaged() { return dockerManaged; }
        public void setDockerManaged(boolean dockerManaged) { this.dockerManaged = dockerManaged; }
    }
}
