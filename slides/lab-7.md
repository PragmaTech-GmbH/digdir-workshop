---
marp: true
theme: pragmatech
---

![bg](./assets/digdir-cover.jpg)

---

<!-- header: 'Testing Spring Boot Applications Demystified Workshop @ Digdir 02.03.2026' -->
<!-- footer: '![w:32 h:32](assets/generated/logo.webp)' -->

## Discuss Exercises from Lab 6

---

## Lab 6 Recap

### What We Did

- Ran the five `ContextCacheKiller*IT` tests and counted unique Spring contexts in the logs
- Identified what breaks context caching in each class: `@DirtiesContext`, `@MockitoBean`, `@ActiveProfiles`, `@TestPropertySource`
- Refactored all killer tests to extend `SharedIntegrationTestBase` → **one cached context**

### Key Takeaways

- Every annotation difference → new context → expensive cold start (10–30 seconds)
- The Scout24 example: 12 contexts → 2 contexts, build time cut from 45 min → 12 min
- The base class pattern is the single most impactful context caching optimization

---

![bg left:33%](assets/generated/lab-4.jpg)

# Lab 7

## Build Speed & CI Excellence

### Test Parallelization, Testcontainers Optimization & CI Best Practices

---

# Test Parallelization

**Goal**: Reduce build time by running tests concurrently

Two independent mechanisms — they work at different levels:

| Mechanism | Level | Isolation |
|---|---|---|
| Maven Surefire/Failsafe `forkCount` | JVM processes | Separate heaps, class loaders |
| JUnit Jupiter parallel execution | Threads within one JVM | Shared heap, shared class loader |

These are **complementary** — you can (and should) use both together.

---

## Approach 1: Maven forkCount — Process Level

Splits tests across multiple **separate JVM processes**:

```xml
<plugin>
  <artifactId>maven-surefire-plugin</artifactId>
  <configuration>
    <forkCount>1C</forkCount>     <!-- 1 JVM per CPU core -->
    <reuseForks>true</reuseForks> <!-- Reuse JVMs across test classes -->
  </configuration>
</plugin>
```

- `forkCount=1` — default: one JVM for all tests
- `forkCount=2` — two JVMs running in parallel
- `forkCount=1C` — one JVM per available CPU core (dynamic)
- `forkCount=0` — runs in Maven's own JVM (not recommended)

> **Maven Failsafe** works the same way for `*IT.java` integration tests.

---

## Approach 2: JUnit Jupiter Parallel — Thread Level

Runs test **classes** (and/or methods) concurrently on threads within one JVM:

```properties
# src/test/resources/junit-platform.properties
junit.jupiter.execution.parallel.enabled = true
junit.jupiter.execution.parallel.mode.default = same_thread
junit.jupiter.execution.parallel.mode.classes.default = concurrent
```

Or configure directly in Maven Surefire:

```xml
<properties>
  <configurationParameters>
    junit.jupiter.execution.parallel.enabled = true
    junit.jupiter.execution.parallel.mode.default = same_thread
    junit.jupiter.execution.parallel.mode.classes.default = concurrent
  </configurationParameters>
</properties>
```

---

## The Two Axes of Parallelism Visualized

![w:1050 center](assets/lab-7-parallelization.png)

---

## Parallelization Strategies Compared

| Strategy | `mode.classes.default` | `mode.default` | Effect |
|---|---|---|---|
| **Safest** (recommended start) | `concurrent` | `same_thread` | Classes in parallel, methods sequential |
| **Fastest** (requires careful isolation) | `concurrent` | `concurrent` | Everything in parallel |
| **Sequential** (debugging) | `same_thread` | `same_thread` | Fully sequential |

Override per class with `@Execution`:

```java
@Execution(ExecutionMode.CONCURRENT)  // Override global setting for this class
class DiscountServiceTest { ... }
```

---

## Unit Tests: Writing Parallel-Safe Code

Unit tests are the easiest to parallelize — **no shared infrastructure**.

**What to AVOID:**

```java
class OrderServiceTest {
  // ❌ Static mutable state — shared across all threads!
  static int callCount = 0;

  // ❌ ThreadLocal without cleanup — leaks across tests in the same thread
  static ThreadLocal<String> currentUser = new ThreadLocal<>();

  @Test
  void shouldTrackCalls() {
    callCount++;  // ❌ Race condition when methods run concurrently
  }
}
```

---

## Unit Tests: The Safe Pattern

