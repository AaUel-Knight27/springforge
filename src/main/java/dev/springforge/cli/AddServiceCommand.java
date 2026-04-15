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
    description = "Add a bounded context domain (package) to your Spring Boot monolith"
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

            // Create service definition
            String packageName = config.getBasePackage() + "." + serviceName.replace("-", ".");
            ServiceDefinition service = new ServiceDefinition(serviceName, packageName, 8080);

            // Register service in config
            config.addService(serviceName, service);
            configManager.save(projectDir, config);

            ConsoleOutput.done("Service domain '" + serviceName + "' registered in package: " + packageName);
            ConsoleOutput.info("Next: Add entities with:");
            ConsoleOutput.command("forge add entity <Name> field:type field:type --service " + serviceName);
            ConsoleOutput.newline();

        } catch (Exception e) {
            ConsoleOutput.error("Failed to add service: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
