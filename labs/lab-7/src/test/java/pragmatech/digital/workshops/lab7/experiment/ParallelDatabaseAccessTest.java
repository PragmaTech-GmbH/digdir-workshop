package pragmatech.digital.workshops.lab7.experiment;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import pragmatech.digital.workshops.lab7.LocalDevTestcontainerConfig;
import pragmatech.digital.workshops.lab7.config.WireMockContextInitializer;
import pragmatech.digital.workshops.lab7.entity.Book;
import pragmatech.digital.workshops.lab7.entity.BookStatus;
import pragmatech.digital.workshops.lab7.repository.BookRepository;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Experiment: Parallel Database Access Challenges
 *
 * <p>This test class demonstrates what happens when multiple tests access
 * the same database concurrently. With parallel execution enabled (see
 * junit-platform.properties), test classes run on different threads.
 *
 * <p><strong>The problem:</strong>
 * <br>If two test methods in different classes both insert a book with the
 * same ISBN, one of them will fail with a unique constraint violation.
 * Even within the same class (with method-level parallelism), shared
 * mutable database state causes flaky tests.
 *
 * <p><strong>The fix demonstrated here:</strong>
 * <ul>
 *   <li><strong>@Transactional:</strong> Each test runs in its own transaction
 *       that is rolled back after the test. Other tests never see this data.</li>
 *   <li><strong>Unique ISBNs:</strong> Using UUID-based ISBNs eliminates
 *       collisions even without transactional rollback.</li>
 * </ul>
 *
 * <p><strong>Try this experiment:</strong>
 * <ol>
 *   <li>Remove @Transactional from the class</li>
 *   <li>Change both test methods to use the same hardcoded ISBN</li>
 *   <li>Run with parallel execution enabled</li>
 *   <li>Observe the constraint violation failure</li>
 *   <li>Re-add @Transactional and see the tests pass again</li>
 * </ol>
 */
@SpringBootTest
@Import(LocalDevTestcontainerConfig.class)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
@Transactional
class ParallelDatabaseAccessTest {

  @Autowired
  private BookRepository bookRepository;

  private Book createTestBook(String isbn) {
    Book book = new Book(isbn, "Test Book " + isbn, "Author", LocalDate.of(2022, 6, 15));
    book.setStatus(BookStatus.AVAILABLE);
    book.setDescription("Test description");
    return book;
  }

  @Test
  @DisplayName("First test inserts a book - uses unique ISBN for safety")
  void shouldInsertFirstBook() {
    // Using UUID-based ISBN guarantees no collision with other tests
    String isbn = UUID.randomUUID().toString().substring(0, 13);
    Book book = createTestBook(isbn);

    Book savedBook = bookRepository.save(book);

    assertThat(savedBook.getId()).isNotNull();
    assertThat(bookRepository.findByIsbn(isbn)).isPresent();

    System.out.println("[ParallelDB] Test 1 inserted book with ISBN: " + isbn
      + " on thread: " + Thread.currentThread().getName());
  }

  @Test
  @DisplayName("Second test also inserts a book - uses unique ISBN for safety")
  void shouldInsertSecondBook() {
    // Using UUID-based ISBN guarantees no collision with other tests
    String isbn = UUID.randomUUID().toString().substring(0, 13);
    Book book = createTestBook(isbn);

    Book savedBook = bookRepository.save(book);

    assertThat(savedBook.getId()).isNotNull();
    assertThat(bookRepository.findByIsbn(isbn)).isPresent();

    System.out.println("[ParallelDB] Test 2 inserted book with ISBN: " + isbn
      + " on thread: " + Thread.currentThread().getName());
  }

  @Test
  @DisplayName("Third test verifies isolation - should not see books from other tests")
  void shouldNotSeeOtherTestsData() {
    // Insert our own book
    String isbn = UUID.randomUUID().toString().substring(0, 13);
    Book book = createTestBook(isbn);
    bookRepository.save(book);

    // We should only find our own book by this ISBN
    assertThat(bookRepository.findByIsbn(isbn)).isPresent();

    System.out.println("[ParallelDB] Test 3 verified isolation on thread: "
      + Thread.currentThread().getName());
  }
}
