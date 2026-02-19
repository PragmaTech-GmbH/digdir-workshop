# Lab 7: Test Parallelization and Spring Boot Testing Best Practices

## Overview

This lab explores how to speed up your test suite using JUnit 5 parallel execution, Maven Surefire fork configuration, and best practices for maintaining test isolation. You will also learn about mutation testing with PIT to measure the quality of your tests.

## Key Concepts

### JUnit 5 Parallel Execution

JUnit 5 supports running tests in parallel at both the class level and the method level. Configuration is done via `junit-platform.properties` or Maven Surefire plugin settings.

There are two independent axes of parallelism:

| Setting | Value | Meaning |
|---|---|---|
| `mode.default` | `same_thread` | Methods within a class run sequentially |
| `mode.default` | `concurrent` | Methods within a class run in parallel |
| `mode.classes.default` | `same_thread` | Test classes run sequentially |
| `mode.classes.default` | `concurrent` | Test classes run in parallel |

The recommended starting point is **classes concurrent, methods same_thread**. This gives you parallelism benefits while keeping method-level execution simple and predictable.

### Maven Surefire forkCount

The `forkCount` setting in the maven-surefire-plugin controls how many **separate JVM processes** are spawned to run tests. This is different from JUnit 5 parallel execution, which uses **threads within a single JVM**.

```
forkCount=1 (default): All tests run in one JVM
forkCount=2:           Tests are split across 2 JVM processes
forkCount=0:           Tests run in the Maven process itself (not recommended)
```

These two mechanisms are complementary:
- **forkCount** provides process-level isolation (separate heaps, class loaders)
- **JUnit 5 parallel** provides thread-level concurrency within each fork

### Test Isolation Patterns

When running tests in parallel, shared mutable state causes failures. The three main isolation strategies are:

1. **@Transactional** - Wraps each test in a transaction that rolls back after the test. Other tests never see the data. This is the most effective approach for database tests.

2. **Unique test data** - Generate unique identifiers (e.g., UUID-based ISBNs) so tests never collide on unique constraints. Essential as a defense-in-depth measure.

3. **@Sql setup/cleanup** - Use SQL scripts to set up and tear down test data explicitly. Useful when @Transactional is not applicable (e.g., testing transaction boundaries).

### Mutation Testing with PIT

PIT (PITest) mutates your production code and re-runs your tests to check if they catch the mutations. If a test suite still passes after a mutation, that mutation "survived" -- indicating a gap in your test coverage.

Common mutation types:
- **Conditionals boundary**: changes `<` to `<=`, `>` to `>=`
- **Negate conditionals**: changes `==` to `!=`
- **Remove conditionals**: replaces conditionals with `true` or `false`
- **Return values**: changes return values (e.g., `return 0` to `return 1`)

## Project Structure

```
src/
  main/java/pragmatech/digital/workshops/lab7/
    service/
      BookService.java          -- existing book CRUD service
      DiscountService.java      -- NEW: discount calculation logic
    entity/
      Book.java                 -- JPA entity
      BookStatus.java           -- status enum
    ...
  test/java/pragmatech/digital/workshops/lab7/
    exercises/
      Exercise1ParallelExecutionTest.java   -- observe parallel execution
      Exercise2TestIsolationTest.java       -- fix isolation issues
    solutions/
      Solution1ParallelExecutionTest.java   -- parallel execution solution
      Solution2TestIsolationTest.java       -- test isolation solution
    experiment/
      DiscountServiceTest.java              -- unit tests + mutation testing target
      ParallelDatabaseAccessTest.java       -- demonstrates DB isolation
    config/
      WireMockContextInitializer.java       -- WireMock setup for integration tests
      OpenLibraryApiStub.java               -- WireMock stubs
      PostgresTestcontainer.java            -- Testcontainers config
    LocalDevTestcontainerConfig.java        -- shared Testcontainers config
    Lab7ApplicationIT.java                  -- application context smoke test
  test/resources/
    junit-platform.properties               -- JUnit 5 parallel execution config
    __files/                                -- WireMock response files
    init-postgres.sql                       -- PostgreSQL init script
    logback-test.xml                        -- test logging config
```

## Exercises

### Exercise 3: Write a Reusable JUnit 5 Testcontainers Extension

**Goal:** Build a reusable JUnit 5 extension that manages a singleton PostgreSQL Testcontainer.

This teaches the JUnit extension approach as an alternative to Spring Boot's `@ServiceConnection` + `@TestConfiguration` pattern. The extension approach works at a lower level and is framework-agnostic.

**Steps:**
1. Create class `SharedPostgresContainerExtension` implementing `org.junit.jupiter.api.extension.BeforeAllCallback`
2. Declare a `private static final PostgreSQLContainer<?> POSTGRES` field — this is the singleton
3. In a `static {}` initializer block, create and start the container, and register a `Runtime.getRuntime().addShutdownHook(...)` to stop it
4. In `beforeAll(ExtensionContext context)`, call `System.setProperty(...)` to configure Spring's datasource:
   - `spring.datasource.url` → `container.getJdbcUrl()`
   - `spring.datasource.username` → `container.getUsername()`
   - `spring.datasource.password` → `container.getPassword()`
5. Add `@ExtendWith(SharedPostgresContainerExtension.class)` to `Exercise3ReusableExtensionTest`
6. Remove `@Import(LocalDevTestcontainerConfig.class)` from that test class — the extension replaces it
7. Run the test and verify it passes

