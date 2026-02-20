package pragmatech.digital.workshops.lab5.experiment;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pragmatech.digital.workshops.lab5.LocalDevTestcontainerConfig;
import pragmatech.digital.workshops.lab5.config.WireMockContextInitializer;
import pragmatech.digital.workshops.lab5.entity.Book;
import pragmatech.digital.workshops.lab5.entity.BookStatus;
import pragmatech.digital.workshops.lab5.repository.BookRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Experiment: MockMvc vs WebTestClient -- Side-by-Side Comparison (MockMvc edition)
 * <p>
 * This test class demonstrates the MockMvc side of the comparison. It shows three
 * fundamental differences between MockMvc and WebTestClient when used in integration tests.
 * <p>
 * Run this test alongside {@link WebTestClientContextTest} to see the contrast.
 * <p>
 * <strong>1. Thread Context</strong>
 * <ul>
 *   <li>MockMvc: Controller code runs in the SAME thread as the test method</li>
 *   <li>WebTestClient: Controller code runs in a DIFFERENT thread (real HTTP)</li>
 * </ul>
 * <p>
 * <strong>2. Data Access</strong>
 * <ul>
 *   <li>MockMvc: Shares the test transaction -- controller can see uncommitted test data</li>
 *   <li>WebTestClient: Separate transaction -- controller cannot see uncommitted test data</li>
 * </ul>
 * <p>
 * <strong>3. Transaction Rollback</strong>
 * <ul>
 *   <li>MockMvc: With {@code @Transactional}, all changes (test + controller) are rolled back</li>
 *   <li>WebTestClient: Controller changes are committed regardless of {@code @Transactional} on the test</li>
 * </ul>
 * <p>
 * These differences affect your choice of test approach depending on whether you need
 * transaction isolation, real HTTP behavior, or simplified test data management.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(LocalDevTestcontainerConfig.class)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
class MockMvcVsWebTestClientComparisonTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private BookRepository bookRepository;

  @PersistenceContext
  private EntityManager entityManager;

  @BeforeEach
  void printDatabaseState() {
    System.out.println("=== Books in database before test: " + bookRepository.count() + " ===");
  }

  // ---------------------------------------------------------------------------
  // 1. Thread Context: MockMvc runs controller code in the same thread
  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("Thread Context Tests")
  class ThreadContextTests {

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("MockMvc: controller executes in the SAME thread as the test")
    void shouldRunInSameThreadWhenUsingMockMvc() throws Exception {
      // Capture the test thread ID
      AtomicReference<Long> testThreadId = new AtomicReference<>(Thread.currentThread().getId());
      AtomicReference<Long> controllerThreadId = new AtomicReference<>();

      // The ThreadController returns Thread.currentThread().getId() as its response
      mockMvc.perform(get("/api/tests/thread-id")
          .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andDo(result -> {
          controllerThreadId.set(Long.valueOf(result.getResponse().getContentAsString()));
        });

      // With MockMvc, the controller runs in the same thread as the test
      assertThat(controllerThreadId.get()).isEqualTo(testThreadId.get());

      System.out.println("  Test thread ID:       " + testThreadId.get());
      System.out.println("  Controller thread ID: " + controllerThreadId.get());
      System.out.println("  => Same thread: " + testThreadId.get().equals(controllerThreadId.get()));
    }
  }

  // ---------------------------------------------------------------------------
  // 2. Data Access: MockMvc shares the test transaction
  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("Data Access Tests")
  @Transactional
  class DataAccessTests {

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("MockMvc: controller CAN see uncommitted data from the test transaction")
    void shouldAccessUncommittedTestDataWhenUsingMockMvc() throws Exception {
      // Create a book in the test transaction (not yet committed)
      Book book = new Book();
      book.setIsbn("1111111111");
      book.setTitle("Uncommitted Test Book");
      book.setAuthor("Test Author");
      book.setPublishedDate(LocalDate.now());
      book.setStatus(BookStatus.AVAILABLE);
      bookRepository.save(book);
      entityManager.flush();

      // MockMvc can see the book because it shares the same transaction
      mockMvc.perform(get("/api/tests/data-access/{isbn}", book.getIsbn())
          .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

      System.out.println("  MockMvc found the uncommitted book with ISBN: " + book.getIsbn());
      // Compare with WebTestClientContextTest: WebTestClient would get 404 here
    }
  }

  // ---------------------------------------------------------------------------
  // 3. Transaction Rollback: MockMvc changes are rolled back with @Transactional
  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("Transaction Rollback Tests")
  @Transactional
  class TransactionRollbackTests {

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("MockMvc: data created through the controller is rolled back after the test")
    void shouldRollbackControllerChangesWhenUsingMockMvc() throws Exception {
      // Create a book through the controller endpoint
      String isbn = "2222222222";
      mockMvc.perform(get("/api/tests/create-for-test/{isbn}/{title}", isbn, "Rollback Demo")
          .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

      // The book is visible within this test because they share the transaction
      assertThat(bookRepository.findByIsbn(isbn)).isPresent();
      assertThat(TestTransaction.isActive()).isTrue();

      System.out.println("  Book created via MockMvc is visible in the test transaction");
      System.out.println("  Test transaction is active: " + TestTransaction.isActive());
      System.out.println("  => After test: transaction will be ROLLED BACK automatically");
      // Compare with WebTestClientContextTest: data would remain committed
    }
  }
}
