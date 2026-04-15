package dev.springforge.cli;

import dev.springforge.generator.ConfigManager;
import dev.springforge.model.ForgeConfig;
import dev.springforge.util.ConsoleOutput;
import dev.springforge.util.FileUtils;
import dev.springforge.util.NameUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.nio.file.Paths;

@Command(
    name = "broker",
    description = "Add an event-driven message broker (Kafka/RabbitMQ) to the application"
)
public class AddBrokerCommand implements Runnable {

    @Parameters(index = "0", description = "Broker type (kafka or rabbitmq)", defaultValue = "kafka")
    private String brokerType;

    private final ConfigManager configManager = new ConfigManager();

    @Override
    public void run() {
        try {
            Path projectDir = Paths.get(System.getProperty("user.dir"));
            ForgeConfig config = configManager.loadOrFail(projectDir);

            ConsoleOutput.header("Adding Event Broker: " + brokerType.toUpperCase());

            if (!brokerType.equalsIgnoreCase("kafka")) {
                ConsoleOutput.error("Currently only 'kafka' is supported via CLI generation.");
                return;
            }

            String packagePath = NameUtils.toPackagePath(config.getBasePackage());
            Path javaDir = projectDir.resolve("src/main/java").resolve(packagePath);

            // 1. Create Messaging utilities
            Path msgDir = javaDir.resolve("messaging");
            FileUtils.mkdirs(msgDir);

            Path producerPath = msgDir.resolve("KafkaProducer.java");
            if (!FileUtils.exists(producerPath)) {
                String producer = String.format("""
                        package %s.messaging;
                        
                        import org.springframework.kafka.core.KafkaTemplate;
                        import org.springframework.stereotype.Service;
                        
                        @Service
                        public class KafkaProducer {
                        
                            private final KafkaTemplate<String, String> kafkaTemplate;
                            
                            public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
                                this.kafkaTemplate = kafkaTemplate;
                            }
                            
                            public void sendMessage(String topic, String message) {
                                kafkaTemplate.send(topic, message);
                            }
                        }
                        """, config.getBasePackage());
                FileUtils.writeFile(producerPath, producer);
                ConsoleOutput.created("messaging/KafkaProducer.java");
            }

            Path consumerPath = msgDir.resolve("KafkaConsumer.java");
            if (!FileUtils.exists(consumerPath)) {
                String consumer = String.format("""
                        package %s.messaging;
                        
                        import org.springframework.kafka.annotation.KafkaListener;
                        import org.springframework.stereotype.Component;
                        
                        @Component
                        public class KafkaConsumer {
                        
                            @KafkaListener(topics = "default-topic", groupId = "group_id")
                            public void consume(String message) {
                                System.out.println("Consumed message: " + message);
                            }
                        }
                        """, config.getBasePackage());
                FileUtils.writeFile(consumerPath, consumer);
                ConsoleOutput.created("messaging/KafkaConsumer.java");
            }

            // 2. Add dependencies
            Path pomPath = projectDir.resolve("pom.xml");
            if (FileUtils.exists(pomPath)) {
                String kafkaDeps = """
                        
                                <!-- Apache Kafka -->
                                <dependency>
                                    <groupId>org.springframework.kafka</groupId>
                                    <artifactId>spring-kafka</artifactId>
                                </dependency>""";
                FileUtils.insertBeforeLine(pomPath, "</dependencies>", kafkaDeps);
                ConsoleOutput.updated("pom.xml (added spring-kafka)");
            }

            // 3. Update docker-compose.yml
            Path dockerCompose = projectDir.resolve("docker-compose.yml");
            if (FileUtils.exists(dockerCompose)) {
                String kafkaDocker = """
                        
                          zookeeper:
                            image: confluentinc/cp-zookeeper:latest
                            environment:
                              ZOOKEEPER_CLIENT_PORT: 2181
                              ZOOKEEPER_TICK_TIME: 2000
                            ports:
                              - 22181:2181
                          
                          kafka:
                            image: confluentinc/cp-kafka:latest
                            depends_on:
                              - zookeeper
                            ports:
                              - 29092:29092
                            environment:
                              KAFKA_BROKER_ID: 1
                              KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
                              KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092,PLAINTEXT_HOST://localhost:29092
                              KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
                              KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
                              KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
                        """;
                FileUtils.appendFile(dockerCompose, kafkaDocker);
                ConsoleOutput.updated("docker-compose.yml (added kafka + zookeeper containers)");
            }

            ConsoleOutput.done("Kafka Broker successfully configured!");

        } catch (Exception e) {
            ConsoleOutput.error("Failed to add broker: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
