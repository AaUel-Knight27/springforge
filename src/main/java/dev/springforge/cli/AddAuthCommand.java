package dev.springforge.cli;

import dev.springforge.generator.ConfigManager;
import dev.springforge.generator.TemplateEngine;
import dev.springforge.model.ForgeConfig;
import dev.springforge.model.ServiceDefinition;
import dev.springforge.util.ConsoleOutput;
import dev.springforge.util.FileUtils;
import dev.springforge.util.NameUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Command(
    name = "auth",
    description = "Add JWT authentication + Spring Security to a service"
)
public class AddAuthCommand implements Runnable {

    @Option(names = {"--service", "-s"}, description = "Target service name", required = true)
    private String serviceName;

    @Option(names = {"--jwt-secret"}, description = "JWT secret key", defaultValue = "springforge-secret-key-change-in-production-2024")
    private String jwtSecret;

    private final ConfigManager configManager = new ConfigManager();
    private final TemplateEngine engine = new TemplateEngine();

    @Override
    public void run() {
        try {
            Path projectDir = Paths.get(System.getProperty("user.dir"));
            ForgeConfig config = configManager.loadOrFail(projectDir);

            ConsoleOutput.header("Adding Authentication to: " + serviceName);

            ServiceDefinition service = config.getService(serviceName);
            if (service == null) {
                ConsoleOutput.error("Service '" + serviceName + "' not found.");
                return;
            }

            if (service.isAuthEnabled()) {
                ConsoleOutput.warn("Authentication already configured for '" + serviceName + "'.");
                return;
            }

            String serviceDirName = serviceName + "-service";
            Path serviceDir = projectDir.resolve("services").resolve(serviceDirName);
            String packagePath = NameUtils.toPackagePath(service.getPackageName());
            Path javaDir = serviceDir.resolve("src/main/java").resolve(packagePath);

            // Context for templates
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("packageName", service.getPackageName());
            ctx.put("jwtSecret", jwtSecret);
            ctx.put("serviceName", serviceName);

            // Create auth subdirectories
            FileUtils.mkdirs(javaDir.resolve("security"));
            FileUtils.mkdirs(javaDir.resolve("auth"));
            FileUtils.mkdirs(javaDir.resolve("auth/dto"));

            // Security Config
            engine.renderToFile("backend/auth/security-config.java.mustache", ctx,
                    javaDir.resolve("security/SecurityConfig.java"));
            ConsoleOutput.created("SecurityConfig.java");

            // JWT Provider
            engine.renderToFile("backend/auth/jwt-provider.java.mustache", ctx,
                    javaDir.resolve("security/JwtTokenProvider.java"));
            ConsoleOutput.created("JwtTokenProvider.java");

            // JWT Filter
            engine.renderToFile("backend/auth/jwt-filter.java.mustache", ctx,
                    javaDir.resolve("security/JwtAuthenticationFilter.java"));
            ConsoleOutput.created("JwtAuthenticationFilter.java");

            // User Entity (auth-specific)
            engine.renderToFile("backend/auth/user-entity.java.mustache", ctx,
                    javaDir.resolve("auth/UserEntity.java"));
            ConsoleOutput.created("UserEntity.java");

            // User Repository
            engine.renderToFile("backend/auth/user-repository.java.mustache", ctx,
                    javaDir.resolve("auth/UserRepository.java"));
            ConsoleOutput.created("UserRepository.java");

            // Auth Service
            engine.renderToFile("backend/auth/auth-service.java.mustache", ctx,
                    javaDir.resolve("auth/AuthService.java"));
            ConsoleOutput.created("AuthService.java");

            // Auth Controller
            engine.renderToFile("backend/auth/auth-controller.java.mustache", ctx,
                    javaDir.resolve("auth/AuthController.java"));
            ConsoleOutput.created("AuthController.java");

            // DTOs
            engine.renderToFile("backend/auth/login-request.java.mustache", ctx,
                    javaDir.resolve("auth/dto/LoginRequest.java"));
            ConsoleOutput.created("LoginRequest.java");

            engine.renderToFile("backend/auth/signup-request.java.mustache", ctx,
                    javaDir.resolve("auth/dto/SignupRequest.java"));
            ConsoleOutput.created("SignupRequest.java");

            engine.renderToFile("backend/auth/auth-response.java.mustache", ctx,
                    javaDir.resolve("auth/dto/AuthResponse.java"));
            ConsoleOutput.created("AuthResponse.java");

            // Generate user migration
            int version = config.getAndIncrementMigrationVersion();
            Path migrationDir = serviceDir.resolve("src/main/resources/db/migration");
            String migrationFile = String.format("V%d__create_users_and_roles.sql", version);

            String migration = """
                    -- SpringForge: Users and Roles tables for authentication
                    
                    CREATE TABLE users (
                        id BIGSERIAL PRIMARY KEY,
                        username VARCHAR(50) UNIQUE NOT NULL,
                        email VARCHAR(100) UNIQUE NOT NULL,
                        password VARCHAR(255) NOT NULL,
                        role VARCHAR(20) DEFAULT 'USER',
                        enabled BOOLEAN DEFAULT TRUE,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    );
                    
                    CREATE INDEX idx_users_username ON users(username);
                    CREATE INDEX idx_users_email ON users(email);
                    """;
            FileUtils.writeFile(migrationDir.resolve(migrationFile), migration);
            ConsoleOutput.created("db/migration/" + migrationFile);

            // Add Spring Security dependency to service POM
            addSecurityDependency(serviceDir);

            // Update config
            service.setAuthEnabled(true);
            config.setAuthEnabled(true);
            config.setAuthService(serviceName);
            configManager.save(projectDir, config);

            ConsoleOutput.done("Authentication added to '" + serviceName + "'!");
            ConsoleOutput.info("Endpoints:");
            ConsoleOutput.info("  POST /api/auth/signup  → Register");
            ConsoleOutput.info("  POST /api/auth/login   → Login (returns JWT)");
            ConsoleOutput.info("  GET  /api/auth/me      → Current user info");
            ConsoleOutput.newline();

        } catch (Exception e) {
            ConsoleOutput.error("Failed to add auth: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addSecurityDependency(Path serviceDir) throws Exception {
        Path pomPath = serviceDir.resolve("pom.xml");
        if (!FileUtils.exists(pomPath)) return;

        String securityDep = """
                
                        <!-- Spring Security -->
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-security</artifactId>
                        </dependency>
                        <!-- JWT -->
                        <dependency>
                            <groupId>io.jsonwebtoken</groupId>
                            <artifactId>jjwt-api</artifactId>
                            <version>0.12.6</version>
                        </dependency>
                        <dependency>
                            <groupId>io.jsonwebtoken</groupId>
                            <artifactId>jjwt-impl</artifactId>
                            <version>0.12.6</version>
                            <scope>runtime</scope>
                        </dependency>
                        <dependency>
                            <groupId>io.jsonwebtoken</groupId>
                            <artifactId>jjwt-jackson</artifactId>
                            <version>0.12.6</version>
                            <scope>runtime</scope>
                        </dependency>""";

        FileUtils.insertBeforeLine(pomPath, "<!-- Test -->", securityDep);
        ConsoleOutput.updated("pom.xml (added Spring Security + JWT dependencies)");
    }
}
