# Lab 7: Test Parallelization and Test Isolation

## Learning Objectives

- Configure JUnit 5 parallel test execution via `junit-platform.properties`
- Understand the difference between JUnit 5 thread-level parallelism and Maven Surefire `forkCount` process-level forking
- Identify and fix test isolation issues caused by shared database state
- Apply `@Transactional` and UUID-based unique data as complementary isolation strategies

## Key Concepts

### JUnit 5 Parallel Execution

JUnit 5 supports running tests in parallel at both the class level and the method level. Configuration lives in `src/test/resources/junit-platform.properties`.

There are two independent axes of parallelism:

| Setting | Value | Meaning |
|---|---|---|
| `mode.default` | `same_thread` | Methods within a class run sequentially |
| `mode.default` | `concurrent` | Methods within a class run in parallel |
| `mode.classes.default` | `same_thread` | Test classes run sequentially |
| `mode.classes.default` | `concurrent` | Test classes run in parallel |

The recommended starting point is **classes concurrent, methods same_thread**. This gives you parallelism benefits while keeping method-level execution simple and predictable.

### Maven Surefire `forkCount`

The `forkCount` setting in `maven-surefire-plugin` controls how many **separate JVM processes** are spawned to run tests. This is different from JUnit 5 parallel execution, which uses **threads within a single JVM**.

```
forkCount=1 (default): All tests run in one JVM
forkCount=2:           Tests are split across 2 JVM processes
forkCount=0:           Tests run in the Maven process itself (not recommended)
```

These two mechanisms are complementary:
- **`forkCount`** provides process-level isolation (separate heaps, class loaders)
- **JUnit 5 parallel** provides thread-level concurrency within each fork

### Test Isolation Strategies

When running tests in parallel, shared mutable state causes failures. The three main isolation strategies are:

1. **`@Transactional`** — Wraps each test in a transaction that rolls back after the test. Other tests never see the data. This is the most effective approach for database tests.
2. **Unique test data** — Generate unique identifiers (e.g., UUID-based ISBNs) so tests never collide on unique constraints. Essential as a defense-in-depth measure.
3. **`@Sql` setup/cleanup** — Use SQL scripts to set up and tear down test data explicitly. Useful when `@Transactional` is not applicable (e.g., testing transaction boundaries).

## Project Structure

```
src/test/java/pragmatech/digital/workshops/lab7/
  exercises/
    Exercise1ParallelExecutionTest.java   -- observe parallel execution (TODO stubs)
    Exercise2TestIsolationTest.java       -- fix isolation issues (TODO stubs)
  solutions/
    Solution1ParallelExecutionTest.java
    Solution2TestIsolationTest.java
  config/
    WireMockContextInitializer.java
    OpenLibraryApiStub.java
    PostgresTestcontainer.java
  LocalDevTestcontainerConfig.java
  Lab7ApplicationIT.java
src/test/resources/
  junit-platform.properties              -- JUnit 5 parallel execution config
  logback-test.xml
```

## Exercises

### Exercise 1: Configure and Observe Parallel Test Execution

Understand JUnit 5 parallel execution configuration and observe its effect on thread allocation.

**Tasks:**
1. Open `src/test/resources/junit-platform.properties` and review the current settings
2. Open `Exercise1ParallelExecutionTest.java` — the test methods print the current thread name
3. Run `mvn test` and observe which threads each test class runs on in the console output
4. Try different parallelism strategies by modifying `junit-platform.properties`:
   - Classes concurrent, methods `same_thread` (current default)
   - Both classes and methods `concurrent`
   - Parallel execution fully disabled (`parallel.enabled = false`)
5. Compare build times for each configuration
6. In the test class, understand why `forkCount=2` in `pom.xml` complements JUnit 5 parallelism

**File:** `exercises/Exercise1ParallelExecutionTest.java`
**Solution:** `solutions/Solution1ParallelExecutionTest.java`

---

### Exercise 2: Fix Test Isolation Issues for Parallel Execution

Apply isolation strategies so tests pass reliably when running concurrently against a shared database.

**Tasks:**
1. Open `Exercise2TestIsolationTest.java` — it has `MockMvc` and `BookRepository` injected
2. Implement `shouldCreateBookWithIsolatedData`:
   - Generate a unique ISBN using `UUID.randomUUID().toString().substring(0, 13)`
   - Insert a `Book` via `BookRepository` and assert it was saved
   - Add `@Transactional` at the class level for automatic rollback
3. Implement `shouldRetrieveBookWithoutSideEffects`:
   - Insert a book directly, then retrieve it via `GET /api/books/{id}` using MockMvc
   - Assert the response fields with `jsonPath`
4. Implement `shouldDeleteBookSafely`:
   - Insert a book, delete it via `DELETE /api/books/{id}`, assert it no longer exists
5. Run `mvn test` and verify all tests pass consistently

**Tips:**
- `@Transactional` on the test class rolls back after every method — no `@AfterEach` needed
- `UUID.randomUUID().toString().substring(0, 13)` produces a valid-length unique ISBN
- Use `@WithMockUser(roles = "USER")` for GET, `@WithMockUser(roles = "ADMIN")` for DELETE

**File:** `exercises/Exercise2TestIsolationTest.java`
**Solution:** `solutions/Solution2TestIsolationTest.java`

## How to Run

```bash
# Run all lab-7 tests (parallel execution enabled)
mvn test

# Run a specific exercise
mvn test -Dtest=Exercise1ParallelExecutionTest
mvn test -Dtest=Exercise2TestIsolationTest

# Run solutions
mvn test -Dtest=Solution1ParallelExecutionTest
mvn test -Dtest=Solution2TestIsolationTest

# Temporarily disable parallel execution
mvn test -Djunit.jupiter.execution.parallel.enabled=false
```

## Configuration Reference

### `junit-platform.properties`
```properties
junit.jupiter.execution.parallel.enabled = true
junit.jupiter.execution.parallel.mode.default = same_thread
junit.jupiter.execution.parallel.mode.classes.default = concurrent
```

### `maven-surefire-plugin` in `pom.xml`
```xml
<configuration>
    <forkCount>2</forkCount>
</configuration>
```

## Best Practices Summary

1. **Start with class-level parallelism** — it is the safest entry point and usually provides the biggest speedup
2. **Always use `@Transactional`** for Spring Boot integration tests that modify the database
3. **Generate unique test data** with UUIDs as a defense-in-depth measure against constraint collisions
4. **Use `@Execution` annotations** to opt individual test classes in or out of parallel execution
5. **Measure before optimizing** — compare build times with and without parallelism to quantify the benefit
6. **Combine `forkCount` with JUnit 5 parallel execution** for maximum throughput on CI servers
7. **Avoid shared mutable state** in test classes — no static fields that tests write to
