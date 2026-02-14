package pragmatech.digital.workshops.lab6.experiment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import pragmatech.digital.workshops.lab6.LocalDevTestcontainerConfig;
import pragmatech.digital.workshops.lab6.config.WireMockContextInitializer;

/**
 * Context Cache Killer #1: Uses @DirtiesContext
 *
 * The @DirtiesContext annotation tells Spring to close and recreate the application
 * context before each test method. This completely defeats context caching and adds
 * a significant time penalty (~5-10 seconds per context reload).
 */
@SpringBootTest
@Import(LocalDevTestcontainerConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
class ContextCacheKillerOneIT {

  @Test
  void contextLoads() {
    System.out.println("ContextCacheKillerOneIT - Context loaded");
  }
}
