package pragmatech.digital.workshops.lab3.experiment;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;
import pragmatech.digital.workshops.lab3.LocalDevTestcontainerConfig;
import pragmatech.digital.workshops.lab3.config.WireMockContextInitializer;
import pragmatech.digital.workshops.lab3.entity.Book;
import pragmatech.digital.workshops.lab3.entity.BookStatus;
import pragmatech.digital.workshops.lab3.repository.BookRepository;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test class demonstrates key differences between MockMvc and WebTestClient:
 * <p>
 * 1. Thread Context:
 * - MockMvc runs in the same thread as the test method
 * - WebTestClient runs in a separate thread (reactive/non-blocking)
 * <p>
 * 2. Data Access:
 * - MockMvc shares the same transaction context as the test method
 * - WebTestClient runs in a separate thread with a separate transaction
 * <p>
 * 3. Transaction Rollback:
 * - Changes made through MockMvc are rolled back after the test completes
 * - Changes made through WebTestClient are committed and not rolled back
 * <p>
 * These differences are important to understand when choosing the appropriate
 * test approach, especially when testing transactional behavior or when the test
 * requires access to data created within the test transaction.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(LocalDevTestcontainerConfig.class)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
class WebTestClientContextTest {

  @Autowired
  private BookRepository bookRepository;

  @PersistenceContext
  private EntityManager entityManager;

  @Autowired
  private WebTestClient webTestClient;

  @BeforeEach
  void afterEach() {
    System.out.println("### Books Left in the Database Before the Test ###");
    System.out.println(bookRepository.count());
  }

  /**
   * Thread Context Tests demonstrate that:
   * - MockMvc executes the controller code in the same thread as the test
   * - WebTestClient executes the controller code in a different thread
   * <p>
   * This has implications for ThreadLocal variables and context propagation.
   */
  @Nested
  @DisplayName("Thread Context Tests")
  class ThreadContextTests {

    @Test
    @DisplayName("should use different thread when using WebTestClient")
    void shouldUseDifferentThreadWhenUsingWebTestClient() {
      // Arrange
      AtomicReference<Long> testThreadId = new AtomicReference<>(Thread.currentThread().getId());
      AtomicReference<Long> controllerThreadId = new AtomicReference<>();

      // Act & Assert
      webTestClient.get()
        .uri("/api/tests/thread-id")
        .accept(MediaType.APPLICATION_JSON)
        .headers(headers -> headers.setBasicAuth("admin", "admin"))
        .exchange()
        .expectStatus().isOk()
        .expectBody(String.class)
        .consumeWith(response -> {
          controllerThreadId.set(Long.valueOf(response.getResponseBody()));
        });

      // Assert
      assertThat(controllerThreadId.get()).isNotEqualTo(testThreadId.get());
      System.out.println("Test Thread ID: " + testThreadId.get());
      System.out.println("App Thread ID: " + controllerThreadId.get());
    }
  }

  /**
   * Data Access Tests demonstrate that:
   * - With MockMvc, the controller can access data created in the test transaction
   * - With WebTestClient, the controller cannot access uncommitted data from the test transaction
   * <p>
   * This is because WebTestClient uses a separate transaction in a different thread.
   */
  @Nested
  @DisplayName("Data Access Tests")
  @Transactional
  class DataAccessTests {

    @Test
    @DisplayName("should not access uncommitted data with WebTestClient")
    void shouldNotAccessUncommittedDataWithWebTestClient() {
      // Arrange - Create a book in the test transaction
      Book book = new Book();
      book.setIsbn("0987654321");
      book.setTitle("Test Book WebClient");
      book.setAuthor("Test Author");
      book.setPublishedDate(LocalDate.now());
      book.setStatus(BookStatus.AVAILABLE);
      bookRepository.save(book);
      entityManager.flush();

      // Act & Assert - WebTestClient cannot see the book because it uses a different transaction
      webTestClient.get().uri("/api/tests/data-access/{isbn}", book.getIsbn())
        .accept(MediaType.APPLICATION_JSON)
        .headers(headers -> headers.setBasicAuth("admin", "admin"))
        .exchange()
        .expectStatus()
        .isNotFound();
    }
  }

  /**
   * Transaction Rollback Tests demonstrate that:
   * - Changes made through MockMvc are rolled back with the test transaction
   * - Changes made through WebTestClient are committed and not rolled back
   * <p>
   * This is because WebTestClient operates in a separate thread with its own
   * transaction that gets committed independently of the test transaction.
   */
  @Nested
  @DisplayName("Transaction Rollback Tests")
  class TransactionRollbackTests {

    @AfterEach
    void afterEach() {
      System.out.println("### Books Left in the Database After the Test ###");
      System.out.println(bookRepository.count());
    }

    @Test
    @DisplayName("should not rollback changes created by WebTestClient")
    void shouldNotRollbackChangesCreatedByWebTestClient() {
      // Arrange
      String isbn = "5544332211";
      assertThat(bookRepository.findByIsbn(isbn)).isEmpty();

      // Act - Create a book through the controller with WebTestClient
      webTestClient.get().uri("/api/tests/create-for-test/{isbn}/{title}", isbn, "Rollback Test WebClient")
        .accept(MediaType.APPLICATION_JSON)
        .headers(headers -> headers.setBasicAuth("admin", "admin"))
        .exchange()
        .expectStatus().isOk();

      // Assert - Book is committed by the WebTestClient thread and remains in the database
      assertThat(bookRepository.findByIsbn(isbn)).isPresent();
      assertThat(bookRepository.findByIsbn(isbn).get().getTitle()).isEqualTo("Rollback Test WebClient");
      // Note: This book will remain in the database even after the test completes
    }
  }
}
