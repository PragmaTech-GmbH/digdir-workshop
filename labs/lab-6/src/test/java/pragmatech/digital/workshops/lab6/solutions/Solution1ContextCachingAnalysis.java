package pragmatech.digital.workshops.lab6.solutions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Solution 1: Context Caching Analysis
 *
 * Findings: 5 tests create approximately 4-5 separate application contexts:
 *
 * - ContextCacheKillerOneIT: Creates its own context AND destroys it after each test method
 *   because of @DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD). This is the most
 *   expensive pattern since it forces a full context reload before every single test.
 *
 * - ContextCacheKillerTwoIT: Creates a new context because it adds
 *   @MockitoBean OpenLibraryApiClient. The mock bean replacement changes the context
 *   configuration, making it incompatible with the base context (no mocks).
 *
 * - ContextCacheKillerThreeIT: Creates a new context because of the combination of
 *   @ActiveProfiles("test"), @TestPropertySource with custom properties, and
 *   @MockitoBean BookService. Each of these annotations changes the context cache key.
 *
 * - ContextCacheKillerFourIT: Creates a new context because of @ActiveProfiles("test")
 *   combined with @MockitoBean OpenLibraryApiClient. Even though it also uses
 *   @ActiveProfiles("test") like ThreeIT, the different @MockitoBean target and absence
 *   of @TestPropertySource results in yet another unique cache key.
 *
 * - ContextCacheKillerFiveIT: Creates a new context because of
 *   @TestPropertySource(properties = {"book.metadata.api.timeout=10"}). The additional
 *   property source is different from the base configuration.
 *
 * Key insight: The Spring context cache key is composed of:
 *   - Configuration classes and locations
 *   - Active profiles
 *   - Property sources
 *   - Context initializers
 *   - MockitoBean/SpyBean definitions
 *
 * Any difference in these components results in a separate cached context.
 *
 * Proposed fix: Create a SharedIntegrationTestBase class with the common annotations
 * (@SpringBootTest, @Import, @ContextConfiguration) and have all tests extend it.
 * Remove @DirtiesContext, unnecessary @TestPropertySource, different @ActiveProfiles,
 * and unnecessary @MockitoBean annotations. Use WireMock stubs instead of mocks.
 */
class Solution1ContextCachingAnalysis {

  @Test
  void contextCachingAnalysis() {
    // This test represents the completed analysis
    // See the Javadoc above for detailed findings
    assertThat(true).isTrue();
  }
}
