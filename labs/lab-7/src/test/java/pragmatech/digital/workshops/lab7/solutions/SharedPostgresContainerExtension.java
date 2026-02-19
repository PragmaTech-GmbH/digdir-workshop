package pragmatech.digital.workshops.lab7.solutions;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * A reusable JUnit 5 extension that manages a singleton PostgreSQL Testcontainer.
 *
 * <p>This extension demonstrates the <strong>JUnit 5 extension approach</strong> to Testcontainers,
 * as an alternative to Spring Boot's {@code @ServiceConnection} + {@code @TestConfiguration}.
 *
 * <p><strong>Key design decisions:</strong>
 * <ul>
 *   <li><strong>Static container field</strong> — the container is created once per JVM, shared
 *       across all test classes that use this extension. The static initializer block starts it.</li>
 *   <li><strong>System.setProperty</strong> — overrides Spring's datasource properties before
 *       the {@code @SpringBootTest} context initializes. Since {@code BeforeAllCallback} runs
 *       before context startup, this is safe.</li>
 *   <li><strong>Shutdown hook</strong> — ensures the container stops when the JVM exits, whether
 *       tests pass or fail.</li>
 * </ul>
 *
 * <p><strong>Usage:</strong>
 * <pre>
 * {@literal @}ExtendWith(SharedPostgresContainerExtension.class)
 * {@literal @}SpringBootTest
 * class MyIntegrationTest {
 *   // No @Import(LocalDevTestcontainerConfig.class) needed!
 * }
 * </pre>
 *
 * <p><strong>Comparison with Spring Boot's approach:</strong>
 * <pre>
 * Spring Boot way:                          JUnit extension way:
 * @TestConfiguration                        @ExtendWith(SharedPostgresContainerExtension.class)
 * class TestcontainersConfig {
 *   @Bean @ServiceConnection
 *   static PostgreSQLContainer postgres() {
 *     return new PostgreSQLContainer(...);  static container in extension class
 *   }
 * }
 * </pre>
 *
 * <p>The Spring Boot way integrates tightly with context lifecycle and {@code @ServiceConnection}.
 * The extension way works at a lower level and is framework-agnostic.
 */
public class SharedPostgresContainerExtension implements BeforeAllCallback {

  private static final Logger log = LoggerFactory.getLogger(SharedPostgresContainerExtension.class);

  private static final PostgreSQLContainer<?> POSTGRES;

  static {
    POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("testdb")
      .withUsername("test")
      .withPassword("test");

    POSTGRES.start();

    log.info("Shared PostgreSQL container started at: {}", POSTGRES.getJdbcUrl());

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      log.info("Stopping shared PostgreSQL container");
      POSTGRES.stop();
    }));
  }

  /**
   * Called by JUnit before the first test in the annotated class runs.
   *
   * <p>Sets system properties so Spring's {@code @SpringBootTest} will pick up
   * the container's JDBC URL when it initializes the application context.
   *
   * <p>Because system properties take higher priority than {@code application.yml},
   * this effectively overrides the datasource configuration for the test.
   */
  @Override
  public void beforeAll(ExtensionContext context) {
    System.setProperty("spring.datasource.url", POSTGRES.getJdbcUrl());
    System.setProperty("spring.datasource.username", POSTGRES.getUsername());
    System.setProperty("spring.datasource.password", POSTGRES.getPassword());

    log.debug("Datasource system properties set for context: {}",
      context.getDisplayName());
  }

  public static String getJdbcUrl() {
    return POSTGRES.getJdbcUrl();
  }

  public static String getUsername() {
    return POSTGRES.getUsername();
  }

  public static String getPassword() {
    return POSTGRES.getPassword();
  }
}
