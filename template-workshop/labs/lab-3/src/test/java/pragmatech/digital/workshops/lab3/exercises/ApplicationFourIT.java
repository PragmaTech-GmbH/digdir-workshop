package pragmatech.digital.workshops.lab3.exercises;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import pragmatech.digital.workshops.lab3.LocalDevTestcontainerConfig;
import pragmatech.digital.workshops.lab3.client.OpenLibraryApiClient;
import pragmatech.digital.workshops.lab3.config.WireMockContextInitializer;
import pragmatech.digital.workshops.lab3.repository.BookRepository;

/**
 * Integration test that combines both a different profile and a @MockBean.
 * This creates yet another application context.
 */
@SpringBootTest
@Transactional
@Import(LocalDevTestcontainerConfig.class)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
@ActiveProfiles("test")
class ApplicationFourIT {

  @Autowired
  private BookRepository bookRepository;

  @MockitoBean
  private OpenLibraryApiClient openLibraryApiClient;

  @Test
  void contextLoads() {
    // Verify that the application context loads successfully
    System.out.println("ApplicationFourIT - Context loaded successfully");
  }
}
