package dev.springforge.cli;

import dev.springforge.generator.ConfigManager;
import dev.springforge.model.ForgeConfig;
import dev.springforge.util.ConsoleOutput;
import dev.springforge.util.FileUtils;
import dev.springforge.util.NameUtils;
import picocli.CommandLine.Command;

import java.nio.file.Path;
import java.nio.file.Paths;

@Command(
    name = "cache",
    description = "Add Redis caching to the application"
)
public class AddCacheCommand implements Runnable {

    private final ConfigManager configManager = new ConfigManager();

    @Override
    public void run() {
        try {
            Path projectDir = Paths.get(System.getProperty("user.dir"));
            ForgeConfig config = configManager.loadOrFail(projectDir);

            ConsoleOutput.header("Adding Redis Cache");

            String packagePath = NameUtils.toPackagePath(config.getBasePackage());
            Path javaDir = projectDir.resolve("src/main/java").resolve(packagePath);

            // 1. Create CacheConfig.java
            Path configDir = javaDir.resolve("config");
            FileUtils.mkdirs(configDir);
            Path cacheConfigPath = configDir.resolve("CacheConfig.java");

            if (!FileUtils.exists(cacheConfigPath)) {
                String cacheConfig = String.format("""
                        package %s.config;
                        
                        import org.springframework.cache.annotation.EnableCaching;
                        import org.springframework.context.annotation.Configuration;
                        
                        @Configuration
                        @EnableCaching
                        public class CacheConfig {
                        }
                        """, config.getBasePackage());
                FileUtils.writeFile(cacheConfigPath, cacheConfig);
                ConsoleOutput.created("config/CacheConfig.java");
            }

            // 2. Add dependencies
            Path pomPath = projectDir.resolve("pom.xml");
            if (FileUtils.exists(pomPath)) {
                String redisDeps = """
                        
                                <!-- Redis Caching -->
                                <dependency>
                                    <groupId>org.springframework.boot</groupId>
                                    <artifactId>spring-boot-starter-data-redis</artifactId>
                                </dependency>
                                <dependency>
                                    <groupId>org.springframework.boot</groupId>
                                    <artifactId>spring-boot-starter-cache</artifactId>
                                </dependency>""";
                FileUtils.insertBeforeLine(pomPath, "</dependencies>", redisDeps);
                ConsoleOutput.updated("pom.xml (added spring-boot-starter-data-redis)");
            }

            // 3. Update docker-compose.yml
            Path dockerCompose = projectDir.resolve("docker-compose.yml");
            if (FileUtils.exists(dockerCompose)) {
                String redisDocker = """
                        
                          redis:
                            image: redis:7-alpine
                            ports:
                              - "6379:6379"
                        """;
                FileUtils.appendFile(dockerCompose, redisDocker);
                ConsoleOutput.updated("docker-compose.yml (added redis container)");
            }

            ConsoleOutput.done("Redis Cache successfully configured!");
            ConsoleOutput.info("Note: You can now use @Cacheable(\"yourCacheName\") on any Service method to cache its results.");

        } catch (Exception e) {
            ConsoleOutput.error("Failed to add cache: " + e.getMessage());

        }
    }
}
