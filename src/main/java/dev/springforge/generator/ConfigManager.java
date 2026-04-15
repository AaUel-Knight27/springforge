package dev.springforge.generator;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import dev.springforge.model.ForgeConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Manages reading and writing forge.config.yml.
 */
public class ConfigManager {

    private static final String CONFIG_FILE = "forge.config.yml";
    private final ObjectMapper yaml;

    public ConfigManager() {
        YAMLFactory factory = new YAMLFactory()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
        this.yaml = new ObjectMapper(factory)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Load config from the project directory.
     */
    public ForgeConfig load(Path projectDir) throws IOException {
        Path configPath = projectDir.resolve(CONFIG_FILE);
        if (!Files.exists(configPath)) {
            return null;
        }
        return yaml.readValue(configPath.toFile(), ForgeConfig.class);
    }

    /**
     * Save config to the project directory.
     */
    public void save(Path projectDir, ForgeConfig config) throws IOException {
        Path configPath = projectDir.resolve(CONFIG_FILE);
        yaml.writeValue(configPath.toFile(), config);
    }

    /**
     * Check if a forge.config.yml exists in the given directory.
     */
    public boolean exists(Path projectDir) {
        return Files.exists(projectDir.resolve(CONFIG_FILE));
    }

    /**
     * Load config or throw an error if not found.
     */
    public ForgeConfig loadOrFail(Path projectDir) throws IOException {
        ForgeConfig config = load(projectDir);
        if (config == null) {
            throw new IOException("No forge.config.yml found in " + projectDir
                    + ". Run 'forge init' first to initialize your project.");
        }
        return config;
    }
}
