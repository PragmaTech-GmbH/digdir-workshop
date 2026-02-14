# Lab 6: Understanding Spring Test Context Caching for Fast Builds

## Learning Objectives

- Understand how Spring's TestContext caching mechanism works
- Identify common configuration mistakes that break context caching
- Measure the performance impact of `@DirtiesContext` and `@MockitoBean`
- Apply the SharedIntegrationTestBase pattern to maximize context reuse
- Reduce integration test suite execution time

## How Context Caching Works

Spring's TestContext framework caches application contexts between test classes to avoid the expensive cost of starting a new context for every test. A single Spring Boot context startup typically takes 5-10 seconds, so a test suite with 20 integration test classes could take 100-200 seconds just on context initialization if caching is broken.

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

## The @DirtiesContext Problem

`@DirtiesContext` is the most expensive annotation in terms of test performance. It marks the context as "dirty" and forces Spring to destroy it and create a new one:

```java
// AVOID: Destroys and recreates context after EVERY test method
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)

// AVOID: Destroys context after the entire test class
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
```

**Better alternatives:**
- Use `@Transactional` for automatic database rollback after each test
- Use `@Sql` annotations to set up and tear down test data
- Reset WireMock stubs in `@AfterEach` methods
- Design beans to be stateless

## The SharedIntegrationTestBase Pattern

The recommended approach is to create a single abstract base class with all common test annotations:

```java
@SpringBootTest
@Import(LocalDevTestcontainerConfig.class)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
public abstract class SharedIntegrationTestBase {
    // Common test infrastructure
}
```

All integration tests extend this base class:

```java
class MyFeatureIT extends SharedIntegrationTestBase {
    @Autowired
    private MyService myService;

    @Test
    void shouldDoSomething() {
        // Uses the shared context - no extra startup cost
    }
}
```

**Rules for subclasses to maintain a single cached context:**
1. Do NOT add `@DirtiesContext`
2. Do NOT add `@MockitoBean` or `@SpyBean`
3. Do NOT add `@TestPropertySource` with unique properties
4. Do NOT add `@ActiveProfiles` with a different profile
5. Use WireMock stubs for external API simulation instead of mocking clients
6. Use `@Transactional` or `@Sql` for data isolation (these do not break caching)

## Exercises

### Exercise 1: Context Caching Analysis

Run the five `ContextCacheKiller*IT` tests in the `experiment` package and analyze:
1. How many application contexts are created (look for "Initializing Spring" log messages)
2. What configuration difference in each test causes a separate context
3. Propose changes to reduce the number of contexts to ONE

### Exercise 2: Shared Base Class

Create a `SharedIntegrationTestBase` and refactor all integration tests to extend it, ensuring only a single application context is created across the entire test suite.

## Hints

- Look for `"Initializing Spring"` in the console output to count context creations
- The experiment files in `src/test/java/.../experiment/` demonstrate both bad and good patterns
- `DirtiesContextDemoIT` shows the per-method cost of `@DirtiesContext`
- `OptimizedContextReuseIT` shows the ideal pattern using `SharedIntegrationTestBase`
- Solutions are available in the `solutions` package

## How to Run

```bash
# Run only the ContextCacheKiller experiments (observe multiple contexts)
./mvnw test -pl labs/lab-6 -Dtest="ContextCacheKiller*IT"

# Run the DirtiesContext demo (observe context reloads)
./mvnw test -pl labs/lab-6 -Dtest="DirtiesContextDemoIT"

# Run the optimized test (observe context reuse)
./mvnw test -pl labs/lab-6 -Dtest="OptimizedContextReuseIT"

# Run all lab-6 tests
./mvnw test -pl labs/lab-6
```

## Key Takeaways

1. **Context caching is automatic** but fragile - small annotation differences break it
2. **@DirtiesContext is expensive** - avoid it unless absolutely necessary
3. **@MockitoBean creates new contexts** - prefer WireMock stubs for HTTP clients
4. **A shared base class** is the simplest way to ensure consistent annotations
5. **Measure your build time** before and after optimizing context caching
