package pragmatech.digital.workshops.lab7.exercises;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import pragmatech.digital.workshops.lab7.LocalDevTestcontainerConfig;
import pragmatech.digital.workshops.lab7.config.WireMockContextInitializer;
import pragmatech.digital.workshops.lab7.repository.BookRepository;

/**
 * Exercise 3: Write a Reusable JUnit 5 Testcontainers Extension
 *
 * <p>The Spring Boot way of managing Testcontainers via {@code @ServiceConnection} works great,
 * but relies on the Spring context being loaded. A JUnit 5 extension is a lower-level approach
 * that manages container lifecycle independently of Spring.
 *
 * <p>Tasks:
 * <ol>
 *   <li>Create a class {@code SharedPostgresContainerExtension} in the {@code solutions} package
 *       that implements {@code BeforeAllCallback} from JUnit 5</li>
 *   <li>The class should hold a {@code static} {@code PostgreSQLContainer} field so the
 *       container is shared across all test classes in the JVM (singleton pattern)</li>
 *   <li>In {@code beforeAll}, set the following system properties so Spring can connect:
 *     <pre>
 *     System.setProperty("spring.datasource.url", container.getJdbcUrl());
 *     System.setProperty("spring.datasource.username", container.getUsername());
 *     System.setProperty("spring.datasource.password", container.getPassword());
 *     </pre>
 *   </li>
 *   <li>Register a JVM shutdown hook to stop the container when tests are done</li>
 *   <li>Annotate this test class with {@code @ExtendWith(SharedPostgresContainerExtension.class)}
 *       and remove the {@code @Import(LocalDevTestcontainerConfig.class)} that normally brings
 *       in the Spring-managed container</li>
 *   <li>Run the test and verify it passes</li>
 * </ol>
 *
 * <p>Hints:
 * <ul>
 *   <li>The static initializer block runs when the class is loaded — perfect for starting the container</li>
 *   <li>{@code BeforeAllCallback} runs before {@code @SpringBootTest} starts the context</li>
 *   <li>System properties set in {@code beforeAll} are visible to Spring at context startup time</li>
 *   <li>Compare this to the {@code LocalDevTestcontainerConfig} approach: both solve the same
 *       problem, but the extension works without a {@code @TestConfiguration} Spring bean</li>
 * </ul>
 *
 * <p>Solution: {@code solutions/SharedPostgresContainerExtension.java} and
 * {@code solutions/Solution3ReusableExtensionTest.java}
 */
// TODO: Add @ExtendWith(SharedPostgresContainerExtension.class) here
@SpringBootTest
@ContextConfiguration(initializers = WireMockContextInitializer.class)
// TODO: Remove the @Import below once you wire the extension — the extension replaces it
@Import(LocalDevTestcontainerConfig.class)
class Exercise3ReusableExtensionTest {

  @Autowired
  private BookRepository bookRepository;

  @Test
  void shouldRunWithSharedContainerExtension() {
    // TODO: Verify the repository works (container is up and schema is applied)
    // Hint: bookRepository.count() should return a non-negative number
    throw new UnsupportedOperationException("Implement this exercise");
  }

  @Test
  void shouldShareContainerAcrossTestMethods() {
    // TODO: Insert a book and verify it was saved
    // (show the container is shared and functional)
    throw new UnsupportedOperationException("Implement this exercise");
  }
}
