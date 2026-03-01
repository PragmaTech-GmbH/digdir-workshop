package pragmatech.digital.workshops.lab5.experiment;

import java.net.URI;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import pragmatech.digital.workshops.lab5.LocalDevTestcontainerConfig;
import pragmatech.digital.workshops.lab5.config.OpenLibraryApiStub;
import pragmatech.digital.workshops.lab5.config.WireMockContextInitializer;
import pragmatech.digital.workshops.lab5.entity.Book;
import pragmatech.digital.workshops.lab5.repository.BookRepository;

/**
 * Integration tests for {@link pragmatech.digital.workshops.lab5.controller.BookController}
 * using a real embedded HTTP server (RANDOM_PORT).
 *
 * <p>Key observations about this setup:
 * <ul>
 *   <li>A real Tomcat server starts on a random port — WebTestClient sends actual TCP requests</li>
 *   <li>The test thread and server thread are DIFFERENT — {@code @Transactional} on this class
 *       would NOT roll back server-side changes (different transaction boundary)</li>
 *   <li>Data saved directly via {@code bookRepository} in {@code @BeforeEach} IS visible to
 *       the server because it is auto-committed before the HTTP request is sent</li>
 *   <li>Manual cleanup via {@code @AfterEach} is required to prevent data leaking between tests</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(LocalDevTestcontainerConfig.class)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
@AutoConfigureWebTestClient
class BookControllerIT {

  private static final String VALID_ISBN = "978-0134757599";

  @Autowired
  private WebTestClient webTestClient;

  @Autowired
  private BookRepository bookRepository;

  @Autowired
  private OpenLibraryApiStub openLibraryApiStub;

  @AfterEach
  void cleanUp() {
    // The server thread commits data to the shared database.
    // We cannot rely on @Transactional rollback here — manual cleanup is required.
    bookRepository.deleteAll();
  }

  @Nested
  class GetAllBooks {

    @BeforeEach
    void setUp() {
      // Data saved here is auto-committed BEFORE the HTTP request is sent.
      // The server thread CAN see this data because it is already in the DB.
      bookRepository.save(new Book("111-1234567890", "Clean Code", "Robert C. Martin", LocalDate.of(2008, 8, 1)));
      bookRepository.save(new Book("222-1234567890", "Effective Java", "Joshua Bloch", LocalDate.of(2018, 1, 6)));
    }

    @Test
    void shouldReturnAllBooksWhenGettingAllBooks() {
      webTestClient.get()
        .uri("/api/books")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$").isArray()
        .jsonPath("$.length()").isEqualTo(2);
    }

    @Test
    void shouldAllowUnauthenticatedAccessToBookListWhenGettingAllBooks() {
      webTestClient.get()
        .uri("/api/books")
        .exchange()
        .expectStatus().isOk();
    }
  }

  @Nested
  class GetBookById {

    @Test
    void shouldReturnBookWhenBookExistsAndUserIsAuthenticated() {
      Book savedBook = bookRepository.save(
        new Book("333-1234567890", "Domain-Driven Design", "Eric Evans", LocalDate.of(2003, 8, 30)));

      webTestClient.get()
        .uri("/api/books/{id}", savedBook.getId())
        .accept(MediaType.APPLICATION_JSON)
        .headers(headers -> headers.setBasicAuth("user", "user"))
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.isbn").isEqualTo("333-1234567890")
        .jsonPath("$.title").isEqualTo("Domain-Driven Design")
        .jsonPath("$.status").isEqualTo("AVAILABLE");
    }

    @Test
    void shouldReturn401WhenGettingBookByIdWithoutAuthentication() {
      // GET /api/books/{id} requires ROLE_USER — no credentials → 401
      webTestClient.get()
        .uri("/api/books/1")
        .exchange()
        .expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturn404WhenBookDoesNotExist() {
      webTestClient.get()
        .uri("/api/books/99999")
        .headers(headers -> headers.setBasicAuth("user", "user"))
        .exchange()
        .expectStatus().isNotFound();
    }
  }

  @Nested
  class CreateBook {

    @Test
    void shouldCreateBookAndReturnLocationWhenRequestIsValid() {
      openLibraryApiStub.stubForSuccessfulBookResponse(VALID_ISBN);

      String requestBody = """
          {
            "isbn": "%s",
            "title": "Effective Java",
            "author": "Joshua Bloch",
            "publishedDate": "2018-01-06"
          }
          """.formatted(VALID_ISBN);

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

      // The book was committed by the server thread — we can now fetch it
      webTestClient.get()
        .uri(locationUri.getPath())
        .headers(headers -> headers.setBasicAuth("user", "user"))
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.isbn").isEqualTo(VALID_ISBN)
        .jsonPath("$.title").isEqualTo("Effective Java")
        .jsonPath("$.status").isEqualTo("AVAILABLE");
    }

    @Test
    void shouldReturn400WhenIsbnFormatIsInvalid() {
      String requestBody = """
          {
            "isbn": "not-a-valid-isbn",
            "title": "Some Book",
            "author": "Some Author",
            "publishedDate": "2020-01-01"
          }
          """;

      webTestClient.post()
        .uri("/api/books")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(headers -> headers.setBasicAuth("user", "user"))
        .bodyValue(requestBody)
        .exchange()
        .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturn401WhenCreatingBookWithoutAuthentication() {
      String requestBody = """
          {
            "isbn": "%s",
            "title": "Effective Java",
            "author": "Joshua Bloch",
            "publishedDate": "2018-01-06"
          }
          """.formatted(VALID_ISBN);

      webTestClient.post()
        .uri("/api/books")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(requestBody)
        .exchange()
        .expectStatus().isUnauthorized();
    }
  }

  @Nested
  class DeleteBook {

    @Test
    void shouldDeleteBookWhenUserIsAdmin() {
      Book savedBook = bookRepository.save(
        new Book("444-1234567890", "Refactoring", "Martin Fowler", LocalDate.of(2018, 11, 20)));

      webTestClient.delete()
        .uri("/api/books/{id}", savedBook.getId())
        .headers(headers -> headers.setBasicAuth("admin", "admin"))
        .exchange()
        .expectStatus().isNoContent();
    }

    @Test
    void shouldReturn403WhenDeletingBookWithoutAdminRole() {
      Book savedBook = bookRepository.save(
        new Book("555-1234567890", "The Pragmatic Programmer", "David Thomas", LocalDate.of(2019, 9, 13)));

      webTestClient.delete()
        .uri("/api/books/{id}", savedBook.getId())
        .headers(headers -> headers.setBasicAuth("user", "user"))
        .exchange()
        .expectStatus().isForbidden();
    }
  }
}
