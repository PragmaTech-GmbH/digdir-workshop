package pragmatech.digital.workshops.lab5.experiment;

import java.net.URI;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import pragmatech.digital.workshops.lab5.LocalDevTestcontainerConfig;
import pragmatech.digital.workshops.lab5.config.OpenLibraryApiStub;
import pragmatech.digital.workshops.lab5.config.WireMockContextInitializer;
import pragmatech.digital.workshops.lab5.entity.Book;
import pragmatech.digital.workshops.lab5.repository.BookRepository;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Experiment: TestRestTemplate as a third integration testing option.
 * <p>
 * {@link TestRestTemplate} is the classic synchronous HTTP client for integration tests.
 * Like WebTestClient, it sends real HTTP requests to the embedded server, so:
 * <ul>
 *   <li>Requests run in a DIFFERENT thread from the test</li>
 *   <li>{@code @Transactional} does NOT roll back server-side changes</li>
 *   <li>Real HTTP Basic Auth is needed (not {@code @WithMockUser})</li>
 *   <li>Manual cleanup is required in {@code @AfterEach}</li>
 * </ul>
 * <p>
 * Comparison:
 * <ul>
 *   <li><strong>MockMvc</strong>: No real HTTP, same thread, transaction rollback works</li>
 *   <li><strong>WebTestClient</strong>: Real HTTP, fluent reactive-style API, good for streaming</li>
 *   <li><strong>TestRestTemplate</strong>: Real HTTP, classic RestTemplate-style API, simpler for basic cases</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(LocalDevTestcontainerConfig.class)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
@AutoConfigureTestRestTemplate
class TestRestTemplateDemo {

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private BookRepository bookRepository;

  @Autowired
  private OpenLibraryApiStub openLibraryApiStub;

  private static final String VALID_ISBN = "978-0201633610";

  @BeforeEach
  void setUp() {
    // Register a WireMock stub for the dashed ISBN format
    openLibraryApiStub.stubForSuccessfulBookResponse(VALID_ISBN);
  }

  @AfterEach
  void cleanUp() {
    // Like WebTestClient, TestRestTemplate commits transactions on the server,
    // so we must clean up created data manually.
    bookRepository.deleteAll();
  }

  @Test
  @DisplayName("TestRestTemplate: GET all books (public endpoint)")
  void shouldReturnAllBooksWithTestRestTemplate() {
    // GET /api/books is publicly accessible
    ResponseEntity<Book[]> response = restTemplate.getForEntity("/api/books", Book[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();

    System.out.println("  Retrieved " + response.getBody().length + " books via TestRestTemplate");
  }

  @Test
  @DisplayName("TestRestTemplate: POST to create and GET to retrieve a book")
  void shouldCreateAndRetrieveBookWithTestRestTemplate() {
    // Arrange
    String requestBody = """
      {
        "isbn": "%s",
        "title": "Design Patterns",
        "author": "Gang of Four",
        "publishedDate": "1994-10-31"
      }
      """.formatted(VALID_ISBN);

    // Act - Create a book using POST with Basic Auth
    // TestRestTemplate.withBasicAuth() returns a new instance with credentials.
    // We need HttpEntity with Content-Type header so the controller can parse the JSON.
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> httpEntity = new HttpEntity<>(requestBody, headers);

    URI locationUri = restTemplate
      .withBasicAuth("user", "user")
      .postForLocation("/api/books", httpEntity);

    // The response should contain a Location header
    assertThat(locationUri).isNotNull();

    System.out.println("  Created book, Location: " + locationUri);

    // Act - Retrieve the created book using GET with Basic Auth
    ResponseEntity<Book> getResponse = restTemplate
      .withBasicAuth("user", "user")
      .getForEntity(locationUri.getPath(), Book.class);

    // Assert
    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(getResponse.getBody()).isNotNull();
    assertThat(getResponse.getBody().getIsbn()).isEqualTo(VALID_ISBN);
    assertThat(getResponse.getBody().getTitle()).isEqualTo("Design Patterns");
    assertThat(getResponse.getBody().getAuthor()).isEqualTo("Gang of Four");
    assertThat(getResponse.getBody().getStatus()).isNotNull();

    System.out.println("  Retrieved book: " + getResponse.getBody().getTitle());
  }

  @Test
  @DisplayName("TestRestTemplate: 401 Unauthorized without credentials")
  void shouldRejectUnauthorizedAccessWithTestRestTemplate() {
    // GET /api/books/{id} requires USER role
    // Without credentials, expect 401
    ResponseEntity<String> response = restTemplate.getForEntity("/api/books/1", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

    System.out.println("  Received expected 401 Unauthorized");
  }
}
