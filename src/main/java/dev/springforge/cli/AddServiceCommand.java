package dev.springforge.cli;

import dev.springforge.generator.BackendGenerator;
import dev.springforge.generator.ConfigManager;
import dev.springforge.model.ForgeConfig;
import dev.springforge.model.ServiceDefinition;
import dev.springforge.util.ConsoleOutput;
import dev.springforge.util.NameUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.nio.file.Paths;

@Command(
    name = "service",
    description = "Add a new microservice to the project"
)
public class AddServiceCommand implements Runnable {

    @Parameters(index = "0", description = "Service name (e.g., user, blog, product)")
    private String serviceName;

    @Option(names = {"--port", "-p"}, description = "Server port", defaultValue = "0")
    private int port;

    private final ConfigManager configManager = new ConfigManager();
    private final BackendGenerator backendGenerator = new BackendGenerator();

    @Override
    public void run() {
        try {
            Path projectDir = Paths.get(System.getProperty("user.dir"));
            ForgeConfig config = configManager.loadOrFail(projectDir);

            ConsoleOutput.header("Adding Service: " + serviceName);

            // Check if service already exists
            if (config.getService(serviceName) != null) {
                ConsoleOutput.warn("Service '" + serviceName + "' already exists.");
                return;
            }

            // Auto-assign port if not specified
            if (port == 0) {
                port = 8080 + config.getServices().size() + 1;
            }

            // Create service definition
            String packageName = config.getGroupId() + "." + serviceName.replace("-", ".");
            ServiceDefinition service = new ServiceDefinition(serviceName, packageName, port);

            // Generate service files
            backendGenerator.generateService(projectDir, config, service);

            // Update parent POM with new module
            backendGenerator.addModuleToParentPom(projectDir, serviceName);

            // Register service in config
            config.addService(serviceName, service);
            configManager.save(projectDir, config);

            ConsoleOutput.done("Service '" + serviceName + "' created on port " + port + "!");
            ConsoleOutput.info("Next: Add entities with:");
            ConsoleOutput.command("forge add entity <Name> field:type field:type --service " + serviceName);
            ConsoleOutput.newline();

        } catch (Exception e) {
            ConsoleOutput.error("Failed to add service: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
