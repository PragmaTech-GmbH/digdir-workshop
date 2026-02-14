package pragmatech.digital.workshops.lab6.experiment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pragmatech.digital.workshops.lab6.LocalDevTestcontainerConfig;
import pragmatech.digital.workshops.lab6.config.WireMockContextInitializer;
import pragmatech.digital.workshops.lab6.service.BookService;

/**
 * Context Cache Killer #3: Combines @ActiveProfiles, @TestPropertySource, and @MockitoBean
 *
 * This test creates a unique context because of the combination of:
 * - @ActiveProfiles("test") changes the active profile (context key includes profiles)
 * - @TestPropertySource adds custom properties (context key includes property sources)
 * - @MockitoBean BookService replaces a different bean than other tests
 *
 * Each of these differences contributes to a unique context cache key, guaranteeing
 * that this test cannot share a context with any other test class.
 */
@SpringBootTest
@Import(LocalDevTestcontainerConfig.class)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
@TestPropertySource(properties = {"spring.main.banner-mode=off", "custom.property=42"})
@ActiveProfiles("test")
class ContextCacheKillerThreeIT {

  @MockitoBean
  BookService bookService;

  @Test
  void contextLoads() {
    System.out.println("ContextCacheKillerThreeIT - Context loaded");
  }
}
