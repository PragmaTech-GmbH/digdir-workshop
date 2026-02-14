package pragmatech.digital.workshops.lab6.experiment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pragmatech.digital.workshops.lab6.LocalDevTestcontainerConfig;
import pragmatech.digital.workshops.lab6.client.OpenLibraryApiClient;
import pragmatech.digital.workshops.lab6.config.WireMockContextInitializer;

/**
 * Context Cache Killer #2: Uses @MockitoBean
 *
 * Adding a @MockitoBean changes the bean definition in the application context.
 * Since the context now contains a mock instead of the real OpenLibraryApiClient,
 * Spring cannot reuse a cached context that has the real bean - it must create
 * a new context with the mock replacement.
 */
@SpringBootTest
@Import(LocalDevTestcontainerConfig.class)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
class ContextCacheKillerTwoIT {

  @MockitoBean
  OpenLibraryApiClient openLibraryApiClient;

  @Test
  void contextLoads() {
    System.out.println("ContextCacheKillerTwoIT - Context loaded");
  }
}
