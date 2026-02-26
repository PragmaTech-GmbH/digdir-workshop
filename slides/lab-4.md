---
marp: true
theme: pragmatech
---

![bg](./assets/digdir-cover.jpg)

---


<!-- _class: title -->
![bg h:500 left:33%](assets/generated/demystify.png)

# Testing Spring Boot Applications Demystified

## Lab 4

_Digdir Workshop 02.03.2026_

Philip Riecks - [PragmaTech GmbH](https://pragmatech.digital/) - [@rieckpil](https://x.com/rieckpil)

---

<!-- header: 'Testing Spring Boot Applications Demystified Workshop @ Digdir 02.03.2026' -->
<!-- footer: '![w:32 h:32](assets/generated/logo.webp)' -->

## Discuss Exercises from Lab 3

- Data JPA Test to test native SQL query

---

![bg left:33%](assets/generated/lab-4.jpg)

# Lab 4

## Integration Testing Part I

### Testing Against the Entire Application Context

---

![bg right:25%](assets/evolve2.jpg)


## Enriching the Application

- **OpenLibrary API client** (`WebClient`) fetches book metadata from a remote API
- **Book entity** gains `description` and `thumbnailUrl` columns (Flyway `V002`)
- **HTTP on startup**: `CommandLineRunner` pre-fetches metadata for 3 ISBNs on every context start
- **Security**: endpoints protected by roles - imagine an OAuth2 resource server

---

<!-- _class: section -->

# Starting Everything
## Booting the Entire `ApplicationContext`

![bg right:33%](assets/generated/full.jpg)

---

<!--

Notes:

-->

## The Default Integration Test

![](assets/generated/spring-boot-test-setup.png)

---


## Challenges: Starting a Full Context

1. **HTTP calls during context initialisation** → external API unavailable in CI/offline
2. **Infrastructure dependencies** → databases, caches, message brokers
3. **Security** → OAuth2, JWT, Basic Auth, role-protected endpoints
4. **Data preparation & cleanup** → consistent, isolated state between tests
5. **Speed** → keeping the build times reasonable

---

## Introducing: Microservice HTTP Communication

```java
public BookMetadataResponse getBookByIsbn(String isbn) {
  return webClient.get()
    .uri("/isbn/{isbn}", isbn)
    .retrieve()
    .bodyToMono(BookMetadataResponse.class)
    .block();
}
```

---

## Challenge 1: HTTP Calls During Context Init

```java
@Bean
public CommandLineRunner initializeBookMetadata() {
  return args -> {
    // Fires real HTTP to https://openlibrary.org on every context start
    openLibraryApiClient.getBookByIsbn("9780132350884");
    openLibraryApiClient.getBookByIsbn("9780201633610");
    openLibraryApiClient.getBookByIsbn("9780134757599");
  };
}
```

- Context fails to start when the remote API is **unreachable** (CI, airplane mode)
- Tests become **non-deterministic** - dependent on external state and sample data
- Solution: stub the HTTP calls **before** the Spring context finishes starting

---

## HTTP Communication During Tests

- Unreliable when performing real HTTP calls during tests
- Sample data - what if the remote API changes its response?
- Authentication - real API keys in CI pipelines?
- Cleanup - data written to external systems
- No airplane-mode testing possible
- Solution: **stub the HTTP responses** for the remote system

---

## Why Offline / Airplane Mode Matters

- Tests should pass **anywhere**: laptop, CI/CD pipeline, air-gapped environments
- Real network calls make tests:
  - **Slow** - latency accumulates across a large suite
  - **Flaky** - rate limits, API downtime, responses that change over time
  - **Insecure** - credentials leak into logs, data written to external systems
- **Rule:** no test should require an outbound network connection

---

![w:1200 h:700](assets/wiremock-usage.svg)

---

## Introducing WireMock

- In-memory (or container) Jetty to stub HTTP responses
- Simulate failures, slow responses, etc.
- Stateful setups possible (scenarios): first request fails, then succeeds
- Alternatives: MockServer, MockWebServer, etc.

```java
WireMockServer wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
wireMockServer.start();

wireMockServer.stubFor(
  get("/isbn/" + isbn)
    .willReturn(aResponse()
      .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
      .withBodyFile(isbn + "-success.json"))
);
```

---

## WireMock: Advanced Features

**Stateful scenarios** - simulate retry / eventual consistency

```java
wireMockServer.stubFor(get("/isbn/123")
  .inScenario("retry").whenScenarioStateIs(STARTED)
  .willReturn(serverError())
  .willSetStateTo("recovered"));

wireMockServer.stubFor(get("/isbn/123")
  .inScenario("retry").whenScenarioStateIs("recovered")
  .willReturn(ok().withBodyFile("123-success.json")));
```

---

**Response templating** - inject request values into the response body

```java
wireMockServer.stubFor(get(urlPathMatching("/users/.*"))
  .willReturn(aResponse()
    .withHeader("Content-Type", "application/json")
    .withBody(
        {
          "id": "{{request.pathSegments.[1]}}",
          "userAgent": "{{request.headers.User-Agent}}",
          "timestamp": "{{now format='yyyy-MM-dd'}}"
        }
       )
    .withTransformers("response-template")));
```

---

**Proxying & Recording** - record real API responses once, replay offline

```java
wireMockServer.startRecording(RecordSpec.forTarget("https://openlibrary.org/")
    .makeStubsPersistent(true)
    .build());

// ... make real requests ...

wireMockServer.stopRecording();
```

---

## Making Our Application Context Start

- Stubbing HTTP responses during the launch of our Spring Context
- Introducing a new concept: `ContextInitializer`

```java
WireMockServer wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
wireMockServer.start();

applicationContext.addApplicationListener(event -> {
  if (event instanceof ContextClosedEvent) {
    wireMockServer.stop();
  }
});

// Configure stubs before Spring beans initialise
new OpenLibraryApiStub(wireMockServer).stubForSuccessfulBookResponse("9780132350884");

TestPropertyValues.of(
  "book.metadata.api.url=http://localhost:" + wireMockServer.port()
).applyTo(applicationContext);
```

---

## Challenge 2: Infrastructure Dependencies

```java
@TestConfiguration
class PostgresTestcontainerConfig {

  @Bean
  @ServiceConnection
  PostgreSQLContainer<?> postgresContainer() {
    return new PostgreSQLContainer<>("postgres:16-alpine")
        .withInitScript("init-postgres.sql");
  }
}
```

- Provide external infrastructure dependencies (databases, caches, message brokers) via **Testcontainers**
- Declare `static` containers to share across tests in a class → faster suites
- Same image version as production: eliminates "works on my machine" surprises

---

## Challenge 3: Security

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

## Challenge 4: Data Preparation & Cleanup

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

## Wrap-Up: Day 1 — Lab 1 & Lab 2

**Lab 1 · JUnit 5 Fundamentals**

- Test pyramid: unit → sliced → integration → E2E
- JUnit 5 lifecycle: `@BeforeEach`, `@AfterEach`, `@Nested`, `@ParameterizedTest`
- AssertJ for fluent, readable assertions
- Maven Surefire (unit) vs. Failsafe (integration `*IT.java`) plugins

**Lab 2 · Web Layer: `@WebMvcTest`**

- Loads only controllers + security config — no database, no service beans
- MockMvc: `perform()` → `andExpect()` for request/response verification
- `@MockitoBean` stubs the service layer
- Spring Security: `@WithMockUser`, `.with(jwt())`, `.with(csrf())`
- Validates HTTP status, JSON paths, headers, and error responses

---

## Wrap-Up: Day 1 — Lab 3

**Persistence Layer: `@DataJpaTest`**

- Loads JPA layer only — no web, no security
- `@Transactional` by default → each test rolls back automatically
- Replace H2 with a real Postgres via **Testcontainers** + `@ServiceConnection`
- `TestEntityManager` for low-level entity manipulation
- `@Sql` for declarative test data loading
- Native queries (`to_tsvector`, `ts_rank`) must be tested against a real DB engine

**JSON & HTTP Client Slices**

- `@JsonTest` → verifies Jackson serialization / deserialization in isolation
- `@RestClientTest` → stubs outbound HTTP with `MockRestServiceServer`; only works with `RestClient` / `RestTemplate` — use WireMock for `WebClient`
- Both slices are faster and simpler than a full `@SpringBootTest`

---

## Wrap-Up: Day 1 — Lab 4

**Full Context: `@SpringBootTest`**

- Boots the entire `ApplicationContext` — closest to production
- Challenges: external HTTP on startup, infrastructure deps, security, test data
- **WireMock**: stubs outbound HTTP at the socket level — offline, deterministic
- **Testcontainers**: real Postgres container managed by the JVM test lifecycle
- **`ContextInitializer`**: registers WireMock stubs before beans are initialised
- **Spring Security Test**: `jwt()`, `@WithMockUser` — no real auth server needed

**Data Cleanup Strategy**

| Test Client | Cleanup |
|---|---|
| MockMvc | `@Transactional` → automatic rollback |
| WebTestClient / TestRestTemplate | `@AfterEach` deleteAll |

Tomorrow: full-context exercises, context caching, and test performance

---

<!-- _class: section -->

# See You Tomorrow!

## Day 2 starts at 09:00

_No lab exercise for Lab 4 - content feeds directly into tomorrow's exercises_
