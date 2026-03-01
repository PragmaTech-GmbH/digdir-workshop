# Lab 8: Spring Boot Testing Grab Bag

This lab covers a collection of practical techniques that frequently come up in real-world Spring Boot projects: architecture testing with ArchUnit, verifying application events, capturing output and container logs, testing conditional auto-configuration, and email testing with GreenMail.

## Learning Objectives

- Write ArchUnit rules to enforce layered architecture constraints
- Use `@RecordApplicationEvents` to assert that Spring application events are published
- Capture console and log output in tests with `OutputCaptureExtension`
- Test `@Conditional` beans without starting a full Spring context using `ApplicationContextRunner`
- Capture Testcontainers logs with `Slf4jLogConsumer`
- Test email sending with the GreenMail in-process SMTP server

## Project Structure

```
src/test/java/pragmatech/digital/workshops/lab8/
  exercises/
    Exercise1ArchUnitTest.java                  -- ArchUnit exercise (TODO stubs)
    Exercise2RecordApplicationEventsTest.java   -- @RecordApplicationEvents exercise (TODO stub)
    Exercise1GithubActionsWorkflow.java         -- GitHub Actions CI configuration exercise
    Exercise2MavenBestPractices.java            -- Maven test profiles exercise
  solutions/
    Solution1ArchUnitTest.java
    Solution2RecordApplicationEventsTest.java
    Solution1GithubActionsWorkflow.java
    Solution2MavenBestPractices.java
  config/
    WireMockContextInitializer.java
    OpenLibraryApiStub.java
    PostgresTestcontainer.java
  LocalDevTestcontainerConfig.java
  Lab8ApplicationIT.java
src/test/resources/
  junit-platform.properties
  logback-test.xml
.github/workflows/
  build.yml          -- basic CI workflow (starting point for the GHA exercise)
  nightly.yml        -- nightly build workflow example
```

## Exercises

### Exercise 1: Enforce Architecture Rules with ArchUnit

Write ArchUnit rules that verify the layered architecture of the application is respected.

**Background:** ArchUnit statically analyses compiled bytecode and fails the test if any rule is violated — no application context is started.

**Tasks:**
1. Open `Exercise1ArchUnitTest.java` in the `exercises` package
2. Replace the `controllersShouldNotAccessRepositories` placeholder with a real rule:
   - Classes in the `controller` package (excluding `ThreadController`) must not access the `repository` package
   - Use `noClasses().that().resideInAPackage("..controller..").and().doNotHaveSimpleName("ThreadController")`
3. Replace the `layeredArchitectureRuleShouldBeRespected` placeholder with a `layeredArchitecture()` rule:
   - Define three layers: `Controller → Service → Repository`
   - Use `.ignoreDependency(ThreadController.class, BookRepository.class)` to exempt the known violation

**Tips:**
- `@AnalyzeClasses` with `ImportOption.DoNotIncludeTests.class` scans only production code
- `@ArchTest` on `static final ArchRule` fields is picked up automatically by the JUnit 5 ArchUnit extension
- `ThreadController` intentionally bypasses the service layer — always exclude it explicitly

**File:** `exercises/Exercise1ArchUnitTest.java`
**Solution:** `solutions/Solution1ArchUnitTest.java`

---

### Exercise 2: Verify Application Events with `@RecordApplicationEvents`

Assert that `BookCreatedEvent` is published when a book is created via `BookService`.

**Background:** `@RecordApplicationEvents` captures all `ApplicationEvent`s published during a test. The recorded events are exposed via an injected `ApplicationEvents` field.

**Tasks:**
1. Open `Exercise2RecordApplicationEventsTest.java` in the `exercises` package
2. Implement `shouldPublishBookCreatedEventWhenCreatingBook`:
   - Create a `BookCreationRequest` with ISBN `"978-0201633610"`, a title, an author, and a past date
   - Call `bookService.createBook(request)`
   - Assert that exactly one `BookCreatedEvent` was published using `events.stream(BookCreatedEvent.class)`
   - Assert the `isbn`, `title`, and `bookId` fields of the published event

**Tips:**
- `ApplicationEvents` must be injected as a field (not via constructor) in the test class
- Use `events.stream(BookCreatedEvent.class).count()` to check how many events were published
- Use `.findFirst().orElseThrow()` to retrieve the event and inspect its fields

**File:** `exercises/Exercise2RecordApplicationEventsTest.java`
**Solution:** `solutions/Solution2RecordApplicationEventsTest.java`

