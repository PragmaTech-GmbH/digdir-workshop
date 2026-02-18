---
marp: true
theme: pragmatech
---

![bg](./assets/digdir-cover.jpg)

---

<!-- _class: title -->
![bg h:500 left:33%](assets/generated/demystify.png)

# Testing Spring Boot Applications Demystified

## Full-Day Workshop

_Digdir Workshop 02.03.2026_

Philip Riecks - [PragmaTech GmbH](https://pragmatech.digital/) - [@rieckpil](https://x.com/rieckpil)

---

<!-- header: 'Testing Spring Boot Applications Demystified Workshop @ Digdir 02.03.2026' -->
<!-- footer: '![w:32 h:32](assets/generated/logo.webp)' -->

## Quick Recap: Day 1

- Sliced testing — `@WebMvcTest`, `@DataJpaTest`, `@JsonTest` — fast, focused, outer layers
- `@SpringBootTest` starts the full application context
- WireMock via `ContextInitializer` solves HTTP calls during context startup
- Testcontainers provides real infrastructure (PostgreSQL)
- Cleanup strategy depends on which HTTP client you use

Today: **`@SpringBootTest` modes in depth**, test HTTP clients, context customisation, exercises

---

![bg left:33%](assets/generated/lab-5.jpg)

# Lab 5

## Integration Testing Part II

### MockMvc, WebTestClient & Context Customisation

---

## @SpringBootTest Web Environments

| Mode | Web server | Real HTTP |
|---|---|---|
| `MOCK` *(default)* | Mock servlet container | ❌ |
| `RANDOM_PORT` | Real embedded Tomcat | ✅ |
| `DEFINED_PORT` | Real embedded Tomcat | ✅ |
| `NONE` | No servlet | ❌ |

```java
@SpringBootTest                                              // MOCK (default)
@SpringBootTest(webEnvironment = RANDOM_PORT)               // real HTTP
@SpringBootTest(webEnvironment = DEFINED_PORT)              // real HTTP, fixed port
@SpringBootTest(webEnvironment = NONE)                      // service / batch tests
```

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

**`@TestConfiguration`** — contribute additional beans without replacing the whole context:

```java
@TestConfiguration
class FakeClientConfig {
  @Bean
  OpenLibraryApiClient openLibraryApiClient() { return new NoOpClient(); }
}
```

⚠️ `@MockitoBean` forces a **new application context** — avoid in large test suites

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
