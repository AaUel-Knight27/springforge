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
    description = "Initialize SpringForge in an existing Spring Boot application"
)
public class InitCommand implements Runnable {

    private final ConfigManager configManager = new ConfigManager();
    private final TemplateEngine templateEngine = new TemplateEngine();

    @Override
    public void run() {
        try {
            ConsoleOutput.banner();
            ConsoleOutput.header("Initializing SpringForge Project");

            Path projectDir = Paths.get(System.getProperty("user.dir"));
            String projectName = projectDir.getFileName().toString();
            ConsoleOutput.info("Initializing in existing project: " + projectDir);

            // Check if already initialized
            if (configManager.exists(projectDir)) {
                ConsoleOutput.warn("Project already initialized. Use 'forge add' to add components.");
                return;
            }

            // Detect base package from Spring Boot application class
            ConsoleOutput.step("Detecting project structure...");
            String basePackage = detectBasePackage(projectDir);
            ConsoleOutput.info("Found base package: " + basePackage);

            // Create forge.config.yml
            ForgeConfig config = new ForgeConfig();
            config.setProjectName(projectName);
            config.setBasePackage(basePackage);

            // frontend placeholder
            FileUtils.mkdirs(projectDir.resolve("frontend"));
            ConsoleOutput.created("frontend/");

            // Generate docker-compose.yml
            Map<String, Object> dockerContext = new HashMap<>();
            dockerContext.put("projectName", projectName);
            dockerContext.put("databases", List.of()); // starts empty
            templateEngine.renderToFile("docker/docker-compose.yml.mustache", dockerContext,
                    projectDir.resolve("docker-compose.yml"));
            ConsoleOutput.created("docker-compose.yml");

            // Inject Flyway dependency natively if project has pom.xml
            Path pomPath = projectDir.resolve("pom.xml");
            if (FileUtils.exists(pomPath)) {
                String flywayDep = """
                        
                                <!-- Flyway for DB Migrations -->
                                <dependency>
                                    <groupId>org.flywaydb</groupId>
                                    <artifactId>flyway-core</artifactId>
                                </dependency>
                                <dependency>
                                    <groupId>org.flywaydb</groupId>
                                    <artifactId>flyway-database-postgresql</artifactId>
                                </dependency>""";
                if (!FileUtils.readFile(pomPath).contains("flyway-core")) {
                    FileUtils.insertBeforeLine(pomPath, "</dependencies>", flywayDep);
                    ConsoleOutput.updated("pom.xml (added flyway-core + flyway-database-postgresql)");
                }

                // Inject Flyway Maven Plugin
                String flywayPlugin = String.format("""
                        
                                    <plugin>
                                        <groupId>org.flywaydb</groupId>
                                        <artifactId>flyway-maven-plugin</artifactId>
                                        <configuration>
                                            <url>jdbc:postgresql://localhost:5432/%s</url>
                                            <user>postgres</user>
                                            <password>postgres</password>
                                        </configuration>
                                    </plugin>""", projectName.replace("-", "_") + "_db");

                String pomContent = FileUtils.readFile(pomPath);
                if (!pomContent.contains("flyway-maven-plugin")) {
                    if (pomContent.contains("<plugins>")) {
                        FileUtils.insertAfterLine(pomPath, "<plugins>", flywayPlugin);
                    } else if (pomContent.contains("</build>")) {
                        FileUtils.insertBeforeLine(pomPath, "</build>", "\n        <plugins>" + flywayPlugin + "\n        </plugins>");
                    }
                    ConsoleOutput.updated("pom.xml (added flyway-maven-plugin)");
                }
            }

            // Inject application.properties
            Path propertiesPath = projectDir.resolve("src/main/resources/application.properties");
            Path ymlPath = projectDir.resolve("src/main/resources/application.yml");
            Path yamlPath = projectDir.resolve("src/main/resources/application.yaml");

            if (!FileUtils.exists(propertiesPath) && !FileUtils.exists(ymlPath) && !FileUtils.exists(yamlPath)) {
                FileUtils.mkdirs(projectDir.resolve("src/main/resources"));
                FileUtils.writeFile(propertiesPath, "");
                ConsoleOutput.created("src/main/resources/application.properties");
            }

            if (FileUtils.exists(propertiesPath)) {
                String dbProps = String.format("""
                        
                        # Database Configuration (SpringForge)
                        spring.datasource.url=jdbc:postgresql://localhost:5432/%s
                        spring.datasource.username=postgres
                        spring.datasource.password=postgres
                        spring.jpa.hibernate.ddl-auto=validate
                        spring.flyway.enabled=true
                        spring.flyway.locations=classpath:db/migration
                        """, projectName.replace("-", "_") + "_db");

                String propsContent = FileUtils.readFile(propertiesPath);
                if (!propsContent.contains("spring.datasource.url")) {
                    FileUtils.appendFile(propertiesPath, dbProps);
                    ConsoleOutput.updated("application.properties (added DB & Flyway config)");
                }
            }

            // Save config
            configManager.save(projectDir, config);
            ConsoleOutput.created("forge.config.yml");

            // Summary
            ConsoleOutput.done("Project ready for SpringForge!");
            ConsoleOutput.info("Next steps:");
            ConsoleOutput.command("forge add service default");
            ConsoleOutput.command("forge add entity User username:string email:string --service default");
            ConsoleOutput.command("forge setup-db --docker");
            ConsoleOutput.newline();

        } catch (Exception e) {
            ConsoleOutput.error("Failed to initialize project: " + e.getMessage());

        }
    }

    private String detectBasePackage(Path projectDir) {
        Path sourceDir = projectDir.resolve("src/main/java");
        if (!FileUtils.exists(sourceDir)) {
            ConsoleOutput.warn("No src/main/java found, falling back to com.example.app");
            return "com.example.app";
        }
        
        try (java.util.stream.Stream<Path> stream = java.nio.file.Files.walk(sourceDir)) {
            return stream.filter(java.nio.file.Files::isRegularFile)
                  .filter(path -> path.toString().endsWith(".java"))
                  .filter(path -> {
                      try {
                          String content = new String(java.nio.file.Files.readAllBytes(path));
                          return content.contains("@SpringBootApplication");
                      } catch (Exception e) { return false; }
                  })
                  .findFirst()
                  .map(path -> {
                      try {
                          String content = new String(java.nio.file.Files.readAllBytes(path));
                          java.util.regex.Matcher m = java.util.regex.Pattern.compile("^package\\s+([a-zA-Z0-9_.]+);", java.util.regex.Pattern.MULTILINE).matcher(content);
                          if (m.find()) return m.group(1);
                      } catch (Exception e) {}
                      return "com.example.app";
                  })
                  .orElse("com.example.app");
        } catch (java.io.IOException e) {
            return "com.example.app";
        }
    }
}