**Why does `System.setProperty` work?** JUnit's `BeforeAllCallback` runs before `@SpringBootTest` initializes the application context. Spring reads system properties during context startup, so the datasource URL is picked up correctly.

**File:** `exercises/Exercise3ReusableExtensionTest.java`
**Solution:** `solutions/SharedPostgresContainerExtension.java` + `solutions/Solution3ReusableExtensionTest.java`

---

### Exercise 1: Configure and Observe Parallel Test Execution

**Goal:** Understand JUnit 5 parallel execution configuration and observe its effect.

**Steps:**
1. Open `src/test/resources/junit-platform.properties` and review the settings
2. Run `mvn test` and observe thread names in the console output
3. Try different parallelism strategies by modifying the properties file:
   - Classes concurrent, methods same_thread (current default)
   - Both classes and methods concurrent
   - Parallel execution fully disabled
4. Compare build times for each configuration
5. Note which tests break with aggressive parallelism and why

**File:** `exercises/Exercise1ParallelExecutionTest.java`
**Solution:** `solutions/Solution1ParallelExecutionTest.java`

### Exercise 2: Fix Test Isolation Issues

**Goal:** Apply isolation strategies so all tests pass reliably under parallel execution.

**Steps:**
1. Review the test class and identify potential isolation issues
2. Apply `@Transactional` for automatic rollback
3. Use UUID-based ISBNs to avoid unique constraint collisions
4. Write tests that insert, retrieve, and delete books without interfering with each other
5. Run `mvn test` and verify all tests pass consistently

**File:** `exercises/Exercise2TestIsolationTest.java`
**Solution:** `solutions/Solution2TestIsolationTest.java`

## Experiments

### Experiment: DiscountService Unit Tests and Mutation Testing

**Goal:** Write thorough unit tests and validate them with PIT mutation testing.

**Steps:**
1. Review `experiment/DiscountServiceTest.java` to understand the test coverage
2. Run the tests: `mvn test -Dtest=DiscountServiceTest`
3. Run PIT mutation testing: `mvn pitest:mutate`
4. Open the PIT report at `target/pit-reports/index.html`
5. Check which mutants survived and consider adding tests to kill them
6. Aim for 100% mutation coverage

**File:** `experiment/DiscountServiceTest.java`

### Experiment: Parallel Database Access Challenges

**Goal:** Observe and fix database isolation issues in parallel tests.

**Steps:**
1. Review `experiment/ParallelDatabaseAccessTest.java`
2. Try removing `@Transactional` and using hardcoded ISBNs -- observe failures
3. Add back `@Transactional` and/or UUID-based ISBNs -- observe passing tests
4. Understand why `@Transactional` provides stronger isolation than unique data alone

**File:** `experiment/ParallelDatabaseAccessTest.java`

## Running the Lab

### Run all unit tests (with parallel execution)
```bash
mvn test
```

### Run integration tests
```bash
mvn verify
```

### Run mutation testing
```bash
mvn pitest:mutate
```

### Run a specific test class
```bash
mvn test -Dtest=DiscountServiceTest
mvn test -Dtest=Solution1ParallelExecutionTest
```

### Disable parallel execution temporarily
```bash
mvn test -Djunit.jupiter.execution.parallel.enabled=false
```

## Configuration Reference

### junit-platform.properties
```properties
junit.jupiter.execution.parallel.enabled = true
junit.jupiter.execution.parallel.mode.default = same_thread
junit.jupiter.execution.parallel.mode.classes.default = concurrent
```

### maven-surefire-plugin (pom.xml)
```xml
<configuration>
    <forkCount>2</forkCount>
    <properties>
        <configurationParameters>
            junit.jupiter.execution.parallel.enabled = true
            junit.jupiter.execution.parallel.mode.default = same_thread
            junit.jupiter.execution.parallel.mode.classes.default = concurrent
        </configurationParameters>
    </properties>
</configuration>
```

### PIT mutation testing plugin (pom.xml)
```xml
<plugin>
    <groupId>org.pitest</groupId>
    <artifactId>pitest-maven</artifactId>
    <configuration>
        <targetClasses>
            <param>pragmatech.digital.workshops.lab7.service.DiscountService</param>
        </targetClasses>
        <targetTests>
            <param>pragmatech.digital.workshops.lab7.experiment.DiscountServiceTest</param>
        </targetTests>
        <mutators>
            <mutator>DEFAULTS</mutator>
            <mutator>REMOVE_CONDITIONALS</mutator>
        </mutators>
    </configuration>
</plugin>
```

## Best Practices Summary

1. **Start with class-level parallelism** -- it is the safest entry point and often provides the biggest speedup.
2. **Always use @Transactional** for Spring Boot integration tests that modify the database.
3. **Generate unique test data** with UUIDs as a defense-in-depth measure against data collisions.
4. **Use @Execution annotations** to opt individual test classes in or out of parallel execution.
5. **Measure before optimizing** -- compare build times with and without parallelism to quantify the benefit.
6. **Run mutation testing** periodically to validate that your tests actually catch regressions.
7. **Combine forkCount with JUnit 5 parallel execution** for maximum throughput on CI servers.
8. **Avoid shared mutable state** in test classes -- no static fields, no shared test fixtures that mutate.
