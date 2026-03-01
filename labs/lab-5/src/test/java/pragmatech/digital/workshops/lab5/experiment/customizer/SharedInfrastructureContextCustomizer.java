package pragmatech.digital.workshops.lab5.experiment.customizer;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;
import org.testcontainers.postgresql.PostgreSQLContainer;
import pragmatech.digital.workshops.lab5.config.OpenLibraryApiStub;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class SharedInfrastructureContextCustomizer implements ContextCustomizer {

  // Static so they persist across multiple test class executions (reusing the context)
  private static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");
  private static final WireMockServer wireMockServer = new WireMockServer(options().dynamicPort());

  @Override
  public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
    // 1. Start Infrastructure
    postgres.start();
    wireMockServer.start();

    // 2. Inject Dynamic Properties (JDBC URL, WireMock Port)
    TestPropertyValues.of(
      "spring.datasource.url=" + postgres.getJdbcUrl(),
      "spring.datasource.username=" + postgres.getUsername(),
      "spring.datasource.password=" + postgres.getPassword(),
      "book.metadata.api.url=http://localhost:" + wireMockServer.port()
    ).applyTo(context.getEnvironment());

    // 3. Register Beans Programmatically
    ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();

    OpenLibraryApiStub openLibraryStub = new OpenLibraryApiStub(wireMockServer);

    openLibraryStub.stubForSuccessfulBookResponse("9780134757599");
    openLibraryStub.stubForSuccessfulBookResponse("9780201633610");
    openLibraryStub.stubForSuccessfulBookResponse("9780132350884");

    // Register WireMock so tests can @Autowired it to stub responses
    beanFactory.registerSingleton("wireMockServer", wireMockServer);
    beanFactory.registerSingleton("openLibraryStub", openLibraryStub);

    // Register a Fixed Clock for deterministic time-based testing
    Clock fixedClock = Clock.fixed(Instant.parse("2026-03-01T10:00:00Z"), ZoneId.of("UTC"));
    beanFactory.registerSingleton("clock", fixedClock);
  }

  // Required for context caching to work correctly
  @Override
  public boolean equals(Object obj) {
    return obj != null && obj.getClass() == this.getClass();
  }

  @Override
  public int hashCode() {
    return SharedInfrastructureContextCustomizer.class.hashCode();
  }
}
