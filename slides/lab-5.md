---
marp: true
theme: pragmatech
---

![bg](./assets/digdir-cover.jpg)

---

<!-- _class: title -->
![bg h:500 left:33%](assets/generated/demystify.png)

# Testing Spring Boot Applications Demystified

## Lab 5

_Digdir Workshop 03.03.2026_

Philip Riecks - [PragmaTech GmbH](https://pragmatech.digital/) - [@rieckpil](https://x.com/rieckpil)

---

<!-- header: 'Testing Spring Boot Applications Demystified Workshop @ Digdir 03.03.2026' -->
<!-- footer: '![w:32 h:32](assets/generated/logo.webp)' -->

## Day 1 Recap — Labs 1 & 2

**Lab 1 · JUnit 5 Fundamentals**

- Test pyramid: unit → sliced → integration → E2E — write most tests at the bottom
- JUnit 5 lifecycle: `@BeforeEach`, `@AfterEach`, `@Nested`, `@ParameterizedTest`, `@Tag`
- AssertJ for fluent, readable assertions; Mockito for mocking dependencies
- Maven Surefire runs `*Test.java` (unit); Failsafe runs `*IT.java` (integration)

**Lab 2 · Web Layer: `@WebMvcTest`**

- Loads only controllers + filters + security config — no database, no service beans
- MockMvc: `perform()` → `andExpect()` — verifies status, JSON paths, headers, errors
- `@MockitoBean` stubs the service layer without starting a full context
- Spring Security: `@WithMockUser`, `.with(jwt())`, `.with(csrf())`

---

## Day 1 Recap — Labs 3 & 4

**Lab 3 · Persistence & HTTP Client Slices**

- `@DataJpaTest` — JPA layer only, `@Transactional` rollback by default
- Real Postgres via **Testcontainers** + `@ServiceConnection` — no more H2 surprises
- `@JsonTest` → Jackson serialization in isolation; `@RestClientTest` → stubs `RestClient`/`RestTemplate` with `MockRestServiceServer`

**Lab 4 · Full Context: `@SpringBootTest`**

- Boots the entire `ApplicationContext` — closest to production, slowest to start
- Four challenges: external HTTP on startup · infrastructure deps · security · test data cleanup
- **WireMock** stubs outbound HTTP at the socket level — offline, deterministic
- **Testcontainers** manages a real Postgres container via the JVM test lifecycle
- Cleanup: `@Transactional` for MockMvc (rollback); `@AfterEach deleteAll` for WebTestClient

---

## Where We Left Off — End of Lab 4

Lab 4 introduced `@SpringBootTest` and solved its four main challenges. Here is what you have so far:

```java
@SpringBootTest
@Import(LocalDevTestcontainerConfig.class)          // ✅ real Postgres via Testcontainers
@ContextConfiguration(initializers =
    WireMockContextInitializer.class)               // ✅ WireMock stubs before beans init
@Transactional                                       // ✅ auto-rollback with MockMvc
class BookControllerIT { ... }
```

**What we haven't covered yet:**

- The four `@SpringBootTest` web environment modes (`MOCK`, `RANDOM_PORT`, `DEFINED_PORT`, `NONE`)
- When to use **MockMvc** vs **WebTestClient** vs **TestRestTemplate**
- How to customise the context per test (properties, beans, profiles)
- How all of this affects **context caching** (Lab 6)

**Today picks up exactly here.** ↓

---

![bg left:33%](assets/lab-5.jpg)

# Lab 5

## Integration Testing Part II

### MockMvc, WebTestClient & Context Customisation

---



## Challenge: Security

- Actual test setup depends on the used authentication mechanism:
  - **OAuth2 Resource Server** - every request must carry a valid and signed JWT
  - **Basic Auth** - provide test users
  - **API Keys** - provide test keys

```java
// MockMvc: inject a mock security context — no real token exchange
mockMvc.perform(get("/api/books/1")
    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
  .andExpect(status().isOk());

// Annotation shortcut
@WithMockUser(roles = "USER")
void shouldReturnBook() { ... }
```

---

![h:300 center](assets/lab-4-mock-variant.png)

---

![w:900 center](assets/lab-4-random-port-variant.png)


---

## Challenge: Data Preparation & Cleanup

**Preparing data:**

```java
@BeforeEach
void setUp() {
  openLibraryApiStub.stubForSuccessfulBookResponse(VALID_ISBN); // WireMock
  bookRepository.save(new Book(...));                           // programmatic
}
```

**Cleanup — strategy depends on the HTTP client:**

| Client | Thread | Cleanup |
|---|---|---|
| MockMvc | **Same** as test | `@Transactional` → automatic rollback |
| WebTestClient | **Different** (server thread) | `@AfterEach` → `repository.deleteAll()` |
| TestRestTemplate | **Different** (server thread) | `@AfterEach` → `repository.deleteAll()` |

---

## MOCK vs RANDOM_PORT — When to Use Which

| | `MOCK` + MockMvc | `RANDOM_PORT` + WebTestClient |
|---|---|---|
| **Speed** | Faster (no server startup overhead) | Slightly slower |
| **Isolation** | `@Transactional` → auto-rollback | Manual cleanup (`@AfterEach`) |
| **Auth** | `@WithMockUser`, `.with(jwt())` | Real `Authorization` header |
| **HTTP filters** | Spring filters ✅, Tomcat filters ❌ | Everything ✅ |
| **Async/streaming** | Limited | Full support (SSE, WebSocket) |
| **Best for** | Controller logic, validation, security rules | End-to-end flows, real HTTP behaviour, filters at all levels |

**Rule of thumb:** start with `MOCK` — only switch to `RANDOM_PORT` when you specifically need real HTTP behaviour (e.g. testing Tomcat filters, streaming responses, or matching how a real client behaves).

---

## Test HTTP Clients at a Glance

| Client | Environment | Auth | Rollback |
|---|---|---|---|
| `MockMvc` | `MOCK` | `@WithMockUser` | ✅ `@Transactional` |
| `WebTestClient` | `RANDOM_PORT` | Real headers | ❌ manual |
| `TestRestTemplate` | `RANDOM_PORT` | Real headers | ❌ manual |

```java
// Auto-configured when @AutoConfigureMockMvc is present
@Autowired MockMvc mockMvc;

// Auto-configured when webEnvironment = RANDOM_PORT
@Autowired WebTestClient webTestClient;

// Auto-configured when webEnvironment = RANDOM_PORT
@Autowired TestRestTemplate restTemplate;
```

---

## MockMvc — Same Thread, Automatic Rollback

```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookControllerMockMvcTest {

  @Autowired private MockMvc mockMvc;

  @Test
  @WithMockUser(roles = "USER")
  void shouldCreateAndRetrieveBook() throws Exception {
    MvcResult result = mockMvc.perform(post("/api/books")
        .contentType(APPLICATION_JSON)
        .content(requestBody))
      .andExpect(status().isCreated())
      .andExpect(header().exists("Location"))
      .andReturn();

    String location = result.getResponse().getHeader("Location");

    mockMvc.perform(get(location))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("AVAILABLE"));
  }
}
```

---

## WebTestClient — Different Thread, Real HTTP

```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(LocalDevTestcontainerConfig.class)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
class BookControllerWebTestClientTest {

  @Autowired private WebTestClient webTestClient;
  @Autowired private BookRepository bookRepository;

  @AfterEach
  void cleanUp() { bookRepository.deleteAll(); }

  @Test
  void shouldCreateAndRetrieveBook() {
    URI location = webTestClient.post().uri("/api/books")
      .contentType(APPLICATION_JSON)
      .headers(h -> h.setBasicAuth("user", "user"))
      .bodyValue(requestBody)
      .exchange()
      .expectStatus().isCreated()
      .returnResult(Void.class).getResponseHeaders().getLocation();

    webTestClient.get().uri(location.getPath())
      .headers(h -> h.setBasicAuth("user", "user"))
      .exchange()
      .expectStatus().isOk()
      .expectBody().jsonPath("$.status").isEqualTo("AVAILABLE");
  }
}
```

---

## MockMvc vs WebTestClient — The Boundary

```
@SpringBootTest(MOCK)
┌──────────────────────────────────────────────────┐
│  Test thread                                     │
│  ────────────────────────────────────────────→   │
│  MockMvc → DispatcherServlet → Controller        │
│                                    ↓             │
│                          Service → Repository    │
│                                                  │
│  @Transactional wraps the whole call chain ✅    │
└──────────────────────────────────────────────────┘

@SpringBootTest(RANDOM_PORT)
┌────────────────┐    TCP    ┌─────────────────────┐
│  Test thread   │ ───────→  │  Server thread       │
│  WebTestClient │           │  Controller          │
│                │           │    ↓                 │
│                │           │  Service → DB commit │
└────────────────┘           └─────────────────────┘
  @Transactional has NO effect on server-side changes ❌
```

---

## Customising the Test Context: Properties

**Inline on the annotation** — highest priority, scoped to one test class:

```java
@SpringBootTest(properties = {
  "spring.flyway.enabled=false",
  "book.metadata.api.timeout=1"
})
```

**`@TestPropertySource`** — reusable, can point to a file:

```java
@TestPropertySource(locations = "classpath:test-overrides.properties")
@TestPropertySource(properties = "book.metadata.api.url=http://localhost:9090")
```

**`src/test/resources/application.yml`** — applies to all tests:

```yaml
spring:
  jpa:
    properties:
      hibernate:
        format_sql: true
```

---

## Customising the Test Context: Profiles

```java
@SpringBootTest
@ActiveProfiles("integration")
class BookControllerIT { ... }
```

`src/test/resources/application-integration.yml`:

```yaml
logging:
  level:
    org.hibernate.SQL: DEBUG
    com.zaxxer.hikari: DEBUG
book:
  metadata:
    api:
      url: http://localhost:${wiremock.server.port}
```

- Activate entire configuration sections per environment
- Useful for switching between H2 and real DB, enabling extra logging, or toggling feature flags

---

## Customising the Test Context: Bean Replacement

**`@MockitoBean`** — replace a bean with a Mockito mock:

```java
@SpringBootTest
class BookServiceIntegrationTest {

  @MockitoBean
  private OpenLibraryApiClient openLibraryApiClient;

  @Test
  void shouldCreateBookWithStubbedMetadata() {
    when(openLibraryApiClient.getBookByIsbn(any()))
      .thenReturn(new BookMetadataResponse(...));
    ...
  }
}
```

⚠️ `@MockitoBean` forces a **new application context** — avoid in large test suites

---

## Customising the Test Context: Custom Beans

**`@TestConfiguration` + `@Import`** — contribute or replace beans without touching production config:

```java
@TestConfiguration
class FakeMailConfig {

  @Bean
  JavaMailSender javaMailSender() {
    return new JavaMailSenderImpl(); // no-op sender for tests
  }
}
```

```java
@SpringBootTest
@Import(FakeMailConfig.class)   // ← wires the test bean into this context only
class BookNotificationServiceTest {

  @Autowired BookNotificationService bookNotificationService;
}
```

> Use `@TestConfiguration(proxyBeanMethods = false)` for faster startup — no CGLIB proxy needed when beans don't call each other.

---

## Customising the Test Context: Custom Beans (2)

**`@Primary`** — declare a test bean as the preferred candidate when multiple exist:

```java
@TestConfiguration
class FixedClockConfig {

  @Bean
  @Primary                              // ← wins over the production Clock bean
  Clock fixedClock() {
    return Clock.fixed(
      Instant.parse("2025-06-01T00:00:00Z"), ZoneOffset.UTC);
  }
}
```

**Reusable config via a shared base class:**

```java
@SpringBootTest
@Import({LocalDevTestcontainerConfig.class, FixedClockConfig.class})
abstract class SharedIntegrationTestBase { }

class LateReturnFeeIT extends SharedIntegrationTestBase {
  // inherits Postgres + fixed clock — no extra annotations needed
}
```

> `@Primary` is cleaner than `@MockitoBean` when you want a **real fake** (a hand-written stub) rather than a Mockito mock — and it doesn't break context caching.

---

## General Questions

> *"If I have a `@SpringBootTest` that covers everything, why bother with `@WebMvcTest`?"*

- **Speed**: Sliced contexts start in < 1 s vs 10–30 s for a full context
- **Corner cases**: reproducing a specific validation error or HTTP status via `@SpringBootTest` often requires a `@MockitoBean` → **that creates a new context**
- **Focus**: sliced tests fail closer to the root cause — easier to debug
- **Feedback loop**: run 50 `@WebMvcTest` tests in the time one `@SpringBootTest` starts

**Rule of thumb:**
- Extensive sliced testing for the **web** and **persistence** layers
- `@SpringBootTest` for key **integration paths** — the happy path and critical flows
- Never `@MockitoBean` your way through a `@SpringBootTest` — use sliced testing instead

---

# Time For Some Exercises
## Lab 5

- Work with the same repository as in Lab 1–4
- Navigate to the `labs/lab-5` folder and complete the tasks in the `README`
- **Exercise 1**: Implement an integration test with `MockMvc` (MOCK environment, `@Transactional`)
- **Exercise 2**: Implement the same scenario with `WebTestClient` (RANDOM_PORT, manual cleanup)
- Time boxed: until the end of the session
