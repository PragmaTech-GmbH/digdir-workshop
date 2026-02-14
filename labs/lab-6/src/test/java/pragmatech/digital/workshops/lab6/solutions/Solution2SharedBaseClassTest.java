package pragmatech.digital.workshops.lab6.solutions;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pragmatech.digital.workshops.lab6.config.SharedIntegrationTestBase;
import pragmatech.digital.workshops.lab6.repository.BookRepository;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Solution 2: Shared Integration Test Base Class
 *
 * This test extends SharedIntegrationTestBase, which provides a single, shared
 * application context for all integration tests. No additional annotations are
 * needed - the base class handles everything.
 *
 * Benefits of this approach:
 * - All integration tests share ONE application context (created once, reused everywhere)
 * - Testcontainers PostgreSQL is started once and shared across all tests
 * - WireMock server is started once and shared across all tests
 * - Total test suite execution time is significantly reduced
 *
 * If a test needs specific WireMock behavior, it can @Autowired the WireMockServer
 * and configure additional stubs within the test method (just remember to reset them
 * in @AfterEach to avoid test pollution).
 */
class Solution2SharedBaseClassTest extends SharedIntegrationTestBase {

  @Autowired
  private BookRepository bookRepository;

  @Test
  void shouldShareSingleContextAcrossAllTests() {
    assertThat(bookRepository).isNotNull();
  }
}
