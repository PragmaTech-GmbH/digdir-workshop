package pragmatech.digital.workshops.lab3.exercises;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import pragmatech.digital.workshops.lab3.LocalDevTestcontainerConfig;
import pragmatech.digital.workshops.lab3.config.WireMockContextInitializer;
import pragmatech.digital.workshops.lab3.repository.BookRepository;

/**
 * Integration test that uses custom test properties.
 * This creates a different application context.
 */
@SpringBootTest
@Import(LocalDevTestcontainerConfig.class)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
@TestPropertySource(properties = {
  "book.metadata.api.timeout=10"
})
class ApplicationFiveIT {

  @Autowired
  private BookRepository bookRepository;

  @Test
  void contextLoads() {
    // Verify that the application context loads successfully
    System.out.println("ApplicationFiveIT - Context loaded successfully");
  }
}