```java
@Execution(ExecutionMode.CONCURRENT)
class DiscountServiceTest {

  // ✅ Instance field — each test class instance gets its own
  private DiscountService cut;

  @BeforeEach
  void setUp() {
    cut = new DiscountService();  // ✅ Fresh instance, no shared state
  }

  @Test
  void shouldReturnTenPercentDiscount() {
    Book book = new Book("978-0-00-000001-1", "Test", "Author",
        LocalDate.now().minusMonths(7));
    book.setStatus(BookStatus.AVAILABLE);

    assertThat(cut.calculateDiscount(book)).isEqualTo(10);
  }
}
```

**Rules for parallel-safe unit tests:**
- No static mutable fields
- No shared service instances across tests
- No `ThreadLocal` usage without guaranteed cleanup

---

## Integration Tests: Different Challenges

Integration tests share infrastructure: database, WireMock, caches.

**The additional risks with parallel integration tests:**

- Two tests `INSERT` the same ISBN → unique constraint violation
- One test `DELETE`s a record another test expects to find
- `count()` assertions return unexpected results from other tests' inserts
- Testcontainers port conflicts when each class starts its own container

**The approach:** parallelize at the **class** level only (`mode.default = same_thread`), and enforce strong data isolation within each class.

---

## Integration Tests: What to AVOID

```java
@SpringBootTest
class BookIT {
  @Autowired BookRepository bookRepository;

  // ❌ Hardcoded ISBN — another parallel test class may insert the same one
  @Test
  void shouldSaveBook() {
    bookRepository.save(new Book("978-0-13-468599-1", "Clean Code",
        "Martin", LocalDate.now()));

    // ❌ Assumes the DB is empty — other parallel tests will break this
    assertThat(bookRepository.count()).isEqualTo(1);
  }

  // ❌ DirtiesContext forces a context restart — expensive AND breaks parallelism
  @DirtiesContext
  @Test
  void shouldHandleEdgeCase() { ... }
}
```

---

## Integration Tests: The Safe Pattern

```java
@SpringBootTest
@Transactional                    // ✅ Auto-rollback after each test
class BookIT {
  @Autowired BookRepository bookRepository;

  @Test
  void shouldSaveBook() {
    // ✅ Unique ISBN per test — no collision with parallel tests
    String isbn = UUID.randomUUID().toString().substring(0, 13);
    bookRepository.save(new Book(isbn, "Test Book", "Author", LocalDate.now()));

    // ✅ Find by this specific ISBN, not total count
    assertThat(bookRepository.findByIsbn(isbn)).isPresent();
  }
}
```

**Rules for parallel-safe integration tests:**
- `@Transactional` for automatic rollback after each test
- UUID-based test data — never hardcode ISBNs or IDs
- Never assert `count()` without scope limiting to your own data
- Use `@AfterEach deleteAll()` if `@Transactional` is not applicable (e.g. `WebTestClient`)
- No `@DirtiesContext` — use `@Sql` for data setup/teardown instead

---

## Parallelization: Unit vs. Integration Summary

| | Unit Tests | Integration Tests |
|---|---|---|
| Recommended tool | JUnit parallel + Surefire forks | JUnit class-level parallel |
| Safe `mode.default` | `concurrent` | `same_thread` |
| Safe `mode.classes` | `concurrent` | `concurrent` |
| Key isolation | No shared mutable state | `@Transactional` + unique data |
| `forkCount` | Very beneficial | Be careful with Testcontainers |
| Typical speed gain | 2–4× | 1.5–2× |

**Always measure! Run with and without, compare wall-clock time.**

---

# Optimize Containers Time

## Correct Usage of Testcontainers

