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

- Data JPA Test

---

![bg left:33%](assets/generated/lab-4.jpg)

# Lab 4

## Integration Testing Part I

### Testing With a Complete Application Context

---

![bg right:25%](assets/evolve2.jpg)


## Enriching the Application

- **OpenLibrary API client** (`WebClient`) fetches book metadata from a remote API
- **Book entity** gains `description` and `thumbnailUrl` columns (Flyway `V002`)
- **HTTP on startup**: `CommandLineRunner` pre-fetches metadata for 3 ISBNs on every context start
- **Security**: endpoints protected by roles — imagine an OAuth 2 resource server

---

<!-- _class: section -->

# Starting Everything
## Writing Tests Against a Complete Application Context

![bg right:33%](assets/generated/full.jpg)

---

<!--

Notes:

-->

## The Default Integration Test

![](assets/generated/spring-boot-test-setup.png)

---

## Starting the Entire Context

- Provide external infrastructure with [Testcontainers](https://testcontainers.com/)
- Start Tomcat with: `@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)`
- Consider WireMock/MockServer for stubbing external HTTP services
- Test controller endpoints via: `MockMvc`, `WebTestClient`, `TestRestTemplate`

---

## Challenges: Starting a Full Context

1. **HTTP calls during context initialisation** → external API unavailable in CI/offline
2. **Infrastructure dependencies** → databases, caches, message brokers
3. **Security** → OAuth 2 tokens, role-protected endpoints
4. **Data preparation & cleanup** → consistent, isolated state between tests

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
- Tests become **non-deterministic** — dependent on external state and sample data
- Solution: stub the HTTP calls **before** the Spring context finishes starting

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

## HTTP Communication During Tests

- Unreliable when performing real HTTP calls during tests
- Sample data — what if the remote API changes its response?
- Authentication — real API keys in CI pipelines?
- Cleanup — data written to external systems
- No airplane-mode testing possible
- Solution: **stub the HTTP responses** for the remote system

---

## Why Offline / Airplane Mode Matters

- Tests must pass **anywhere**: laptop, CI/CD pipeline, air-gapped environments
- Real network calls make tests:
  - **Slow** — latency accumulates across a large suite
  - **Flaky** — rate limits, API downtime, responses that change over time
  - **Insecure** — credentials leak into logs, data written to external systems
- **Rule:** no test should require an outbound network connection

---

![w:1200 h:500](assets/wiremock-usage.svg)

---

## Introducing WireMock

- In-memory (or container) Jetty to stub HTTP responses
- Simulate failures, slow responses, etc.
- Stateful setups possible (scenarios): first request fails, then succeeds
- Alternatives: MockServer, MockWebServer, etc.

```java
wireMockServer.stubFor(
  get("/isbn/" + isbn)
    .willReturn(aResponse()
      .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
      .withBodyFile(isbn + "-success.json"))
);
```

---

## WireMock: Advanced Features

**Stateful scenarios** — simulate retry / eventual consistency

```java
wireMockServer.stubFor(get("/isbn/123")
  .inScenario("retry").whenScenarioStateIs(STARTED)
  .willReturn(serverError())
  .willSetStateTo("recovered"));

wireMockServer.stubFor(get("/isbn/123")
  .inScenario("retry").whenScenarioStateIs("recovered")
  .willReturn(ok().withBodyFile("123-success.json")));
```

**Response templating** — inject request values into the response body

```java
.willReturn(aResponse()
  .withBodyFile("book-template.json")
  .withTransformers("response-template"))
```

**Proxying & Recording** — record real API responses once, replay offline

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

- `@ServiceConnection` auto-configures the datasource — no manual URL overrides needed
- Declare `static` containers to share across tests in a class → faster suites
- Same image version as production: eliminates "works on my machine" surprises

---

## Challenge 3: Security

Imagine the application is an **OAuth 2 Resource Server** — every request must carry a JWT

- Don't spin up a real authorisation server in tests
- Spring Security Test provides mock contexts instead

```java
// MockMvc: inject a mock security context — no real token exchange
mockMvc.perform(get("/api/books/1")
    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
  .andExpect(status().isOk());

// Annotation shortcut
@WithMockUser(roles = "USER")
void shouldReturnBook() { ... }

// WebTestClient / TestRestTemplate: pass real Basic Auth credentials
.headers(h -> h.setBasicAuth("user", "user"))
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

## Wrap-Up: Day 1

- **Sliced testing** (`@WebMvcTest`, `@DataJpaTest`) → fast, focused, outer layers in isolation
- **Integration testing** (`@SpringBootTest`) → validates the full application wiring
- WireMock + Testcontainers = **offline, deterministic** full-context tests
- `ContextInitializer` solves the HTTP-on-startup problem
- Tomorrow: MockMvc vs WebTestClient in depth, context customisation, and exercises

---

<!-- _class: section -->

# See You Tomorrow!

## Day 2 starts at 09:00

_No lab exercise today — Lab 4 content feeds directly into tomorrow's exercises_
