---
marp: true
theme: pragmatech
---

![bg](./assets/digdir-cover.jpg)

---

<!-- _class: title -->
![bg h:500 left:33%](assets/generated/demystify.png)

# Testing Spring Boot Applications Demystified

## Lab 8

_Digdir Workshop 03.03.2026_

Philip Riecks - [PragmaTech GmbH](https://pragmatech.digital/) - [@rieckpil](https://x.com/rieckpil)

---

<!-- header: 'Testing Spring Boot Applications Demystified Workshop @ Digdir 03.03.2026' -->
<!-- footer: '![w:32 h:32](assets/generated/logo.webp)' -->

## Discuss Exercises from Lab 7

- Exercises:
  - `Exercise1ParallelExecutionTest`
  - `Exercise2TestIsolationTest`

---

![bg left:33%](assets/generated/lab-4.jpg)

# Lab 8

## General Spring Boot Testing Tips & Tricks and Q&A

### Various Spring Boot Testing Hacks

---

## `OutputCaptureExtension`

Capture `System.out`, `System.err`, and log output **without** starting a Spring context.

```java
@ExtendWith(OutputCaptureExtension.class)
class OutputCaptureTest {

  private static final Logger log = LoggerFactory.getLogger(OutputCaptureTest.class);

  @Test
  void shouldCaptureStdOutWhenPrintingToSystemOut(CapturedOutput output) {
    System.out.println("hello");

    assertThat(output.getOut()).contains("hello");
  }

  @Test
  void shouldCaptureLogOutputWhenLoggingWithSlf4j(CapturedOutput output) {
    log.info("Book created");

    assertThat(output.getAll()).contains("Book created");
  }
}
```
  
---

## Mutation Testing with PIT

- Having high code coverage might give you a **false sense of security**
- Mutation Testing with [PIT](https://pitest.org/quickstart/)
- Beyond Line Coverage: Traditional tools like JaCoCo show which code runs during tests, but PIT verifies if our tests actually detect when code behaves incorrectly by introducing "**mutations**" to our source code.
- Quality Guarantee: PIT automatically **modifies our code** (changing conditionals, return values, etc.) to ensure our tests fail when they should, **revealing blind spots** in seemingly comprehensive test suites.

---

![center h:500 w:1300](assets/mutation-testing-explained.png)

---

# Mutation Testing with PIT

**The problem:** 100% line coverage ≠ good tests. Weak assertions can still pass.

```java
@Test
void shouldReturnFee() {
  BigDecimal fee = cut.calculateFee(borrowedBook, borrowedDate);
  assertThat(fee).isNotNull(); // ← This assertion is meaningless!
}
```

**PIT solution:** Automatically mutates your production code and checks whether your tests catch it.

```bash
cd labs/lab-8
./mvnw pitest:mutationCoverage
# Opens target/pit-reports/index.html
```

> **A mutation is "killed"** when at least one test fails because of the mutation.
> **A surviving mutant** exposes a gap in your test suite.

---

## PIT in Action — `LateReturnFeeCalculator`

```java
// Production code with multiple fee tiers
public BigDecimal calculateFee(Book book, LocalDate borrowedDate) {
  if (book.getStatus() != BookStatus.BORROWED) return BigDecimal.ZERO;

  long daysOverdue = ChronoUnit.DAYS.between(borrowedDate, LocalDate.now(clock));

  if (daysOverdue <= 0)       return BigDecimal.ZERO;
  else if (daysOverdue <= 7)  return RATE_TIER_ONE.multiply(BigDecimal.valueOf(daysOverdue));
  else if (daysOverdue <= 14) return RATE_TIER_TWO.multiply(BigDecimal.valueOf(daysOverdue));
  else                        return RATE_TIER_THREE.multiply(BigDecimal.valueOf(daysOverdue));
}
```

**PIT mutates conditionals** (`<= 7` → `< 7`, `<= 14` → `< 14`) — boundary tests are essential.

**Pro tip:** Use `Clock` injection instead of `LocalDate.now()` for deterministic tests.

---

## Following Container Logs

Pipe Testcontainers output to your SLF4J logger for debugging:

```java
@Testcontainers
class ContainerLogsTest {

  private static final Logger log = LoggerFactory.getLogger(ContainerLogsTest.class);

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
    .withLogConsumer(new Slf4jLogConsumer(log)); // ← All container output → SLF4J

  @Test
  void shouldCapturePostgresStartupLogsWhenContainerStarts() {
    assertThat(postgres.getLogs())
      .contains("database system is ready to accept connections");
  }
}
```

---

**Add to `LocalDevTestcontainerConfig`** to always stream container logs during test runs:

```java
return new PostgreSQLContainer<>("postgres:16-alpine")
  .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("postgres")));
```

---

## `@RecordApplicationEvents`

Verify that your application code publishes the **right Spring events** - without mocking.

```java
@SpringBootTest
@RecordApplicationEvents                  // ← Enables event recording
@Import(LocalDevTestcontainerConfig.class)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
class RecordApplicationEventsTest {

  @Autowired ApplicationEvents events;    // ← Injected per test
  @Autowired BookService bookService;

  @Test
  void shouldPublishBookCreatedEventWhenCreatingBook() {
    bookService.createBook(new BookCreationRequest("978-0134757599", "Effective Java",
        "Joshua Bloch", LocalDate.of(2018, 1, 6)));

    assertThat(events.stream(BookCreatedEvent.class)).hasSize(1);

    BookCreatedEvent event = events.stream(BookCreatedEvent.class).findFirst().orElseThrow();
    assertThat(event.isbn()).isEqualTo("978-0134757599");
    assertThat(event.bookId()).isNotNull();
  }
}
```

---

## `ApplicationContextRunner`

Test **auto-configuration and conditional beans** without starting a full Spring Boot context.

```java
class ApplicationContextRunnerTest {

  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
    .withUserConfiguration(ConditionalBookImportConfig.class); // ← Minimal slice

  @Test
  void shouldNotHaveBookImportBeanWhenPropertyIsAbsent() {
    contextRunner.run(context ->
      assertThat(context).doesNotHaveBean("bookImportEnabled")
    );
  }

  @Test
  void shouldHaveBookImportBeanWhenPropertyIsEnabled() {
    contextRunner
      .withPropertyValues("bookshelf.import.enabled=true")
      .run(context ->
        assertThat(context).hasSingleBean(String.class)
      );
  }
}
```

**Runs in milliseconds** — ideal for testing `@ConditionalOnProperty`, `@ConditionalOnClass`, `@ConditionalOnMissingBean`.

---

## ArchUnit - Architecture Testing

**What is it?** A Java library that lets you write **executable architecture rules** as unit tests.

**Why does it matter?**

| Without ArchUnit | With ArchUnit |
|---|---|
| Architecture documented in ADRs/wikis | Architecture enforced in CI |
| Violations discovered in code review | Violations fail the build immediately |
| "Soft" conventions | Hard rules with clear error messages |

**No Spring context required** - ArchUnit analyzes the compiled bytecode.

---

## Adding ArchUnit to Our Project

```xml
<dependency>
  <groupId>com.tngtech.archunit</groupId>
  <artifactId>archunit-junit5</artifactId>
  <version>1.3.0</version>
  <scope>test</scope>
</dependency>
```

---

## ArchUnit — Code Examples

```java
@AnalyzeClasses(
  packages = "pragmatech.digital.workshops.lab8",
  importOptions = ImportOption.DoNotIncludeTests.class
)
class ArchUnitTest {

  @ArchTest
  static final ArchRule layeredArchitectureRuleShouldBeRespected = layeredArchitecture()
    .consideringAllDependencies()
    .layer("Controller").definedBy("..controller..")
    .layer("Service").definedBy("..service..")
    .layer("Repository").definedBy("..repository..")
    .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
    .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller")
    .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service");

  @ArchTest
  static final ArchRule classesShouldNotCallLocalDateNowDirectly = noClasses()
    .that().resideOutsideOfPackage("..service..")
    .and().resideInAPackage("pragmatech.digital.workshops.lab8..")
    .should().callMethod(LocalDate.class, "now");
}
```

---

## Object Mother Pattern - Centralise Test Data Creation

**Problem:** every test constructs its own objects → brittle, verbose, inconsistent.

```java
// ❌ Repeated in every test — breaks when the Book constructor changes
Book book = new Book("978-0-13-468599-1", "Clean Code", "Martin", LocalDate.of(2008, 8, 1));
book.setStatus(BookStatus.BORROWED);
```

---

**Solution:** a dedicated factory class with named, pre-configured instances:

```java
public class BookMother {

  public static Book availableBook() {
    return new Book("978-0-13-468599-1", "Clean Code", "Robert C. Martin", LocalDate.of(2008, 8, 1));
  }

  public static Book borrowedBook() {
    Book book = availableBook();
    book.setStatus(BookStatus.BORROWED);
    return book;
  }

  public static Book effectiveJava() {
    return new Book("978-0-13-468599-0", "Effective Java", "Joshua Bloch", LocalDate.of(2018, 1, 6));
  }
}
```

```java
// ✅ Tests read like a specification
Book book = BookMother.borrowedBook();
```

**Constructor changes?** Fix `BookMother` once - all tests stay green.

---

## Useful Libraries: Selenide

**Selenium wrapper** with a fluent API that reduces boilerplate and auto-retries assertions.

```java
// Selenium (verbose)
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
WebElement button = wait.until(ExpectedConditions.elementToBeClickable(By.id("submit")));
button.click();

// Selenide (concise)
$("#submit").click();
$$(".book-card").shouldHave(size(5));
$(".book-title").shouldHave(text("Effective Java"));
```

---

**Key features:**
- Auto-waits for elements (configurable timeout, no explicit waits)
- Screenshots + page source on test failure — saved automatically
- Works with Selenium Grid, BrowserStack, and remote browsers

```xml
<dependency>
  <groupId>com.codeborne</groupId>
  <artifactId>selenide</artifactId>
  <version>7.x</version>
</dependency>
```

---

## Useful Libraries: GreenMail / Mailpit

**Test email sending** without a real SMTP server.

```java
@SpringBootTest(webEnvironment = NONE)
class BookNotificationServiceTest {

  @RegisterExtension
  static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
    .withConfiguration(GreenMailConfiguration.aConfig().withUser("test", "test"))
    .withPerMethodLifecycle(false);

  @Autowired BookNotificationService bookNotificationService;

  @Test
  void shouldSendEmailWhenNotifyingAboutNewBook() throws Exception {
    bookNotificationService.notifyNewBook("Effective Java", "reader@example.com");

    MimeMessage[] messages = greenMail.getReceivedMessages();
    assertThat(messages).hasSize(1);
    assertThat(messages[0].getSubject()).isEqualTo("New book available: Effective Java");
  }
}
```

---

## Useful Libraries: Gatling / JMH

### Gatling — Load & Performance Testing

```scala
class BookApiSimulation extends Simulation {
  val scn = scenario("Browse Books")
    .exec(http("Get All Books").get("/api/books"))
    .pause(1)
    .exec(http("Get Book by ID").get("/api/books/1"))

  setUp(scn.inject(atOnceUsers(100))).protocols(httpProtocol)
}
```

```bash
mvn gatling:test    # Generates HTML report in target/gatling/
```

---

### JMH — Micro-benchmarking

```java
@Benchmark
@BenchmarkMode(Mode.AverageTime)
public void calculateFee(Blackhole bh) {
  bh.consume(cut.calculateFee(book, borrowedDate));
}
```

**Use Gatling** for end-to-end API performance. **Use JMH** for hot code paths and algorithm comparisons.

---

## Useful Libraries: Pact / Spring Cloud Contract

**Consumer-Driven Contract Testing** - verify both sides of an API independently.

```text
Consumer (Frontend/Client)          Provider (Backend)
         │                                  │
         ▼                                  ▼
  Write Pact contract            Verify contract against
  (defines expected API)         real implementation
         │                                  │
         └──── Pact Broker / shared file ───┘
```

**Spring Cloud Contract** (for Spring-to-Spring):

```groovy
Contract.make {
  request { method 'GET'; url '/api/books/1' }
  response { status 200; body(id: 1, title: "Effective Java") }
}
```
---

## TDD with AI: CLAUDE.md Conventions

Define your testing conventions in `.claude/CLAUDE.md` to guide AI code generation:

```markdown
## Test Code Conventions
- Use JUnit 5 + AssertJ for all tests
- Name methods: shouldExpectedBehaviorWhenCondition
- Use Arrange-Act-Assert pattern
- Mock external dependencies with @MockitoBean
- Group related tests with @Nested
- Use parameterized tests for boundary values
- Use Clock injection, never LocalDate.now() directly

... more conventions ...
```

---

**AI-assisted TDD workflow:**

1. Describe the class under test and its contract in plain language
2. Ask AI to write tests first (following CLAUDE.md conventions)
3. Review and commit tests
4. Ask AI to write minimal implementation to make tests pass
5. Run PIT to check mutation coverage

---

## Diffblue Cover - AI-Generated Unit Tests

**What it is:** A commercial tool that automatically generates JUnit unit tests for existing Java code using AI.

**How it works:**
1. Analyzes production bytecode (no source required)
2. Generates `@Test` methods covering branches and edge cases
3. Integrates with IntelliJ IDEA and CI/CD pipelines

**Strengths:**
- Boosts coverage for legacy code with no existing tests
- Generates regression tests before refactoring
- Runs on CI to detect new uncovered code

**Limitations:**
- Generated tests may test implementation details, not behavior
- Requires review — quantity ≠ quality
- No substitute for thinking about *what* to test and *why*

> **Best used as:** a starting point for untested legacy code, not a replacement for thoughtful TDD.

---

# Workshop Summary

## Lab 1 — JUnit 5 & The Testing Pyramid

- JUnit 5 annotations: `@Test`, `@BeforeEach`, `@Nested`, `@ParameterizedTest`, `@Tag`
- Maven Surefire (unit) vs. Failsafe (integration) plugin split
- Test categorization with `@Tag` and Maven profiles (`-Punit-tests`)
- The testing pyramid: unit → integration → e2e; prefer fast tests at the bottom

---

# Workshop Summary

## Lab 2 — Sliced Testing & Testcontainers

- `@WebMvcTest` — loads only the web layer (controller + filters), fast and focused
- `@DataJpaTest` — loads only JPA layer with in-memory or real DB
- `@JsonTest` — tests JSON serialization/deserialization in isolation
- `@RestClientTest` — tests HTTP clients with a mock server
- Testcontainers with `@ServiceConnection` — real PostgreSQL in Docker, zero config

---

# Workshop Summary

## Lab 3 — WireMock & External APIs

- WireMock standalone for stubbing HTTP dependencies in tests
- `ApplicationContextInitializer` pattern to wire WireMock into Spring context
- Stub request/response matching: URL, headers, body matchers
- `WireMock.verify()` to assert outbound HTTP calls were made
- Separating stub definitions into dedicated stub classes for reuse

---

# Workshop Summary

## Lab 4 — Best Practices & Mutation Testing

- Best practices: test readability, meaningful names, AAA structure
- `@MockitoBean` vs `@MockBean` (Spring Boot 3.4+)
- Spring Boot test slices deep dive: what each slice loads
- PIT mutation testing: detecting weak assertions and missing branches
- AI-assisted test generation: using LLMs as a TDD pair programmer

---

# Workshop Summary

## Lab 5 — Advanced Spring Test Slices

- `@SpringBootTest` full context vs. targeted slices
- Custom test slices with `@AutoConfigureXxx`
- `@TestConfiguration` for test-only beans
- Security testing: `@WithMockUser`, `@WithSecurityContext`
- Validation testing with `@SpringBootTest(webEnvironment = NONE)`

---

# Workshop Summary

## Lab 6 — Spring Context Caching

- Spring caches `ApplicationContext` across tests sharing the same configuration
- Context cache killers: `@DirtiesContext`, `@MockitoBean`, `@ActiveProfiles`, `@TestPropertySource`
- The `SharedIntegrationTestBase` pattern: one base class → one cached context
- Scout24 example: 12 contexts → 2 contexts, 45 min → 12 min build time
- Use consistent `@Import`, `@ContextConfiguration` across all integration tests

---

# Workshop Summary

## Lab 7 — Test Parallelization & CI Excellence

- JUnit Jupiter parallel execution + Maven Surefire `forkCount` — complementary mechanisms
- Safe integration test parallelization: `@Transactional` + UUID test data
- Testcontainers: static `@Bean @ServiceConnection` singleton pattern
- Connection pool exhaustion: reduce `hikari.maximum-pool-size` + maximize context reuse
- GitHub Actions: `timeout-minutes`, `cache: maven`, `redirectTestOutputToFile`, `--fail-at-end`

---

# Workshop Summary

## Lab 8 — General Testing Hacks

- `OutputCaptureExtension` — capture stdout/logs in tests without Spring context
- Mutation testing with PIT — find weak assertions and untested branches
- Container log capture — `Slf4jLogConsumer` + `getLogs()` for debugging
- `@RecordApplicationEvents` — assert Spring application events are published
- `ApplicationContextRunner` — test conditional beans in milliseconds
- ArchUnit — enforce layered architecture rules as executable tests
- GreenMail — test email sending without a real SMTP server
- TDD with AI — CLAUDE.md conventions, test-first prompting, Diffblue Cover

---


## Q & A



---

## Joyful Testing!

Workshop materials are on [GitHub](https://github.com/PragmaTech-GmbH/digdir-workshop/)

The rendered slides are in the `slides/` folder.

![bg right:33%](assets/end.jpg)

Reach out any time via:
- [LinkedIn](https://www.linkedin.com/in/rieckpil) (Philip Riecks)
- [X](https://x.com/rieckpil) (@rieckpil)
- [Mail](mailto:philip@pragmatech.digital) (philip@pragmatech.digital)


