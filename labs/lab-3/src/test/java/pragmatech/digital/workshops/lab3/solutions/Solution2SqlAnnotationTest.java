package pragmatech.digital.workshops.lab3.solutions;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pragmatech.digital.workshops.lab3.entity.Book;
import pragmatech.digital.workshops.lab3.entity.BookStatus;
import pragmatech.digital.workshops.lab3.repository.BookRepository;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql("/data/sample-books.sql")
class Solution2SqlAnnotationTest {

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
    .withDatabaseName("testdb")
    .withUsername("test")
    .withPassword("test")
    .withInitScript("init-postgres.sql");

  @Autowired
  private BookRepository cut;

  @Test
  void shouldLoadThreeBooksWhenSqlFileIsProvided() {
    List<Book> books = cut.findAll();

    assertThat(books).hasSize(3);
    assertThat(books).extracting(Book::getTitle)
      .containsExactlyInAnyOrder("Clean Code", "Effective Java", "The Pragmatic Programmer");
  }

  @Test
  void shouldFindBorrowedBookWhenSampleDataIsLoaded() {
    List<Book> books = cut.findAll();

    assertThat(books)
      .filteredOn(book -> book.getStatus() == BookStatus.BORROWED)
      .hasSize(1)
      .extracting(Book::getTitle)
      .containsExactly("The Pragmatic Programmer");
  }

  @Test
  void shouldFindAvailableBooksWhenSampleDataIsLoaded() {
    List<Book> books = cut.findAll();

    assertThat(books)
      .filteredOn(Book::isAvailable)
      .hasSize(2);
  }
}