Reference: [maciejwalkowiak.com/blog/testcontainers-spring-boot-setup](https://maciejwalkowiak.com/blog/testcontainers-spring-boot-setup/)

---

## The Naive Approach (and Why It's Slow)

```java
// ❌ Instance @Container — starts AND stops with every test class
@Testcontainers
@SpringBootTest
class BookControllerIT {

  @Container
  PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
  //                    ↑ No static — new container per test class instance!
}
```

With 10 integration test classes → **10 container starts × ~5s each = 50s overhead**

Even with `@Container static` but spread across many classes, Ryuk stops the container between classes unless you share it properly.

---

## The Singleton Pattern: Static @Bean

Spring Boot 3.1+ — use a `@TestConfiguration` with a `static` factory method:

```java
@TestConfiguration(proxyBeanMethods = false)
public class LocalDevTestcontainerConfig {

  // ✅ static method — container created once, shared across all contexts
  // ✅ @ServiceConnection — no @DynamicPropertySource boilerplate needed
  @Bean
  @ServiceConnection
  static PostgreSQLContainer<?> postgres() {
    return new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("testdb")
      .withUsername("test")
      .withPassword("test");
  }
}
```

Use it in every integration test: `@Import(LocalDevTestcontainerConfig.class)`

---

## Singleton Container Visualized

![w:1100 center](assets/lab-7-singleton-container.png)

---

## The Hidden Danger: Connection Pool Exhaustion

Each Spring context has its **own HikariCP connection pool**.

```
PostgreSQL max_connections = 100  (default)

Context A  →  HikariPool (10 connections each)  ─┐
Context B  →  HikariPool (10 connections each)   ├──▶ 1 Postgres container
Context C  →  HikariPool (10 connections each)   │    (30 connections used)
Context D  →  HikariPool (10 connections each)  ─┘

10th context starts → FATAL: sorry, too many clients already
```

This is why Lab 6 context caching and Lab 7 parallelization work **together** — fewer contexts means fewer connection pools.

---

## Preventing Connection Pool Exhaustion

**Option 1 — Maximize context reuse** (from Lab 6):

→ One `SharedIntegrationTestBase` → one context → one pool

**Option 2 — Reduce pool size per context:**

```yaml
# src/test/resources/application.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 5   # ↓ down from default 10
```

**Option 3 — Raise PostgreSQL's max_connections:**

```java
new PostgreSQLContainer<>("postgres:16-alpine")
  .withCommand("postgres", "-c", "max_connections=200");
```

**Best practice: combine Option 1 + Option 2**

---

## Testcontainers Reuse Mode (Local Dev)

Skip container startup entirely between local test runs:

```java
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
  .withReuse(true);  // ← Surviving container is reused on next run
```

Requires `~/.testcontainers.properties`:

```properties
testcontainers.reuse.enable=true
```

**Implications:**
- Container keeps its state between runs → good test isolation (`@Transactional`) is critical
- Flyway migrations run on an already-migrated schema → use `baseline-on-migrate: true`
- **Not for CI** — CI should always start fresh containers for reproducibility

---

# Maven Best Practices for Testing

---

## Tip 1: Hide Test Output, Show on Failure

By default all test output floods the console. Redirect it to files:

```xml
<plugin>
  <artifactId>maven-surefire-plugin</artifactId>
  <configuration>
    <!-- Output goes to surefire-reports/*.txt; shown in console only on failure -->
    <redirectTestOutputToFile>true</redirectTestOutputToFile>
    <trimStackTrace>false</trimStackTrace>
  </configuration>
</plugin>
```

Same setting for Failsafe (integration tests):

```xml
<plugin>
  <artifactId>maven-failsafe-plugin</artifactId>
  <configuration>
    <redirectTestOutputToFile>true</redirectTestOutputToFile>
  </configuration>
</plugin>
```

---

## Tip 2: Rerun Flaky Tests Automatically

Instead of failing the build on a first flaky failure, retry automatically:

```xml
<plugin>
  <artifactId>maven-surefire-plugin</artifactId>
  <configuration>
    <!-- Retry failing tests up to 2 times before reporting as failure -->
    <rerunFailingTestsCount>2</rerunFailingTestsCount>
  </configuration>
</plugin>
```

**Important:**
- A test that passes on retry is reported as **"flaky"** in the XML report, not as a failure
- This is a **safety net**, not a fix — investigate and eliminate the root cause
- Track flaky test count over time; a rising number is a warning sign

---

## Tip 3: Maven Daemon Locally

`mvnd` keeps a hot Maven JVM between builds — no JVM startup cost:

```bash
# Install (macOS)
brew install mvnd

# Identical commands — just replace ./mvnw with mvnd
mvnd test
mvnd verify
mvnd clean install -DskipTests
```

| Metric | `./mvnw` | `mvnd` |
|---|---|---|
| First build | Normal | Normal |
| Subsequent builds | ~5–10s JVM startup | ~1–2s (JVM already warm) |
| Plugin class loading | Every run | Cached in daemon |

Works seamlessly with all plugins. Particularly helpful for rapid TDD cycles.

---

## Tip 4: Skip Integration Tests for Fast Local Iteration

Add a `skipITs` property to Failsafe so developers can opt out of slow tests:

```xml
<plugin>
  <artifactId>maven-failsafe-plugin</artifactId>
  <configuration>
    <skipITs>${skipITs}</skipITs>
  </configuration>
</plugin>
```

```bash
# Fast local unit tests only
./mvnw test -DskipITs

# Full build including integration tests
./mvnw verify
```

**Useful for:** quick red-green-refactor cycles where you don't want to wait for Testcontainers every time.

---

## Tip 5: Fail Fast vs. Collect All Failures

```bash
# Default: stop on first failure (fast feedback on blocking issue)
./mvnw verify

# Collect ALL failures, then report at the end (see full picture in one run)
./mvnw verify --fail-at-end
```

Use `--fail-at-end` in CI to see all failures in a single build, saving a feedback loop.

Use the default in local development when you want to fix one issue at a time.

---

## Tip 6: Dedicated CI Build Profile

Separate CI-specific settings from the default developer experience:

```xml
<profiles>
  <profile>
    <id>ci</id>
    <build>
      <plugins>
        <plugin>
          <artifactId>maven-surefire-plugin</artifactId>
          <configuration>
            <forkCount>1C</forkCount>
            <redirectTestOutputToFile>true</redirectTestOutputToFile>
          </configuration>
        </plugin>
      </plugins>
    </build>
  </profile>
</profiles>
```

```bash
./mvnw verify -P ci
```

---

# GitHub Actions Best Practices

---

## Best Practice 1: Always Set a Job Timeout

Hanging Testcontainers or infinite loops block CI slots for hours without a timeout:

```yaml
jobs:
  build:
    runs-on: ubuntu-latest
    timeout-minutes: 20    # ← Kill the job if it exceeds this
```

Good defaults:
- Unit tests only: `timeout-minutes: 10`
- Integration tests: `timeout-minutes: 20`
- Full build with slow ITs: `timeout-minutes: 30`

**No timeout = expensive surprise when a container hangs pulling an image.**

---

## Best Practice 2: Cache Maven Dependencies

The `cache: maven` flag in `setup-java` automatically caches `~/.m2/repository`:

```yaml
- uses: actions/setup-java@v4
  with:
    java-version: '21'
    distribution: 'temurin'
    cache: maven              # ← Saves ~200 MB download every run
```

Or with manual cache control (for advanced invalidation):

```yaml
- uses: actions/cache@v4
  with:
    path: ~/.m2/repository
    key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
    restore-keys: |
      ${{ runner.os }}-maven-
```

**Impact:** Cuts CI time by 1–3 minutes on dependency-heavy projects.

---

## Best Practice 3: Enable Testcontainers Reuse in CI

Pre-pull images to avoid rate limiting and cold starts:

```yaml
- name: Pre-pull Testcontainers images
  run: docker pull postgres:16-alpine

- name: Enable Testcontainers container reuse
  run: echo "testcontainers.reuse.enable=true" >> ~/.testcontainers.properties
```

Or via environment variable:

```yaml
env:
  TESTCONTAINERS_REUSE_ENABLE: true
```

**Note:** Container reuse in CI only helps when multiple test jobs share the same runner and Docker daemon. For ephemeral runners, pre-pulling the image is the main win.

---

## Best Practice 4: Separate Unit and Integration Test Jobs

Split for faster feedback and better failure visibility:

```yaml
jobs:
  unit-tests:
    runs-on: ubuntu-latest
    timeout-minutes: 10
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '21', distribution: 'temurin', cache: maven }
      - run: ./mvnw test

  integration-tests:
    runs-on: ubuntu-latest
    timeout-minutes: 20
    needs: unit-tests          # ← Only run if unit tests pass
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '21', distribution: 'temurin', cache: maven }
      - run: ./mvnw verify -DskipUnitTests
```

---

## Best Practice 5: Publish Test Results

Make test failures visible directly in the PR UI:

```yaml
- name: Upload test reports
  uses: actions/upload-artifact@v4
  if: always()           # ← Upload even when tests fail
  with:
    name: test-reports
    path: |
      **/target/surefire-reports/
      **/target/failsafe-reports/
    retention-days: 7
```

Or use `dorny/test-reporter` for inline PR annotations:

```yaml
- uses: dorny/test-reporter@v1
  if: always()
  with:
    name: Maven Tests
    path: '**/surefire-reports/TEST-*.xml,**/failsafe-reports/TEST-*.xml'
    reporter: java-junit
```

---

## Best Practice 6: Cancel Stale Runs

Avoid wasting CI minutes running old commits when a new push arrives:

```yaml
concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true    # ← Cancel the previous run for this branch
```

**Combined workflow example** (`labs/lab-7/.github/workflows/build.yml`):

```yaml
on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]
concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true
```

---

## Best Practice 7: Launch the App in Docker and Smoke-Test It

Catch **deployment-time failures** that automated unit/integration tests miss:

- Custom JVM startup flags or `-javaagent` entries missing in the image
- Missing fonts or native libraries in the container OS (e.g. `libfreetype` for PDF rendering)
- Incorrect `ENTRYPOINT` or `CMD` in the `Dockerfile`
- Memory limits or GC settings that cause OOM at startup

```yaml
- name: Build Docker image
  run: ./mvnw spring-boot:build-image -DskipTests
  # Builds a production OCI image using Cloud Native Buildpacks

- name: Smoke-test the Docker image
  run: |
    docker run --rm -d --name app-smoke \
      -e SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/testdb \
      -p 8080:8080 lab-7:1.0.0
    sleep 10
    curl --fail http://localhost:8080/actuator/health
    docker stop app-smoke
```

---

## Best Practice 7: Smoke-Test with Testcontainers in CI

Or use a dedicated `@SpringBootTest` that starts the **real Docker image**:

```java
// Launches your production Docker image — not the fat JAR — in CI
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class DockerImageSmokeIT {

  @Container
  static GenericContainer<?> app = new GenericContainer<>(
      DockerImageName.parse("lab-7:1.0.0"))
    .withExposedPorts(8080)
    .waitingFor(Wait.forHttp("/actuator/health").forStatusCode(200));

  @Test
  void shouldStartAndRespondToHealthCheck() {
    String url = "http://localhost:" + app.getMappedPort(8080) + "/actuator/health";
    // Assert the real image starts in a real Docker container
    assertThat(app.isRunning()).isTrue();
  }
}
```

**Catches:** missing system libraries, wrong base image, broken `ENTRYPOINT`.

---

## Best Practice 8: Upload Screenshots of Failed UI Tests

When Selenium or Playwright tests fail, screenshots are essential for debugging.
Never lose them — always upload as CI artifacts:

```yaml
- name: Run UI / E2E tests
  run: ./mvnw verify -P e2e
  continue-on-error: true   # Don't stop — let the upload step run

- name: Upload screenshot artifacts on failure
  uses: actions/upload-artifact@v4
  if: failure()             # Only when something broke
  with:
    name: failed-ui-screenshots
    path: |
      **/target/screenshots/
      **/target/videos/
      **/build/reports/tests/
    retention-days: 14
```

Works with **Selenide** (`screenshots/` folder), **Playwright** (`videos/`), or any framework that saves files on failure.

---

# Time For Some Exercises

## Lab 7

---

## Exercise 1: Play Around with Parallelization

**Goal:** Observe and measure the effect of different parallelization settings

**Steps:**
1. Run `./mvnw test` and note the total build time in the output
2. Open `src/test/resources/junit-platform.properties` and change:
   - `mode.default = concurrent` — observe which tests break and why
   - Reset to `mode.default = same_thread`
3. In `pom.xml`, change `forkCount` from `1C` to `1` and compare build time
4. Run `./mvnw verify` and observe thread names printed by experiment tests

**File:** `exercises/Exercise1ParallelExecutionTest.java`
**Solution:** `solutions/Solution1ParallelExecutionTest.java`

---

## Exercise 2: Fix Test Isolation for Parallel Execution

**Goal:** Make all integration tests pass reliably under parallel execution

**Steps:**
1. Open `exercises/Exercise2TestIsolationTest.java` and implement the three test methods
2. Use `@Transactional` on the class for automatic rollback
3. Generate unique ISBNs with `UUID.randomUUID().toString().substring(0, 13)`
4. Run `./mvnw test` three times in a row — verify 100% pass rate each time

**Key APIs:**
- `bookRepository.save(book)` — insert test data directly
- `mockMvc.perform(get("/api/books/{id}", id))` — call the API
- `@WithMockUser(roles = "ADMIN")` — for authenticated endpoints

**Solution:** `solutions/Solution2TestIsolationTest.java`

---

## Exercise 3: Write a Reusable Testcontainers JUnit 5 Extension

**Goal:** Build a JUnit 5 extension that manages a singleton PostgreSQL container

**Steps:**
1. Create `SharedPostgresContainerExtension` implementing `BeforeAllCallback`
2. Hold the `PostgreSQLContainer` in a **static** field (singleton per JVM)
3. In `beforeAll()`, set system properties so Spring can connect:
   ```
   System.setProperty("spring.datasource.url", container.getJdbcUrl());
   System.setProperty("spring.datasource.username", container.getUsername());
   System.setProperty("spring.datasource.password", container.getPassword());
   ```
4. Register a JVM shutdown hook to stop the container
5. Use `@ExtendWith(SharedPostgresContainerExtension.class)` in a `@SpringBootTest`
6. Remove the `@Import(LocalDevTestcontainerConfig.class)` — the extension replaces it

**File:** `exercises/Exercise3ReusableExtensionTest.java`
**Solution:** `solutions/SharedPostgresContainerExtension.java` + `solutions/Solution3ReusableExtensionTest.java`
