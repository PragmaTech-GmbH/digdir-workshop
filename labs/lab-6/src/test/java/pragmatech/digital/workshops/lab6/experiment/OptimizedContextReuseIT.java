package pragmatech.digital.workshops.lab6.experiment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pragmatech.digital.workshops.lab6.config.SharedIntegrationTestBase;
import pragmatech.digital.workshops.lab6.repository.BookRepository;
import pragmatech.digital.workshops.lab6.service.BookService;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Demonstrates the ideal pattern: consistent annotations via a shared base class.
 *
 * This test extends SharedIntegrationTestBase and adds NO extra annotations that
 * would change the context cache key. As a result, it shares the same application
 * context as every other test that extends SharedIntegrationTestBase.
 *
 * Key observations:
 * - No @DirtiesContext: The context is preserved between tests
 * - No @MockitoBean: Real beans are used (WireMock handles external API simulation)
 * - No @TestPropertySource: Default test properties are sufficient
 * - No @ActiveProfiles: The default profile is used consistently
 *
 * Compare the execution time of this test class with DirtiesContextDemoIT
 * to see the performance difference.
 */
class OptimizedContextReuseIT extends SharedIntegrationTestBase {

  @Autowired
  private BookRepository bookRepository;

  @Autowired
  private BookService bookService;

  @Test
  void shouldReuseExistingContext() {
    assertThat(bookRepository).isNotNull();
    System.out.println("OptimizedContextReuseIT - shouldReuseExistingContext: context reused");
  }

  @Test
  void shouldHaveAccessToAllBeans() {
    assertThat(bookService).isNotNull();
    System.out.println("OptimizedContextReuseIT - shouldHaveAccessToAllBeans: context reused");
  }
}
