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
import org.springframework.jdbc.core.JdbcTemplate;
import pragmatech.digital.workshops.lab2.entity.Book;
import pragmatech.digital.workshops.lab2.entity.BookStatus;
import pragmatech.digital.workshops.lab2.repository.BookRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * This test class demonstrates testing JPA repositories with H2 in-memory database.
 * It also showcases the limitations when using PostgreSQL-specific features.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookRepositoryInMemoryTest {

  private static final Logger log =
    LoggerFactory.getLogger(BookRepositoryInMemoryTest.class);

  @Autowired
  private BookRepository bookRepository;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void setUp() {
    System.out.println("### Books in the database ###");
    System.out.println(bookRepository.findAll().size());
  }

  @Test
  void shouldSaveAndRetrieveBook() {
    // given
    Book book = new Book("978-1-2345-6789-0", "Test Book", "Test Author", LocalDate.of(2020, 1, 1));
    book.setStatus(BookStatus.AVAILABLE);

    // when
    Book savedBook = bookRepository.save(book);
    Book retrievedBook = bookRepository.findById(savedBook.getId()).orElseThrow();

    // then
    assertThat(retrievedBook).isNotNull();
    assertThat(retrievedBook.getIsbn()).isEqualTo("978-1-2345-6789-0");
    assertThat(retrievedBook.getTitle()).isEqualTo("Test Book");
    assertThat(retrievedBook.getAuthor()).isEqualTo("Test Author");
    assertThat(retrievedBook.getPublishedDate()).isEqualTo(LocalDate.of(2020, 1, 1));
    assertThat(retrievedBook.getStatus()).isEqualTo(BookStatus.AVAILABLE);
  }

  @Test
  void shouldFindBookByIsbn() {
    // given
    Book book = new Book("978-1-2345-6789-0", "Test Book", "Test Author", LocalDate.of(2020, 1, 1));
    book.setStatus(BookStatus.AVAILABLE);
    bookRepository.save(book);

    // when
    Book retrievedBook = bookRepository.findByIsbn("978-1-2345-6789-0").orElseThrow();

    // then
    assertThat(retrievedBook).isNotNull();
    assertThat(retrievedBook.getTitle()).isEqualTo("Test Book");
  }

  @Test
  void shouldFailWhenUsingPostgresSpecificFullTextSearch() {
    // given
    Book book1 = new Book("978-1-1111-1111-1", "Advanced Java Programming", "Java Expert", LocalDate.of(2020, 1, 1));
    Book book2 = new Book("978-2-2222-2222-2", "Java for Beginners", "Another Author", LocalDate.of(2021, 2, 2));
    bookRepository.saveAll(List.of(book1, book2));

    // when & then
    Exception exception = assertThrows(Exception.class, () -> {
      List<Book> books = bookRepository.searchBooksByTitleWithRanking("java");
      log.info("Books found: {}", books);
    });

    log.error("Expected exception when using PostgreSQL-specific features: {}", exception.getMessage());
    assertThat(exception.getMessage()).contains("to_tsvector");
  }

  @Test
  void shouldDemonstrateH2CompatibilityMode() {
    // Verify we're using PostgreSQL mode
    String mode = jdbcTemplate.queryForObject("SELECT SETTING_VALUE FROM INFORMATION_SCHEMA.SETTINGS WHERE SETTING_NAME = 'MODE'", String.class);

    assertEquals("PostgreSQL", mode, "H2 should be running in PostgreSQL mode");
  }
}
