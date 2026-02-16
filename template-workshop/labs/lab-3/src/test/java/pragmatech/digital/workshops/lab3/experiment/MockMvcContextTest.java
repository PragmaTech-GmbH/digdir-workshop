package pragmatech.digital.workshops.lab3.experiment;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pragmatech.digital.workshops.lab3.LocalDevTestcontainerConfig;
import pragmatech.digital.workshops.lab3.config.WireMockContextInitializer;
import pragmatech.digital.workshops.lab3.entity.Book;
import pragmatech.digital.workshops.lab3.entity.BookStatus;
import pragmatech.digital.workshops.lab3.repository.BookRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
@SpringBootTest
@AutoConfigureMockMvc
@Import(LocalDevTestcontainerConfig.class)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
class MockMvcContextTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private BookRepository bookRepository;

  @PersistenceContext
  private EntityManager entityManager;

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
    @WithMockUser(roles = "ADMIN")
    @DisplayName("should remain in same thread when using MockMvc")
    void shouldRemainInSameThreadWhenUsingMockMvc() throws Exception {
      // Arrange
      AtomicReference<Long> testThreadId = new AtomicReference<>(Thread.currentThread().getId());
      AtomicReference<Long> controllerThreadId = new AtomicReference<>();

      // Act & Assert
      mockMvc.perform(get("/api/tests/thread-id")
          .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andDo(result -> {
          controllerThreadId.set(Long.valueOf(result.getResponse().getContentAsString()));
        });

      // Assert
      assertThat(controllerThreadId.get()).isEqualTo(testThreadId.get());
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
    @WithMockUser(roles = "ADMIN")
    @DisplayName("should access modified data during request with MockMvc")
    void shouldAccessModifiedDataDuringRequestWithMockMvc() throws Exception {
      // Arrange - Create a book in the test transaction
      Book book = new Book();
      book.setIsbn("1234567890");
      book.setTitle("Test Book");
      book.setAuthor("Test Author");
      book.setPublishedDate(LocalDate.now());
      book.setStatus(BookStatus.AVAILABLE);
      bookRepository.save(book);
      entityManager.flush();

      // Act & Assert - MockMvc can see the book because it shares the transaction
      mockMvc.perform(get("/api/tests/data-access/{isbn}", book.getIsbn())
          .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
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
  @Transactional
  class TransactionRollbackTests {

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("should rollback changes after MockMvc test")
    void shouldRollbackChangesAfterMockMvcTest() throws Exception {
      // Arrange
      String isbn = "1122334455";

      // Act - Create a book through the controller with MockMvc
      mockMvc.perform(get("/api/tests/create-for-test/{isbn}/{title}", isbn, "Rollback Test MockMvc")
          .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

      // Assert - Book is visible in the test transaction, but will be rolled back after test
      assertThat(bookRepository.findByIsbn(isbn)).isPresent();
      assertThat(TestTransaction.isActive()).isTrue();
      // Note: After this test completes, the transaction will be rolled back
    }
  }
}
