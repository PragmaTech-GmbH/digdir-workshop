# Lab 5: Full-Stack Integration Testing with MockMvc and WebTestClient

## Learning Objectives

- Understand the difference between MockMvc (mock web environment) and WebTestClient (real embedded server)
- Write full `@SpringBootTest` integration tests that cover the entire application stack
- Handle authentication and authorization in integration tests
- Manage test data isolation with `@Transactional` and manual `@AfterEach` cleanup
- Use WireMock stubs for external HTTP dependencies within Spring integration tests

## Key Concepts

### MockMvc vs. WebTestClient

| Feature | MockMvc | WebTestClient |
|---|---|---|
| Web environment | `MOCK` (no real server) | `RANDOM_PORT` (real embedded server) |
| Request thread | Same thread as the test | Different server thread |
| `@Transactional` rollback | Works automatically | Does NOT work |
| Authentication | `@WithMockUser` | Real HTTP Basic Auth headers |
| Speed | Faster (no network I/O) | Slightly slower |

### MockMvc + `@Transactional` Pattern

MockMvc dispatches requests through the `DispatcherServlet` in the **same thread** as the test. When `@Transactional` is present on the test class, every test method runs inside a transaction that rolls back automatically at the end — no cleanup code needed.

### WebTestClient + `@AfterEach` Pattern

WebTestClient sends real HTTP requests to the embedded server. Those requests are handled in a **different server thread**, so the server commits its transaction independently. `@Transactional` on the test class has no effect on what the server commits. Manual cleanup (e.g., `bookRepository.deleteAll()`) in `@AfterEach` is required.

## Exercises

### Exercise 1: Integration Testing with MockMvc

Write a full integration test using `@SpringBootTest` with the default MOCK web environment.

**Tasks:**
1. Open `Exercise1MockMvcIntegrationTest.java` in the `exercises` package
2. Implement `shouldCreateAndRetrieveBookWhenUsingMockMvc`:
   - Inject `OpenLibraryApiStub` and call `stubForSuccessfulBookResponse(isbn)`
   - POST to `/api/books` with a valid JSON body and `@WithMockUser(roles = "USER")`
   - Assert 201 Created and the presence of a `Location` header
   - GET the URL from the `Location` header and assert the returned book fields
3. Implement `shouldReturnAllBooksWhenUsingMockMvc`:
   - Pre-populate the database via `BookRepository`
   - GET `/api/books` and assert 200 OK with a JSON array
4. Implement `shouldRejectInvalidBookCreationRequest`:
   - POST an invalid body (missing fields or wrong ISBN format) and assert 400 Bad Request

**Tips:**
- ISBN must be in the format `"978-XXXXXXXXXX"` (dashes required by `BookCreationRequest`)
- `GET /api/books` is public; `GET /api/books/{id}` and `POST /api/books` require `USER` role
- Use `MockMvcRequestBuilders.post(...)` and `MockMvcResultMatchers.jsonPath(...)`
- Inject `WireMockServer` as a bean or use the provided `OpenLibraryApiStub` wrapper

**Observe:** Because `@Transactional` is on the class and MockMvc runs in the same thread, all database changes roll back automatically after each test — no `@AfterEach` cleanup needed.

**File:** `exercises/Exercise1MockMvcIntegrationTest.java`
**Solution:** `solutions/Solution1MockMvcIntegrationTest.java`

---

### Exercise 2: Integration Testing with WebTestClient

Write the same round-trip tests using `@SpringBootTest(webEnvironment = RANDOM_PORT)` and WebTestClient.

**Tasks:**
1. Open `Exercise2WebTestClientIntegrationTest.java` in the `exercises` package
2. Add an `@AfterEach` method that calls `bookRepository.deleteAll()` to clean up committed data
3. Implement `shouldCreateAndRetrieveBookWhenUsingWebTestClient`:
   - Stub WireMock for the ISBN
   - POST with `.headers(h -> h.setBasicAuth("user", "user"))` and `.bodyValue(requestBody)`
   - Extract the `Location` URI via `.returnResult(Void.class).getResponseHeaders().getLocation()`
   - GET the location path with basic auth and assert the book fields
4. Implement `shouldReturnAllBooksWhenUsingWebTestClient`:
   - Insert books via `BookRepository`, GET `/api/books`, assert a JSON array
5. Implement `shouldRejectUnauthorizedAccessToProtectedEndpoint`:
   - GET `/api/books/1` without credentials and assert 401 Unauthorized

**Tips:**
- Use `.headers(h -> h.setBasicAuth("admin", "admin"))` for `ADMIN` role endpoints
- `GET /api/books/{id}` requires `USER` role; without credentials the server returns 401

**Observe:** `@Transactional` on the test class does NOT prevent the server from committing changes. The `@AfterEach` cleanup is essential to keep tests independent.

**File:** `exercises/Exercise2WebTestClientIntegrationTest.java`
**Solution:** `solutions/Solution2WebTestClientIntegrationTest.java`

## How to Run

```bash
# Run all lab-5 tests
./mvnw test -pl labs/lab-5

# Run Exercise 1
./mvnw test -pl labs/lab-5 -Dtest="Exercise1MockMvcIntegrationTest"

# Run Exercise 2
./mvnw test -pl labs/lab-5 -Dtest="Exercise2WebTestClientIntegrationTest"

# Run solutions
./mvnw test -pl labs/lab-5 -Dtest="Solution1MockMvcIntegrationTest"
./mvnw test -pl labs/lab-5 -Dtest="Solution2WebTestClientIntegrationTest"
```
