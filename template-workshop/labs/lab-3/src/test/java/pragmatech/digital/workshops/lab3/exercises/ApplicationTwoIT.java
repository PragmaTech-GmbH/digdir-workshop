package pragmatech.digital.workshops.lab3.exercises;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pragmatech.digital.workshops.lab3.LocalDevTestcontainerConfig;
import pragmatech.digital.workshops.lab3.client.OpenLibraryApiClient;
import pragmatech.digital.workshops.lab3.config.WireMockContextInitializer;
import pragmatech.digital.workshops.lab3.repository.BookRepository;

/**
 * Integration test that uses @MockBean to mock the OpenLibraryApiClient.
 * This creates a different application context from ApplicationOneIT.
 */
@SpringBootTest
@Import(LocalDevTestcontainerConfig.class)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
class ApplicationTwoIT {

  @Autowired
  private BookRepository bookRepository;

  @MockitoBean
  private OpenLibraryApiClient openLibraryApiClient;

  @Test
  void contextLoads() {
    // Verify that the application context loads successfully
    System.out.println("ApplicationTwoIT - Context loaded successfully");
  }
}
