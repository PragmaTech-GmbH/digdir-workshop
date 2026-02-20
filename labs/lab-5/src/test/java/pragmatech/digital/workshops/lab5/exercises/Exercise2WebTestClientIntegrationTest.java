package pragmatech.digital.workshops.lab5.exercises;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import pragmatech.digital.workshops.lab5.LocalDevTestcontainerConfig;
import pragmatech.digital.workshops.lab5.config.WireMockContextInitializer;
import pragmatech.digital.workshops.lab5.repository.BookRepository;

/**
 * Exercise 2: Integration Testing with WebTestClient
 * <p>
 * In this exercise, you will write the same round-trip test but using
 * {@code @SpringBootTest(webEnvironment = RANDOM_PORT)} with WebTestClient.
 * <p>
 * Key differences from MockMvc:
 * <ul>
 *   <li>WebTestClient makes REAL HTTP requests to the embedded server</li>
 *   <li>Requests execute in a DIFFERENT thread from the test</li>
 *   <li>{@code @Transactional} on the test does NOT roll back server-side changes</li>
 *   <li>Use real HTTP Basic Auth headers instead of {@code @WithMockUser}</li>
 *   <li>You need {@code @AfterEach} cleanup since data is committed</li>
 * </ul>
 * <p>
 * Hints:
 * <ul>
 *   <li>Use {@code .headers(h -> h.setBasicAuth("user", "user"))} for USER role</li>
 *   <li>Use {@code .headers(h -> h.setBasicAuth("admin", "admin"))} for ADMIN role</li>
 *   <li>POST returns a Location header - use {@code .returnResult(Void.class)} to access it</li>
 *   <li>Clean up created books in {@code @AfterEach} using BookRepository</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(LocalDevTestcontainerConfig.class)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
@AutoConfigureWebTestClient
class Exercise2WebTestClientIntegrationTest {

  @Autowired
  private WebTestClient webTestClient;

  @Autowired
  private BookRepository bookRepository;

  // TODO: Add an @AfterEach method to clean up created books
  // Since WebTestClient commits transactions, data persists after each test.
  // Use bookRepository.deleteAll() or delete specific books.

  @Test
  void shouldCreateAndRetrieveBookWhenUsingWebTestClient() {
    // TODO:
    // 1. Add a WireMock stub for the ISBN you will use (inject WireMockServer)
    //
    // 2. Perform a POST to /api/books with a valid JSON body:
    //    - Use webTestClient.post().uri("/api/books")
    //    - Set content type and basic auth headers
    //    - Use .bodyValue() with the JSON string
    //    - Use .exchange() to send the request
    //
    // 3. Assert the response status is 201 Created
    //
    // 4. Extract the Location header from the response
    //    Hint: Use .returnResult(Void.class).getResponseHeaders().getLocation()
    //
    // 5. Perform a GET to the Location URL:
    //    - Use webTestClient.get().uri(locationUri)
    //    - Include basic auth headers (GET /api/books/{id} requires USER role)
    //
    // 6. Assert the response contains the expected book data
    //    Use .expectBody().jsonPath("$.title").isEqualTo(...)
    //
    // Observe: The request runs in a different thread.
    // @Transactional on the test class would NOT roll back these changes.
  }

  @Test
  void shouldReturnAllBooksWhenUsingWebTestClient() {
    // TODO:
    // 1. Perform a GET to /api/books (publicly accessible, no auth needed)
    //    Use webTestClient.get().uri("/api/books")
    //
    // 2. Assert the response status is 200 OK
    //
    // 3. Assert the response body is a JSON array
  }

  @Test
  void shouldRejectUnauthorizedAccessToProtectedEndpoint() {
    // TODO:
    // 1. Perform a GET to /api/books/1 WITHOUT providing auth headers
    //
    // 2. Assert the response status is 401 Unauthorized
    //
    // Note: Unlike MockMvc with @WithMockUser, WebTestClient requires
    // real credentials because it makes actual HTTP requests.
  }
}
