---
marp: true
theme: pragmatech
---

![bg](./assets/digdir-cover.jpg)

---

<!-- _class: title -->
![bg h:500 left:33%](assets/generated/demystify.png)

# Testing Spring Boot Applications Demystified

## Lab 6

_Digdir Workshop 03.03.2026_

Philip Riecks - [PragmaTech GmbH](https://pragmatech.digital/) - [@rieckpil](https://x.com/rieckpil)

---

<!-- header: 'Testing Spring Boot Applications Demystified Workshop @ Digdir 03.03.2026' -->
<!-- footer: '![w:32 h:32](assets/generated/logo.webp)' -->

## Discuss Exercises from Lab 5

---

![bg left:33%](assets/generated/lab-6.jpg)

# Lab 6

## Context Caching & Fast Builds

### Making Your Integration Test Suite Scale

---

## The Application Has Grown

The team kept shipping features — and writing integration tests:

- `@SpringBootTest` for each feature flow
- Different WireMock configurations per test class
- `@MockitoBean` for "quick isolation" wins
- A `@DirtiesContext` here and there to fix flaky tests

Then someone checks the CI build time…

---

## Build Time: The Hidden Tax

![w:900](assets/generated/build-time-growth.png)

---

## The Root Cause

Every `@SpringBootTest` context startup costs **10–30 seconds**:

- Testcontainers (PostgreSQL) starts → JDBC connection pool opens
- WireMock starts and stubs are registered
- Flyway runs migrations
- Spring wires all beans

If 10 test classes each create a **unique** context → **10 cold starts**

```
test class 1:  context A starts  (15s)
test class 2:  context A reused  (0s)   ← cached ✅
test class 3:  context B starts  (15s)  ← different annotation ❌
test class 4:  context C starts  (15s)  ← another difference ❌
```

---

## The Goal: Fast Feedback

```
Before optimisation:   10 contexts × 15s  = 150s startup overhead
After optimisation:     1 context  × 15s  =  15s startup overhead
```

**Rule of thumb:**
- One shared context for the **happy-path integration tests**
- Sliced tests (`@WebMvcTest`, `@DataJpaTest`) for layer-specific edge cases
- Only spin up a second context when there is a **genuine architectural reason**

---

## Spring Test Context Caching

- Built into Spring Test — available automatically via `spring-boot-starter-test`
- Caches a started `ApplicationContext` by a **cache key**
- Cache is per-JVM process (not shared across forks or CI agents)
- Default size: **32** entries — LRU eviction after that

```
┌────────────────────────────────────────────┐
│  DefaultContextCache                        │
│                                             │
│  key: MergedContextConfiguration           │
│       └─ profiles + properties + mocks +  │
│          initializers + config classes     │
│                                             │
│  value: ApplicationContext (reused!)        │
└────────────────────────────────────────────┘
```

---

## How the Cache Key Is Built

`MergedContextConfiguration` is the cache key. It includes:

| Component | Annotation |
|---|---|
| Active profiles | `@ActiveProfiles` |
| Context initializers | `@ContextConfiguration(initializers = ...)` |
| Property source locations | `@TestPropertySource(locations = ...)` |
| Inline properties | `@TestPropertySource(properties = ...)` or `@SpringBootTest(properties = ...)` |
| Bean overrides | `@MockitoBean`, `@SpyBean` |

→ **Any difference = new context**

---

## Context Cache Killers

| Pattern | Reason |
|---|---|
| `@DirtiesContext` | Destroys the context — forces cold start |
| `@MockitoBean` | Replaces a bean → different cache key |
| `@SpyBean` | Wraps a bean → different cache key |
| `@ActiveProfiles("test")` | Adds a profile → different key |
| `@TestPropertySource(properties = "x=1")` | Extra property → different key |
| `@SpringBootTest(properties = "x=1")` | Extra property → different key |

These are fine **in isolation** — the problem is **using different ones** across test classes.

---

## Spotting Context Restarts in the Logs

Enable context cache logging:

```yaml
# src/test/resources/application.yml
logging:
  level:
    org.springframework.test.context.cache: DEBUG
```

Look for these log lines:

```
[INFO]  Spring  Started Application in 14.3 seconds
[DEBUG] Spring  Retrieved [ContextKey] from cache
[DEBUG] Spring  Storing ApplicationContext in cache
[DEBUG] Spring  Spring TestContext Framework cache statistics:
        [DefaultContextCache@... size = 3, maxSize = 32, ...]
```

Multiple `"Started Application"` entries = multiple contexts.

---

## Analyzing with Spring Test Profiler

Spring Boot 3.4+ ships a built-in **Spring Test Profiler**:

```yaml
# src/test/resources/application.yml
spring:
  test:
    context:
      failure-threshold: 1

logging:
  level:
    org.springframework.test.context.cache: DEBUG
```

Or via system property when running tests:

```bash
./mvnw test -Dspring.test.context.cache.maxSize=1
```

The profiler prints a summary at the end of the build:

```
SpringTestContextProfiler: [contexts: 3, hits: 12, misses: 3, ...]
```

---

## Experiment: 5 Context Cache Killers

The `experiment` package contains five `ContextCacheKiller*IT` tests:

**Killer #1 — `@DirtiesContext`**
```java
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
```
Destroys and recreates the context **before every single test method**.

**Killer #2 — `@MockitoBean`**
```java
@MockitoBean
OpenLibraryApiClient openLibraryApiClient;
```
Bean definition changes → Spring must create a **new context** with the mock.

---

## Experiment: More Context Cache Killers

**Killer #3 — `@ActiveProfiles` + `@MockitoBean` + `@TestPropertySource`**
```java
@ActiveProfiles("test")
@TestPropertySource(properties = "book.metadata.api.timeout=5")
@MockitoBean BookService bookService;
```
Three cache-key changes in one class → guaranteed unique context.

**Killer #4 — Different `@ActiveProfiles` + different `@MockitoBean`**
Same profile as Killer #3 but a different mock target → **still a different key**.

**Killer #5 — `@TestPropertySource` alone**
```java
@TestPropertySource(properties = "book.metadata.api.timeout=10")
```
A single extra property is enough to break caching.

---

## The Solution: SharedIntegrationTestBase

```java
@SpringBootTest
@Import(LocalDevTestcontainerConfig.class)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
public abstract class SharedIntegrationTestBase {
  // Subclasses @Autowired any bean — all share one cached context
}
```

All integration tests extend it — **zero annotation duplication**:

```java
class BookControllerIT extends SharedIntegrationTestBase {

  @Autowired private MockMvc mockMvc;
  @Autowired private BookRepository bookRepository;

  @AfterEach
  void cleanUp() { bookRepository.deleteAll(); }

  @Test
  void shouldReturnBooks() throws Exception { ... }
}
```

---

## Rules for Subclasses

To keep the single cached context:

1. **No `@DirtiesContext`** — use `@Transactional` or `@Sql` for data isolation
2. **No `@MockitoBean` / `@SpyBean`** — use WireMock stubs instead of mocking HTTP clients
3. **No extra `@TestPropertySource`** — all property overrides go into `application.yml`
4. **No different `@ActiveProfiles`** — pick one profile for all integration tests
5. **No extra `@SpringBootTest(properties = ...)` per class**

> If you genuinely need a different context (e.g. disabled security, different DB),
> create a **second base class** rather than ad-hoc annotations.

---

## Real-World Example: Scout24

Scout24 (German real-estate platform, ~200 engineers) ran into this pattern at scale:

**The symptoms:**
- CI build: 45 minutes for a moderate-sized service
- Developers stopped running the full suite locally
- Flaky tests caused by message-queue event "stealing" between test classes

**The root cause:**
- 12 unique `@SpringBootTest` configurations across 40 test classes
- `@DirtiesContext` sprinkled to "fix" ordering issues
- `@MockitoBean` on service classes for convenience

---

## Scout24: The Solution

**Step 1 — Audit unique contexts**
Enable `logging.level.org.springframework.test.context.cache=DEBUG`
→ Found 12 distinct context keys

**Step 2 — Consolidate to a shared base class**
Remove `@DirtiesContext` and `@MockitoBean` from integration tests.
Use WireMock stubs for all external HTTP calls.

**Step 3 — Fix test data isolation**
- `@Transactional` for MockMvc tests (automatic rollback)
- `@AfterEach deleteAll()` for WebTestClient tests

**Result:** 12 contexts → 2 contexts, build time cut from **45 min → 12 min**

---

## Single Context: Trade-Offs

**Advantages:**
- Massive build time reduction
- Simpler test setup — no per-class annotation juggling
- Consistent behaviour — same beans, same config across all tests

**Disadvantages / watch-outs:**
- Shared state in beans with caches or static fields can cause test pollution
- All tests must tolerate the same WireMock stubs (use `@BeforeEach` reset)
- Some corner cases genuinely need a different context (e.g. security off)

**Not a replacement for sliced tests:**

> `@WebMvcTest` and `@DataJpaTest` should still cover 80 % of cases.
> Reserve `@SpringBootTest` for key integration paths.

---

## Why Sliced Testing Still Matters

```
                    Speed   Focus   Context Cost
@WebMvcTest          ✅✅✅   ✅✅✅     cheap slice
@DataJpaTest         ✅✅✅   ✅✅✅     cheap slice
@SpringBootTest      ✅       ✅       expensive — cache it!
```

- `@WebMvcTest`: 50 tests → starts in seconds, pinpoints web layer bugs
- `@DataJpaTest`: catches query and schema issues early
- `@SpringBootTest`: validates **wiring** — keep to key flows, not edge cases

**Anti-pattern:** replacing sliced tests with `@SpringBootTest` + `@MockitoBean`
→ slower suite AND breaks context caching

---

# Time For Some Exercises
## Lab 6

- Work with the same repository as in Labs 1–5
- Navigate to the `labs/lab-6` folder and complete the tasks in the `README`
- **Exercise 1**: Run the `ContextCacheKiller*IT` tests, count contexts in the logs, and explain what breaks caching in each class
- **Exercise 2**: Create (or inspect) `SharedIntegrationTestBase` and refactor the killer tests to extend it — verify that only **one context** starts
- Time boxed: until the end of the session
