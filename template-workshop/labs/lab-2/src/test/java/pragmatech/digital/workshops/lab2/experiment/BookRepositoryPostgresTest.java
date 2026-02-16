package pragmatech.digital.workshops.lab2.experiment;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This test class demonstrates testing JPA repositories with a real PostgreSQL database
 * using Testcontainers. It shows how to test PostgreSQL-specific features.
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookRepositoryPostgresTest {

  private static final Logger log =
    LoggerFactory.getLogger(BookRepositoryPostgresTest.class);

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
    .withDatabaseName("testdb")
    .withUsername("test")
    .withPassword("test")
    .withInitScript("init-postgres.sql"); // Initialize PostgreSQL with required extensions

  @Autowired
  private BookRepository bookRepository;

  @BeforeEach
  void setUp() {
    // Clean up before each test
    bookRepository.deleteAll();

    // Set up sample books
    Book book1 = new Book("978-1-1111-1111-1", "Advanced Java Programming", "Java Expert", LocalDate.of(2020, 1, 1));
    Book book2 = new Book("978-2-2222-2222-2", "Java for Beginners", "Another Author", LocalDate.of(2021, 2, 2));
    Book book3 = new Book("978-3-3333-3333-3", "Python Programming", "Python Guru", LocalDate.of(2022, 3, 3));
    Book book4 = new Book("978-4-4444-4444-4", "JavaScript Mastery", "JS Expert", LocalDate.of(2019, 4, 4));

    bookRepository.saveAll(List.of(book1, book2, book3, book4));
  }

  @Test
  void shouldUseFullTextSearchWithRanking() {
    // when
    List<Book> books = bookRepository.searchBooksByTitleWithRanking("java");

    // then
    assertThat(books).isNotEmpty();
    assertThat(books).hasSize(2);

    // The book with "Java" in the title should be ranked higher
    assertThat(books.get(0).getTitle()).contains("Java");

    log.info("Full text search results: {}", books);
  }

  @Test
  void shouldHandlePostgreSQLSpecificFeatures() {
    // given
    Book book = new Book("978-5-5555-5555-5", "Title with Special Characters: åäö",
      "Author with Emoji: 😊", LocalDate.of(2023, 5, 5));
    book.setStatus(BookStatus.AVAILABLE);
    bookRepository.save(book);

    // when - use case-insensitive search
    Book foundBook = bookRepository.findByIsbn("978-5-5555-5555-5").orElseThrow();

    // then
    assertEquals("Title with Special Characters: åäö", foundBook.getTitle());
    assertEquals("Author with Emoji: 😊", foundBook.getAuthor());
  }
}
