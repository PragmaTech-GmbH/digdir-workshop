package pragmatech.digital.workshops.lab6.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import pragmatech.digital.workshops.lab6.LocalDevTestcontainerConfig;

/**
 * Shared base class for all integration tests.
 *
 * By centralizing the test annotations in a single base class, all integration tests
 * that extend this class will share the same Spring application context. This avoids
 * the costly overhead of creating multiple contexts during the test suite.
 *
 * Context caching key factors unified here:
 * - @SpringBootTest: Loads the full application context
 * - @Import(LocalDevTestcontainerConfig.class): Provides the Testcontainers PostgreSQL instance
 * - @ContextConfiguration(initializers = WireMockContextInitializer.class): Provides WireMock stubs
 *
 * Rules for subclasses to maintain a single cached context:
 * 1. Do NOT add @DirtiesContext - it destroys and recreates the context
 * 2. Do NOT add @MockitoBean/@SpyBean - mock replacements change the context key
 * 3. Do NOT add @TestPropertySource with unique properties - it changes the context key
 * 4. Do NOT add @ActiveProfiles with a different profile - profiles are part of the key
 * 5. Use WireMock stubs (already configured) instead of mocking HTTP clients
 * 6. Use @Sql or direct repository calls for test data setup instead of mocking services
 */
@SpringBootTest
@Import(LocalDevTestcontainerConfig.class)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
public abstract class SharedIntegrationTestBase {
  // Common test infrastructure is provided through the annotations above.
  // Subclasses can @Autowired any bean from the shared application context.
}
