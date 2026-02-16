package pragmatech.digital.workshops.lab3.exercises;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import pragmatech.digital.workshops.lab3.LocalDevTestcontainerConfig;
import pragmatech.digital.workshops.lab3.config.WireMockContextInitializer;
import pragmatech.digital.workshops.lab3.repository.BookRepository;

/**
 * Integration test using the default Spring Boot Test configuration.
 * This test loads the full application context.
 */
@SpringBootTest
@Import(LocalDevTestcontainerConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
class ApplicationOneIT {

  @Autowired
  private BookRepository bookRepository;

  @Test
  void contextLoads() {
    // Verify that the application context loads successfully
    System.out.println("ApplicationOneIT - Context loaded successfully");
  }
}
