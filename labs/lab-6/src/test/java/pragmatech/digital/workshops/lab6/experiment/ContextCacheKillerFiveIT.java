package pragmatech.digital.workshops.lab6.experiment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import pragmatech.digital.workshops.lab6.LocalDevTestcontainerConfig;
import pragmatech.digital.workshops.lab6.config.WireMockContextInitializer;

/**
 * Context Cache Killer #5: Uses @TestPropertySource with a unique property
 *
 * Adding @TestPropertySource with different properties creates a different
 * context cache key. Even a single extra property like "book.metadata.api.timeout=10"
 * is enough to prevent context reuse with tests that do not have this property.
 */
@SpringBootTest
@Import(LocalDevTestcontainerConfig.class)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
@TestPropertySource(properties = {"book.metadata.api.timeout=10"})
class ContextCacheKillerFiveIT {

  @Test
  void contextLoads() {
    System.out.println("ContextCacheKillerFiveIT - Context loaded");
  }
}
