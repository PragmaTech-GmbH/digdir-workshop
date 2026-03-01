# Lab 6: Understanding Spring Test Context Caching for Fast Builds

## Learning Objectives

- Understand how Spring's TestContext caching mechanism works
- Identify common configuration mistakes that break context caching
- Measure the performance impact of `@DirtiesContext` and `@MockitoBean`
- Apply the `SharedIntegrationTestBase` pattern to maximize context reuse
- Reduce integration test suite execution time

## How Context Caching Works

Spring's TestContext framework caches application contexts between test classes to avoid the expensive cost of starting a new context for every test. A single Spring Boot context startup typically takes 5–10 seconds, so a test suite with 20 integration test classes could take 100–200 seconds just on context initialization if caching is broken.

The context cache key is composed of:

- **Configuration classes and locations** (`@ContextConfiguration`, `@SpringBootTest(classes = ...)`)
- **Active profiles** (`@ActiveProfiles`)
- **Property sources** (`@TestPropertySource`)
- **Context initializers** (`@ContextConfiguration(initializers = ...)`)
- **Bean overrides** (`@MockitoBean`, `@SpyBean`)

If two test classes have the **exact same** combination of these attributes, they share the same cached context. If **any** attribute differs, a new context is created.

## What Triggers a New Context

| Configuration Change | New Context? | Why |
|---|---|---|
| Different `@ActiveProfiles` | Yes | Profiles are part of the cache key |
| Different `@TestPropertySource` | Yes | Property sources are part of the cache key |
| Adding `@MockitoBean` | Yes | Bean overrides change the bean definitions |
| Different `@MockitoBean` target | Yes | Different mock targets produce different keys |
| `@DirtiesContext` | Destroys existing | Forces context destruction and recreation |
| `@Transactional` | No | Only affects transaction behavior, not the context key |
| Different test method names | No | Methods are not part of the cache key |

## The `@DirtiesContext` Problem

`@DirtiesContext` is the most expensive annotation in terms of test performance. It marks the context as "dirty" and forces Spring to destroy it and create a new one:

```java
// AVOID: Destroys and recreates context before EVERY test method
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)

// AVOID: Destroys context after the entire test class
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
```

**Better alternatives:**
- Use `@Transactional` for automatic database rollback after each test
- Use `@Sql` annotations to set up and tear down test data
- Reset WireMock stubs in `@AfterEach` methods
- Design beans to be stateless

## The `SharedIntegrationTestBase` Pattern

The recommended approach is a single abstract base class with all common test annotations:

```java
@SpringBootTest
@Import(LocalDevTestcontainerConfig.class)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
public abstract class SharedIntegrationTestBase {
    // Common test infrastructure provided via annotations above
}
```

All integration tests extend this base class:

```java
class MyFeatureIT extends SharedIntegrationTestBase {
    @Autowired
    private BookRepository bookRepository;

    @Test
    void shouldDoSomething() {
        // Uses the shared context — no extra startup cost
    }
}
```

**Rules for subclasses to maintain a single cached context:**
1. Do NOT add `@DirtiesContext`
2. Do NOT add `@MockitoBean` or `@SpyBean`
3. Do NOT add `@TestPropertySource` with unique properties
4. Do NOT add `@ActiveProfiles` with a different profile
5. Use WireMock stubs for external API simulation instead of mocking clients
6. Use `@Transactional` or `@Sql` for data isolation — these do not break caching

## Exercises

### Exercise 1: Context Caching Analysis

Observe which test configurations break Spring's context cache and document your findings.

**Tasks:**
1. Open `Exercise1ContextCachingAnalysis.java` in the `exercises` package
2. Run the five `ContextCacheKiller*IT` tests and count the application contexts created
   - Look for `"Initializing Spring"` log messages in the console output
3. For each of the five tests, identify what configuration difference causes a separate context
4. Fill in the analysis comments in `Exercise1ContextCachingAnalysis.java`
5. Propose the minimal changes to reduce all five tests to share ONE context

**Run:**
```bash
./mvnw test -pl labs/lab-6 -Dtest="ContextCacheKiller*IT"
```

**File:** `exercises/Exercise1ContextCachingAnalysis.java`
**Solution:** `solutions/Solution1ContextCachingAnalysis.java`

---

### Exercise 2: Shared Base Class

Apply the `SharedIntegrationTestBase` pattern to eliminate duplicate context creation across the suite.

**Tasks:**
1. Open `Exercise2SharedBaseClassTest.java` — make the class extend `SharedIntegrationTestBase` (already provided in the `config` package)
2. Add an `@Autowired BookRepository` field and assert it is not null in the test
3. Refactor the `ContextCacheKiller*IT` tests to extend `SharedIntegrationTestBase`:
   - Remove duplicate `@SpringBootTest`, `@Import`, `@ContextConfiguration` from each test
   - Remove `@DirtiesContext` — it defeats caching entirely
   - Remove `@MockitoBean` annotations — use WireMock stubs from the shared context instead
   - Remove `@TestPropertySource` with unique values — move shared properties to `application-test.yml`
   - Remove `@ActiveProfiles` differences — keep profiles consistent across the suite
4. Run all integration tests and verify only ONE `"Initializing Spring"` log line appears
5. Compare build time before and after the optimization

**File:** `exercises/Exercise2SharedBaseClassTest.java`
**Solution:** `solutions/Solution2SharedBaseClassTest.java`

## Hints

- Look for `"Initializing Spring"` in the console output to count context creations
- `SharedIntegrationTestBase` is already implemented in the `config` package — extend it, don't recreate it
- Solutions are available in the `solutions` package

## How to Run

```bash
# Run only the ContextCacheKiller tests (observe multiple contexts)
./mvnw test -pl labs/lab-6 -Dtest="ContextCacheKiller*IT"

# Run all lab-6 tests
./mvnw test -pl labs/lab-6
```

## Key Takeaways

1. **Context caching is automatic** but fragile — small annotation differences break it
2. **`@DirtiesContext` is expensive** — avoid it unless absolutely necessary
3. **`@MockitoBean` creates new contexts** — prefer WireMock stubs for HTTP client simulation
4. **A shared base class** is the simplest way to ensure consistent annotations across all integration tests
5. **Measure your build time** before and after optimizing context caching
