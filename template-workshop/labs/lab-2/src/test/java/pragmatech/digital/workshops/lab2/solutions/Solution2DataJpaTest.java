package pragmatech.digital.workshops.lab2.solutions;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pragmatech.digital.workshops.lab2.entity.Book;
import pragmatech.digital.workshops.lab2.entity.BookStatus;
import pragmatech.digital.workshops.lab2.repository.BookRepository;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Solution 2: Data JPA Test
 * <p>
 * This class demonstrates testing a Spring Data JPA repository with Testcontainers
 * It focuses on testing the PostgreSQL-specific native query in BookRepository
 * and ensuring test isolation.
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class Solution2DataJpaTest {

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
    .withDatabaseName("testdb")
    .withUsername("test")
    .withPassword("test")
    .withInitScript("init-postgres.sql");

  @Autowired
  private BookRepository cut;

  @BeforeEach
  void setUp() {
    // Ensure test isolation by cleaning up before each test
    cut.deleteAll();
  }

  @Nested
  @DisplayName("Full text search tests")
  class FullTextSearchTests {

    @Test
    @DisplayName("Should find books by title with proper ranking")
    void shouldFindBooksByTitleWithProperRanking() {
      // Arrange - Create test books with keywords in titles
      Book javaBook1 = new Book("978-1-1234-5678-1", "Advanced Java Programming", "Author 1", LocalDate.now().minusYears(1));
      Book javaBook2 = new Book("978-1-1234-5678-2", "Java for Beginners", "Author 2", LocalDate.now().minusYears(2));
      Book pythonBook = new Book("978-1-1234-5678-4", "Python Programming", "Author 4", LocalDate.now().minusYears(4));

      cut.saveAll(List.of(javaBook1, javaBook2, pythonBook));

      // Act - Search for Java books using the full-text search
      List<Book> results = cut.searchBooksByTitleWithRanking("java");

      // Assert - Should find Java books, but not Python
      assertThat(results).hasSize(2);

      assertThat(results.stream().map(Book::getTitle))
        .contains("Advanced Java Programming", "Java for Beginners");
    }

    @Test
    @DisplayName("Should return empty list when no books match search term")
    void shouldReturnEmptyListWhenNoBooksMatchSearchTerm() {
      // Arrange - Create books that don't match the search term
      Book book1 = new Book("978-1-1234-5678-1", "Python for Beginners", "Author 1", LocalDate.now().minusYears(1));
      Book book2 = new Book("978-1-1234-5678-2", "C# Programming Guide", "Author 2", LocalDate.now().minusYears(2));

      cut.saveAll(List.of(book1, book2));

      // Act - Search for a term that doesn't match any book
      List<Book> results = cut.searchBooksByTitleWithRanking("javascript");

      // Assert - Should return empty list
      assertThat(results).isEmpty();
    }
  }

  @Nested
  @DisplayName("Basic repository operations tests")
  class BasicOperationsTests {

    @Test
    @DisplayName("Should save and find book by ISBN")
    void shouldSaveAndFindBookByIsbn() {
      // Arrange - Create and save a test book
      String isbn = "978-1-1234-5678-1";
      Book book = new Book(isbn, "Test Book", "Test Author", LocalDate.now().minusYears(1));
      book.setStatus(BookStatus.AVAILABLE);

      cut.save(book);

      // Act - Find the book by ISBN
      Optional<Book> result = cut.findByIsbn(isbn);

      // Assert - Should find the book with the correct data
      assertThat(result).isPresent();
      assertThat(result.get().getIsbn()).isEqualTo(isbn);
      assertThat(result.get().getTitle()).isEqualTo("Test Book");
      assertThat(result.get().getAuthor()).isEqualTo("Test Author");
      assertThat(result.get().getStatus()).isEqualTo(BookStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Should ensure no data persists between tests")
    void shouldEnsureNoDataPersistsBetweenTests() {
      // Act - Count books in the repository
      long count = cut.count();

      // Assert - No books should exist at the start of the test
      assertThat(count).isZero();

      // Arrange/Act - Add a book and verify it was added
      Book book = new Book("978-1-1234-5678-1", "Test Book", "Test Author", LocalDate.now().minusYears(1));
      cut.save(book);

      // Assert - After adding, count should be 1
      assertThat(cut.count()).isEqualTo(1);
    }
  }
}