---

### Exercise 3: Optimize GitHub Actions Workflow

Review and enhance `.github/workflows/build.yml` with CI/CD best practices. No Java code required.

**Tasks:**
1. Open `.github/workflows/build.yml` and identify what is missing
2. Add Maven dependency caching using `setup-java`'s built-in `cache: maven`
3. Add `timeout-minutes` to prevent runaway builds
4. Add a step to upload test reports on failure using `actions/upload-artifact@v4`
5. Review `.github/workflows/nightly.yml` for scheduled build and tag-filtering patterns

**File:** `exercises/Exercise1GithubActionsWorkflow.java` (guidance comments only)
**Solution:** `solutions/Solution1GithubActionsWorkflow.java`

---

### Exercise 4: Test Organization with `@Tag` and Maven Profiles

Learn how to categorize tests and run subsets selectively.

**Tasks:**
1. Open `pom.xml` and review the `unit-tests` and `integration-tests` Maven profiles
2. Run only unit-tagged tests: `mvn test -Punit-tests`
3. Run only nightly-tagged tests: `mvn verify -Dgroups=nightly`
4. Compare execution times between selective and full runs

**Maven commands:**
```bash
# Run all tests (default)
mvn verify

# Run only unit-tagged tests (fast feedback)
mvn test -Punit-tests

# Run only integration-tagged tests
mvn verify -Pintegration-tests

# Run tests by tag directly (without profiles)
mvn test -Dgroups=unit
mvn verify -Dgroups=nightly

# Exclude specific tags (e.g., skip slow tests in PR builds)
mvn verify -DexcludedGroups=nightly
```

**File:** `exercises/Exercise2MavenBestPractices.java` (guidance comments only)
**Solution:** `solutions/Solution2MavenBestPractices.java`

## Key Concepts

### ArchUnit

ArchUnit checks architectural constraints at the bytecode level without starting a Spring context. Rules are expressed in a fluent Java DSL and run as regular JUnit 5 tests via the `@ArchTest` annotation.

```java
@AnalyzeClasses(packages = "com.example", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule controllersShouldNotAccessRepositories = noClasses()
        .that().resideInAPackage("..controller..")
        .should().accessClassesThat().resideInAPackage("..repository..");
}
```

### `@RecordApplicationEvents`

`@RecordApplicationEvents` tells Spring's test framework to record all `ApplicationEvent`s published during a test. The events are available via an `ApplicationEvents` field injected into the test class.

```java
@SpringBootTest
@RecordApplicationEvents
class BookServiceTest {

    @Autowired
    ApplicationEvents events;

    @Test
    void shouldPublishEvent() {
        bookService.createBook(request);

        assertThat(events.stream(BookCreatedEvent.class)).hasSize(1);
    }
}
```

### `ApplicationContextRunner`

`ApplicationContextRunner` allows testing Spring auto-configuration and `@Conditional` beans without starting a full application context. It is fast and lightweight — ideal for testing configuration classes in isolation.

```java
private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
    .withUserConfiguration(ConditionalBookImportConfig.class);

@Test
void shouldHaveBeanWhenPropertyEnabled() {
    contextRunner
        .withPropertyValues("bookshelf.import.enabled=true")
        .run(context -> assertThat(context).hasSingleBean(String.class));
}
```

### GreenMail

GreenMail provides an in-process SMTP server for testing email sending. Register it as a JUnit 5 extension with `@RegisterExtension` and assert on received messages after exercising the production code.

## CI/CD Strategy Reference

| Build Type    | Command                                  | When                 |
|---------------|------------------------------------------|----------------------|
| PR Build      | `mvn verify -DexcludedGroups=nightly`    | Every pull request   |
| Merge to Main | `mvn verify`                             | Push to main branch  |
| Nightly       | `mvn verify -Dgroups=nightly`            | Scheduled (cron)     |

## How to Run

```bash
# Run all lab-8 tests
mvn test -pl labs/lab-8

# Run ArchUnit exercise
mvn test -pl labs/lab-8 -Dtest="Exercise1ArchUnitTest"

# Run @RecordApplicationEvents exercise
mvn test -pl labs/lab-8 -Dtest="Exercise2RecordApplicationEventsTest"

# Run solutions
mvn test -pl labs/lab-8 -Dtest="Solution1ArchUnitTest"
mvn test -pl labs/lab-8 -Dtest="Solution2RecordApplicationEventsTest"
```
