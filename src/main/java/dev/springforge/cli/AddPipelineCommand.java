package dev.springforge.cli;

import dev.springforge.generator.ConfigManager;
import dev.springforge.model.ForgeConfig;
import dev.springforge.util.ConsoleOutput;
import dev.springforge.util.FileUtils;
import picocli.CommandLine.Command;

import java.nio.file.Path;
import java.nio.file.Paths;

@Command(
    name = "pipeline",
    description = "Add a CI/CD pipeline (GitHub Actions + Dockerfile) to the application"
)
public class AddPipelineCommand implements Runnable {

    private final ConfigManager configManager = new ConfigManager();

    @Override
    public void run() {
        try {
            Path projectDir = Paths.get(System.getProperty("user.dir"));
            ForgeConfig config = configManager.loadOrFail(projectDir);

            ConsoleOutput.header("Adding CI/CD Pipeline (GitHub Actions)");

            // 1. Create Dockerfile
            Path dockerfilePath = projectDir.resolve("Dockerfile");
            if (!FileUtils.exists(dockerfilePath)) {
                String dockerfile = """
                        FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
                        WORKDIR /app
                        COPY pom.xml .
                        RUN mvn dependency:go-offline
                        COPY src ./src
                        RUN mvn clean package -DskipTests

                        FROM eclipse-temurin:21-jre-alpine
                        WORKDIR /app
                        COPY --from=build /app/target/*.jar app.jar
                        EXPOSE 8080
                        ENTRYPOINT ["java", "-jar", "app.jar"]
                        """;
                FileUtils.writeFile(dockerfilePath, dockerfile);
                ConsoleOutput.created("Dockerfile");
            }

            // 2. Create .github/workflows/deploy.yml
            Path workflowsDir = projectDir.resolve(".github/workflows");
            FileUtils.mkdirs(workflowsDir);
            Path deployYmlPath = workflowsDir.resolve("deploy.yml");

            if (!FileUtils.exists(deployYmlPath)) {
                String deployYml = """
                        name: CI/CD Pipeline
                        
                        on:
                          push:
                            branches: [ "main" ]
                          pull_request:
                            branches: [ "main" ]
                        
                        jobs:
                          build:
                            runs-on: ubuntu-latest
                            steps:
                            - uses: actions/checkout@v4
                            - name: Set up JDK 21
                              uses: actions/setup-java@v4
                              with:
                                java-version: '21'
                                distribution: 'temurin'
                                cache: maven
                            - name: Build with Maven
                              run: mvn -B package --file pom.xml
                            
                            - name: Build Docker Image
                              run: docker build -t ${{ github.repository }}:latest .
                        """;
                FileUtils.writeFile(deployYmlPath, deployYml);
                ConsoleOutput.created(".github/workflows/deploy.yml");
            } else {
                ConsoleOutput.warn("Pipeline already exists at .github/workflows/deploy.yml");
            }

            ConsoleOutput.done("CI/CD Pipeline injected successfully!");

        } catch (Exception e) {
            ConsoleOutput.error("Failed to add pipeline: " + e.getMessage());

        }
    }
}
