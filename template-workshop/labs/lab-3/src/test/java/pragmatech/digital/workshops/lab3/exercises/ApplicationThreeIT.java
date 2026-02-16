package pragmatech.digital.workshops.lab3.exercises;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pragmatech.digital.workshops.lab3.LocalDevTestcontainerConfig;
import pragmatech.digital.workshops.lab3.config.WireMockContextInitializer;
import pragmatech.digital.workshops.lab3.repository.BookRepository;
import pragmatech.digital.workshops.lab3.service.BookService;

/**
 * Integration test that uses a different active profile.
 * This creates a different application context from the previous tests.
 */
@SpringBootTest
@Import(LocalDevTestcontainerConfig.class)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
@TestPropertySource(properties = {"spring.main.banner-mode=off", "custom.property=42"})
@ActiveProfiles("test")
class ApplicationThreeIT {

  @Autowired
  private BookRepository bookRepository;

  @MockitoBean
  private BookService bookService;

  @Test
  void contextLoads() {
    // Verify that the application context loads successfully
    System.out.println("ApplicationThreeIT - Context loaded successfully");
  }
}
