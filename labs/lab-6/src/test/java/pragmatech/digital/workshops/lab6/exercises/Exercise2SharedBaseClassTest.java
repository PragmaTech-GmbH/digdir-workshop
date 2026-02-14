package pragmatech.digital.workshops.lab6.exercises;

import org.junit.jupiter.api.Test;

/**
 * Exercise 2: Create a Shared Integration Test Base Class
 *
 * After analyzing the context caching issues in Exercise 1, it's time to fix them.
 *
 * Tasks:
 * 1. Create a SharedIntegrationTestBase class in the config package with these annotations:
 *    - @SpringBootTest
 *    - @Import(LocalDevTestcontainerConfig.class)
 *    - @ContextConfiguration(initializers = WireMockContextInitializer.class)
 *    Make the class abstract and public so all test classes can extend it.
 *
 * 2. Refactor the ContextCacheKiller*IT tests to extend SharedIntegrationTestBase:
 *    - Remove duplicate @SpringBootTest, @Import, @ContextConfiguration from each test
 *    - Remove @DirtiesContext (it defeats caching entirely)
 *    - Remove unnecessary @TestPropertySource (use application-test.properties instead)
 *    - Remove unnecessary @ActiveProfiles (keep configuration consistent)
 *    - Remove @MockitoBean annotations (use WireMock stubs from the shared context)
 *
 * 3. Run all tests and verify only ONE application context is created
 *    (look for a single "Initializing Spring" log message)
 *
 * 4. Compare build times before and after the optimization
 *
 * Expected result: All integration tests share a single application context,
 * reducing total test execution time significantly.
 */
class Exercise2SharedBaseClassTest {

  @Test
  void shouldShareSingleContextAcrossAllTests() {
    // TODO: After creating SharedIntegrationTestBase, make this test class
    // extend it and verify the context loads successfully.
    //
    // Example:
    //   class Exercise2SharedBaseClassTest extends SharedIntegrationTestBase {
    //     @Autowired
    //     private BookRepository bookRepository;
    //
    //     @Test
    //     void shouldShareSingleContextAcrossAllTests() {
    //       assertThat(bookRepository).isNotNull();
    //     }
    //   }
  }
}
