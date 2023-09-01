package com.example.demo.support;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class AbstractIntegrationTest {

  protected RequestSpecification requestSpecification;

  @LocalServerPort
  protected int localServerPort;

  static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine")
      .withCopyFileToContainer(
          MountableFile.forClasspathResource("/talks-schema.sql"),
          "/docker-entrypoint-initdb.d/init.sql"
          );

  static final GenericContainer<?> redis = new GenericContainer<>("redis:6-alpine")
      .withExposedPorts(6379);

  static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.2.1"));

  @DynamicPropertySource
  public static void configureKafka(DynamicPropertyRegistry registry) {
    kafka.start();
    registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
  }

  @DynamicPropertySource
  public static void configurePostgres(DynamicPropertyRegistry registry) {
    postgres.start();
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }


  @DynamicPropertySource
  public static void configureRedis(DynamicPropertyRegistry registry) {
    redis.start();
    registry.add("spring.data.redis.host", redis::getHost);
    registry.add("spring.data.redis.port", redis::getFirstMappedPort);
  }

  @BeforeEach
  public void setUpAbstractIntegrationTest() {
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    requestSpecification = new RequestSpecBuilder()
        .setPort(localServerPort)
        .addHeader(
            HttpHeaders.CONTENT_TYPE,
            MediaType.APPLICATION_JSON_VALUE
        )
        .build();
  }

}
