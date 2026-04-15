package dev.springforge.cli;

import dev.springforge.generator.ConfigManager;
import dev.springforge.generator.TemplateEngine;
import dev.springforge.model.ForgeConfig;
import dev.springforge.util.ConsoleOutput;
import dev.springforge.util.FileUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Command(
    name = "init",
    description = "Initialize a new SpringForge project or enhance an existing Spring Boot project"
)
public class InitCommand implements Runnable {

    @Parameters(index = "0", description = "Project name", defaultValue = "")
    private String projectName;

    @Option(names = {"--group-id", "-g"}, description = "Maven group ID", defaultValue = "com.example")
    private String groupId;

    @Option(names = {"--java"}, description = "Java version", defaultValue = "25")
    private String javaVersion;

    @Option(names = {"--microservices", "-m"}, description = "Use microservice architecture", defaultValue = "true")
    private boolean microservices;

    private final ConfigManager configManager = new ConfigManager();
    private final TemplateEngine templateEngine = new TemplateEngine();

    @Override
    public void run() {
        try {
            ConsoleOutput.banner();
            ConsoleOutput.header("Initializing SpringForge Project");

            Path projectDir;
            if (projectName == null || projectName.isEmpty()) {
                // Enhance existing project in current directory
                projectDir = Paths.get(System.getProperty("user.dir"));
                projectName = projectDir.getFileName().toString();
                ConsoleOutput.info("Initializing in existing directory: " + projectDir);
            } else {
                // Create new project directory
                projectDir = Paths.get(System.getProperty("user.dir"), projectName);
                FileUtils.mkdirs(projectDir);
                ConsoleOutput.info("Creating new project: " + projectName);
            }

            // Check if already initialized
            if (configManager.exists(projectDir)) {
                ConsoleOutput.warn("Project already initialized. Use 'forge add' to add components.");
                return;
            }

            // Create forge.config.yml
            ForgeConfig config = new ForgeConfig();
            config.setProjectName(projectName);
            config.setGroupId(groupId);
            config.setJavaVersion(javaVersion);
            config.setMicroservices(microservices);

            // Create project structure
            ConsoleOutput.step("Creating project structure...");

            // services directory
            FileUtils.mkdirs(projectDir.resolve("services"));
            ConsoleOutput.created("services/");

            // frontend placeholder
            FileUtils.mkdirs(projectDir.resolve("frontend"));
            ConsoleOutput.created("frontend/");

            // scripts directory
            FileUtils.mkdirs(projectDir.resolve("scripts"));
            ConsoleOutput.created("scripts/");

            // Generate parent POM
            Map<String, Object> pomContext = new HashMap<>();
            pomContext.put("projectName", projectName);
            pomContext.put("groupId", groupId);
            pomContext.put("javaVersion", javaVersion);
            pomContext.put("springBootVersion", config.getSpringBootVersion());
            pomContext.put("modules", List.of()); // starts empty
            templateEngine.renderToFile("backend/parent-pom.xml.mustache", pomContext,
                    projectDir.resolve("pom.xml"));
            ConsoleOutput.created("pom.xml");

            // Generate docker-compose.yml
            Map<String, Object> dockerContext = new HashMap<>();
            dockerContext.put("projectName", projectName);
            dockerContext.put("databases", List.of()); // starts empty
            templateEngine.renderToFile("docker/docker-compose.yml.mustache", dockerContext,
                    projectDir.resolve("docker-compose.yml"));
            ConsoleOutput.created("docker-compose.yml");

            // Generate .gitignore
            String gitignore = """
                    # Java
                    target/
                    *.class
                    *.jar
                    *.war
                    .idea/
                    *.iml
                    .settings/
                    .project
                    .classpath
                    
                    # Node
                    node_modules/
                    .next/
                    
                    # Environment
                    .env
                    .env.local
                    
                    # OS
                    .DS_Store
                    Thumbs.db
                    """;
            FileUtils.writeFile(projectDir.resolve(".gitignore"), gitignore);
            ConsoleOutput.created(".gitignore");

            // Save config
            configManager.save(projectDir, config);
            ConsoleOutput.created("forge.config.yml");

            // Summary
            ConsoleOutput.done("Project initialized successfully!");
            ConsoleOutput.info("Next steps:");
            ConsoleOutput.command("cd " + projectName);
            ConsoleOutput.command("forge add service user");
            ConsoleOutput.command("forge add entity User username:string email:string password:string --service user");
            ConsoleOutput.command("forge setup-db --docker");
            ConsoleOutput.newline();

        } catch (Exception e) {
            ConsoleOutput.error("Failed to initialize project: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
