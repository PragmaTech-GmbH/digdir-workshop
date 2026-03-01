package pragmatech.digital.workshops.lab5.solutions;

import java.net.URI;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;
import pragmatech.digital.workshops.lab5.LocalDevTestcontainerConfig;
import pragmatech.digital.workshops.lab5.config.OpenLibraryApiStub;
import pragmatech.digital.workshops.lab5.config.WireMockContextInitializer;
import pragmatech.digital.workshops.lab5.entity.Book;
import pragmatech.digital.workshops.lab5.repository.BookRepository;

/**
 * Solution for Exercise 2: Integration Testing with WebTestClient
 * <p>
 * This test uses {@code @SpringBootTest(webEnvironment = RANDOM_PORT)} which starts
 * a real embedded HTTP server. WebTestClient sends real HTTP requests to the server.
 * <p>
 * Key observations:
 * <ul>
 *   <li>WebTestClient sends requests over a real HTTP connection</li>
 *   <li>Requests are handled in a DIFFERENT thread from the test</li>
 *   <li>{@code @Transactional} on the test does NOT roll back server-side changes</li>
 *   <li>Real HTTP Basic Auth headers are needed (not {@code @WithMockUser})</li>
 *   <li>Manual cleanup is required in {@code @AfterEach}</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(LocalDevTestcontainerConfig.class)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
@AutoConfigureWebTestClient
class Solution2WebTestClientIntegrationTest {

  @Autowired
  private WebTestClient webTestClient;

  @Autowired
  private BookRepository bookRepository;

  @Autowired
  private OpenLibraryApiStub openLibraryApiStub;

  private static final String VALID_ISBN = "978-0134757599";

  @AfterEach
  void cleanUp() {
    // WebTestClient commits transactions in the server thread, so we must
    // clean up manually. Without this, data would persist across tests.
    this.bookRepository.deleteAll();
  }

  @Test
  @Transactional
  void shouldCreateAndRetrieveBookWhenUsingWebTestClient() {
    // Arrange
    String requestBody = """
      {
        "isbn": "%s",
        "title": "Effective Java",
        "author": "Joshua Bloch",
        "publishedDate": "2018-01-06"
      }
      """.formatted(VALID_ISBN);

    openLibraryApiStub.stubForSuccessfulBookResponse(VALID_ISBN);

    // Act - Create a book using POST with Basic Auth
    URI locationUri = webTestClient.post()
      .uri("/api/books")
      .contentType(MediaType.APPLICATION_JSON)
      .headers(headers -> headers.setBasicAuth("user", "user"))
      .bodyValue(requestBody)
      .exchange()
      .expectStatus().isCreated()
      .expectHeader().exists("Location")
      .returnResult(Void.class)
      .getResponseHeaders()
      .getLocation();

    // Act - Retrieve the created book using GET with Basic Auth
    // GET /api/books/{id} requires USER role
    webTestClient.get()
      .uri(locationUri.getPath())
      .accept(MediaType.APPLICATION_JSON)
      .headers(headers -> headers.setBasicAuth("user", "user"))
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .jsonPath("$.isbn").isEqualTo(VALID_ISBN)
      .jsonPath("$.title").isEqualTo("Effective Java")
      .jsonPath("$.author").isEqualTo("Joshua Bloch")
      .jsonPath("$.status").isEqualTo("AVAILABLE");

    // Note: The created book is committed to the database because WebTestClient
    // sends real HTTP requests that are processed in a separate server thread.
    // The @AfterEach method cleans it up.
  }

  @Test
  void shouldReturnAllBooksWhenUsingWebTestClient() {
    // GET /api/books is publicly accessible (permitAll)

    // this won't work when we use @Trannsactional because the test transaction is separate from the server thread's transaction.
    this.bookRepository.save(new Book("123-1234567890", "Book One", "Author A", LocalDate.now()));
    this.bookRepository.save(new Book("456-1234567890", "Book Two", "Author B", LocalDate.now()));

    this.webTestClient.get()
      .uri("/api/books")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .jsonPath("$").isArray()
      .jsonPath("$.size()").isEqualTo(2);
  }

  @Test
  void shouldRejectUnauthorizedAccessToProtectedEndpoint() {
    // GET /api/books/{id} requires USER role
    // Without credentials, the server should return 401
    this.webTestClient.get()
      .uri("/api/books/1")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isUnauthorized();
  }
}
