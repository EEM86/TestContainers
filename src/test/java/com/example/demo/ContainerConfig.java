package com.example.demo;

import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

@TestConfiguration(proxyBeanMethods = false)
public class ContainerConfig {

  @Bean
  @RestartScope
  @ServiceConnection(name = "redis")
  public GenericContainer<?> redis() {
    return new GenericContainer<>("redis:6-alpine")
        .withExposedPorts(6379);
  }

  @Bean
  @RestartScope
  @ServiceConnection(name = "postgres")
  public PostgreSQLContainer<?> postgres() {
    return new PostgreSQLContainer<>("postgres:14-alpine")
        .withCopyFileToContainer(
            MountableFile.forClasspathResource("talks-schema.sql"),
            "/docker-entrypoint-initdb.d/init.sql"
        );
  }

  @Bean
  @RestartScope
  @ServiceConnection(name = "kafka")
  public KafkaContainer kafka() {
    return new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.2.1"));
  }
}
