package pragmatech.digital.workshops.lab7.solutions;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pragmatech.digital.workshops.lab7.LocalDevTestcontainerConfig;
import pragmatech.digital.workshops.lab7.config.WireMockContextInitializer;
import pragmatech.digital.workshops.lab7.entity.Book;
import pragmatech.digital.workshops.lab7.entity.BookStatus;
import pragmatech.digital.workshops.lab7.repository.BookRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Solution for Exercise 2: Fix Test Isolation Issues for Parallel Execution
 *
 * <p>This test demonstrates three key isolation strategies that work together:
 *
 * <p><strong>Strategy 1: @Transactional</strong>
 * <br>Adding @Transactional to a test method (or the class) causes Spring to
 * wrap each test in a transaction that is automatically rolled back after the
 * test completes. This is the most effective isolation mechanism for database tests.
 *
 * <p><strong>Strategy 2: Unique test data</strong>
 * <br>Each test generates a unique ISBN using UUID. Even without @Transactional,
 * this prevents conflicts from the UNIQUE constraint on the isbn column.
 * This is essential when @Transactional cannot be used (e.g., tests that verify
 * transaction boundaries or use @DirtiesContext).
 *
 * <p><strong>Strategy 3: Direct repository setup</strong>
 * <br>Instead of relying on shared fixtures or @BeforeAll, each test sets up
 * exactly the data it needs. This makes tests independent and self-documenting.
 *
 * <p><strong>Why this matters for parallel execution:</strong>
 * <br>Without isolation, parallel tests can:
 * <ul>
 *   <li>Insert duplicate ISBNs causing constraint violations</li>
 *   <li>Delete data that another test expects to find</li>
 *   <li>Count rows and get unexpected results from other tests' inserts</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(LocalDevTestcontainerConfig.class)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
@Transactional
class Solution2TestIsolationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private BookRepository bookRepository;

  /**
   * Helper method to generate a unique ISBN for each test.
   * Using UUID ensures no two tests ever collide on the ISBN column.
   */
  private String uniqueIsbn() {
    return UUID.randomUUID().toString().substring(0, 13);
  }

  /**
   * Helper method to create a Book entity with a unique ISBN.
   */
  private Book createTestBook(String isbn, String title) {
    Book book = new Book(isbn, title, "Test Author", LocalDate.of(2023, 1, 15));
    book.setStatus(BookStatus.AVAILABLE);
    book.setDescription("A test book for isolation testing");
    return book;
  }

  @Test
  @WithMockUser(roles = "USER")
  void shouldCreateBookWithIsolatedData() {
    // Arrange: each test uses a unique ISBN
    String isbn = uniqueIsbn();
    Book book = createTestBook(isbn, "Isolated Book " + isbn);

    // Act: insert via repository
    Book savedBook = bookRepository.save(book);

    // Assert
    assertThat(savedBook.getId()).isNotNull();
    assertThat(bookRepository.findByIsbn(isbn)).isPresent();

    // No cleanup needed: @Transactional will roll back after this test
  }

  @Test
  @WithMockUser(roles = "USER")
  void shouldRetrieveBookWithoutSideEffects() throws Exception {
    // Arrange: insert test data directly
    String isbn = uniqueIsbn();
    Book book = createTestBook(isbn, "Retrievable Book");
    Book savedBook = bookRepository.save(book);

    // Act & Assert: retrieve via API
    mockMvc.perform(get("/api/books/{id}", savedBook.getId())
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.isbn").value(isbn))
      .andExpect(jsonPath("$.title").value("Retrievable Book"));

    // @Transactional ensures this data is rolled back
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void shouldDeleteBookSafely() throws Exception {
    // Arrange: insert a book to delete
    String isbn = uniqueIsbn();
    Book book = createTestBook(isbn, "Book To Delete");
    Book savedBook = bookRepository.save(book);

    // Act: delete via API
    mockMvc.perform(delete("/api/books/{id}", savedBook.getId()))
      .andExpect(status().isNoContent());

    // Assert: book is gone
    assertThat(bookRepository.findById(savedBook.getId())).isEmpty();
  }

  @Test
  @WithMockUser(roles = "USER")
  void shouldNotSeeDataFromOtherTests() {
    // Arrange: insert a book
    String isbn = uniqueIsbn();
    Book book = createTestBook(isbn, "My Private Book");
    bookRepository.save(book);

    // Assert: only this test's book should exist with this ISBN
    // Other tests running in parallel will not see this book
    // because @Transactional isolates us at the database level
    assertThat(bookRepository.findByIsbn(isbn)).isPresent();
  }

  @Test
  @WithMockUser(roles = "USER")
  void shouldHandleConcurrentInsertsWithUniqueData() {
    // Arrange: insert multiple books with unique ISBNs
    String isbn1 = uniqueIsbn();
    String isbn2 = uniqueIsbn();

    Book book1 = createTestBook(isbn1, "Concurrent Book 1");
    Book book2 = createTestBook(isbn2, "Concurrent Book 2");

    // Act
    bookRepository.save(book1);
    bookRepository.save(book2);

    // Assert: both books exist independently
    assertThat(bookRepository.findByIsbn(isbn1)).isPresent();
    assertThat(bookRepository.findByIsbn(isbn2)).isPresent();
  }
}
