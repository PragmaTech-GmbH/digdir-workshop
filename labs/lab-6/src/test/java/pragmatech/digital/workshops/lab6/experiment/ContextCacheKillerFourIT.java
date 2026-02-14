package pragmatech.digital.workshops.lab6.experiment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import pragmatech.digital.workshops.lab6.LocalDevTestcontainerConfig;
import pragmatech.digital.workshops.lab6.client.OpenLibraryApiClient;
import pragmatech.digital.workshops.lab6.config.WireMockContextInitializer;

/**
 * Context Cache Killer #4: Combines @Transactional, @ActiveProfiles, and @MockitoBean
 *
 * This test creates a unique context because of the combination of:
 * - @ActiveProfiles("test") changes the active profile
 * - @MockitoBean OpenLibraryApiClient replaces a bean with a mock
 *
 * Note: @Transactional itself does NOT affect context caching - it only controls
 * transaction rollback behavior. However, the combination of @ActiveProfiles("test")
 * with @MockitoBean OpenLibraryApiClient is different from other test classes,
 * producing yet another unique context cache key.
 */
@SpringBootTest
@Transactional
@Import(LocalDevTestcontainerConfig.class)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
@ActiveProfiles("test")
class ContextCacheKillerFourIT {

  @MockitoBean
  OpenLibraryApiClient openLibraryApiClient;

  @Test
  void contextLoads() {
    System.out.println("ContextCacheKillerFourIT - Context loaded");
  }
}
