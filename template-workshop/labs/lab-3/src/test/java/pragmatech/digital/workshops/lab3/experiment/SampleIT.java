package pragmatech.digital.workshops.lab3.experiment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pragmatech.digital.workshops.lab3.config.WireMockContextInitializer;

@Testcontainers
@SpringBootTest
@ContextConfiguration(initializers = WireMockContextInitializer.class)
class SampleIT {

  @Container
  @ServiceConnection
  static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16-alpine")
    .withDatabaseName("testdb")
    .withUsername("test")
    .withPassword("test");
  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void testSample() {
  }
}
