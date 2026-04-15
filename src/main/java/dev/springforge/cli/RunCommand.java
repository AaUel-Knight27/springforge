package dev.springforge.cli;

import dev.springforge.generator.ConfigManager;
import dev.springforge.model.ForgeConfig;
import dev.springforge.model.ServiceDefinition;
import dev.springforge.util.ConsoleOutput;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Command(
    name = "run",
    description = "Run all services and frontend in development mode"
)
public class RunCommand implements Runnable {

    @Option(names = {"--service", "-s"}, description = "Run specific service only")
    private String specificService;

    @Option(names = {"--no-frontend"}, description = "Skip frontend", defaultValue = "false")
    private boolean noFrontend;

    @Option(names = {"--no-db"}, description = "Skip database startup", defaultValue = "false")
    private boolean noDb;

    private final ConfigManager configManager = new ConfigManager();

    @Override
    public void run() {
        try {
            Path projectDir = Paths.get(System.getProperty("user.dir"));
            ForgeConfig config = configManager.loadOrFail(projectDir);

            ConsoleOutput.banner();
            ConsoleOutput.header("Starting Development Environment");

            List<Process> processes = new ArrayList<>();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                ConsoleOutput.info("Shutting down all services...");
                processes.forEach(Process::destroyForcibly);
            }));

            // Start Docker DB
            if (!noDb && config.getDatabase().isDockerManaged()) {
                ConsoleOutput.step("Starting PostgreSQL...");
                Process dbProcess = new ProcessBuilder("docker-compose", "up", "-d")
                        .directory(projectDir.toFile())
                        .inheritIO()
                        .start();
                dbProcess.waitFor();
                ConsoleOutput.success("PostgreSQL started");
                // Wait for DB to be ready
                Thread.sleep(3000);
            }

            // Start Spring Boot service (Monolith)
            ConsoleOutput.step("Starting Spring Boot application...");
            Process process = new ProcessBuilder("mvn", "spring-boot:run")
                    .directory(projectDir.toFile())
                    .inheritIO()
                    .start();
            processes.add(process);

            // Start frontend
            if (!noFrontend) {
                Path frontendDir = projectDir.resolve("frontend");
                if (frontendDir.resolve("package.json").toFile().exists()) {
                    ConsoleOutput.step("Starting Next.js frontend...");
                    Process frontendProcess = new ProcessBuilder("npm", "run", "dev")
                            .directory(frontendDir.toFile())
                            .inheritIO()
                            .start();
                    processes.add(frontendProcess);
                }
            }

            ConsoleOutput.done("All services started!");
            ConsoleOutput.info("Press Ctrl+C to stop all services.");

            // Wait for all processes
            for (Process p : processes) {
                p.waitFor();
            }

        } catch (Exception e) {
            ConsoleOutput.error("Failed to run: " + e.getMessage());

        }
    }
}
