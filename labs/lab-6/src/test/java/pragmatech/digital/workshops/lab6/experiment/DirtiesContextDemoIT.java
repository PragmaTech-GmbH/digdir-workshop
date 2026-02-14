package pragmatech.digital.workshops.lab6.experiment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import pragmatech.digital.workshops.lab6.LocalDevTestcontainerConfig;
import pragmatech.digital.workshops.lab6.config.WireMockContextInitializer;

/**
 * Demonstrates the performance impact of @DirtiesContext.
 *
 * With @DirtiesContext(classMode = AFTER_EACH_TEST_METHOD), the Spring application
 * context is destroyed and recreated after every single test method. For a typical
 * Spring Boot application, each context reload takes approximately 5-10 seconds.
 *
 * This class has two test methods, so it will trigger TWO context reloads on top
 * of the initial context creation - adding roughly 10-20 seconds of overhead just
 * for this one test class.
 *
 * When to (rarely) use @DirtiesContext:
 * - A test modifies a singleton bean's internal state in a way that cannot be reset
 * - A test changes Spring configuration programmatically
 *
 * Better alternatives:
 * - Use @Transactional for database state isolation (auto-rollback after each test)
 * - Use @Sql to set up and tear down test data
 * - Reset WireMock stubs in @AfterEach instead of reloading the entire context
 * - Design beans to be stateless or resettable
 */
@SpringBootTest
@Import(LocalDevTestcontainerConfig.class)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DirtiesContextDemoIT {

  @Test
  void firstTest() {
    System.out.println("DirtiesContextDemoIT - firstTest executed");
    // After this test, the context will be destroyed and recreated
  }

  @Test
  void secondTest() {
    System.out.println("DirtiesContextDemoIT - secondTest executed");
    // After this test, the context will be destroyed again
  }
}
