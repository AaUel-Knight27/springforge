package dev.springforge.cli;

import dev.springforge.generator.ConfigManager;
import dev.springforge.generator.FrontendGenerator;
import dev.springforge.model.*;
import dev.springforge.util.ConsoleOutput;
import picocli.CommandLine.Command;

import java.nio.file.Path;
import java.nio.file.Paths;

@Command(
    name = "sync",
    description = "Synchronize frontend API layer with backend entities"
)
public class SyncCommand implements Runnable {

    private final ConfigManager configManager = new ConfigManager();
    private final FrontendGenerator frontendGenerator = new FrontendGenerator();

    @Override
    public void run() {
        try {
            Path projectDir = Paths.get(System.getProperty("user.dir"));
            ForgeConfig config = configManager.loadOrFail(projectDir);

            ConsoleOutput.header("Syncing Frontend with Backend");

            // Regenerate base API client
            frontendGenerator.initFrontend(projectDir, config);

            // Regenerate API layer for all entities across all services
            int entityCount = 0;
            for (var entry : config.getServices().entrySet()) {
                String serviceName = entry.getKey();
                ServiceDefinition service = entry.getValue();

                for (EntityDefinition entity : service.getEntities()) {
                    ConsoleOutput.step("Syncing: " + entity.getName() + " (" + serviceName + ")");
                    frontendGenerator.generateEntityApi(projectDir, config, entity, serviceName);
                    entityCount++;
                }
            }

            if (entityCount == 0) {
                ConsoleOutput.warn("No entities found. Add entities first:");
                ConsoleOutput.command("forge add entity <Name> field:type --service <service>");
            } else {
                ConsoleOutput.done("Synced " + entityCount + " entities to frontend!");
            }
            ConsoleOutput.newline();

        } catch (Exception e) {
            ConsoleOutput.error("Failed to sync: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
