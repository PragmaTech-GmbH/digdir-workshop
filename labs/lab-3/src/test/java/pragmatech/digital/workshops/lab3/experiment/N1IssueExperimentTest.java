package pragmatech.digital.workshops.lab3.experiment;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(
  properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
  })
class N1IssueExperimentTest {

  @Autowired private TestEntityManager entityManager;
  @Autowired private AuthorRepository authorRepository;

  @BeforeEach
  void setUp() {
    var tolkien = new Author("J.R.R. Tolkien");
    tolkien.addBook(new BookEntry("The Fellowship of the Ring"));
    tolkien.addBook(new BookEntry("The Two Towers"));
    tolkien.addBook(new BookEntry("The Return of the King"));

    var orwell = new Author("George Orwell");
    orwell.addBook(new BookEntry("1984"));
    orwell.addBook(new BookEntry("Animal Farm"));
    orwell.addBook(new BookEntry("Homage to Catalonia"));

    var huxley = new Author("Aldous Huxley");
    huxley.addBook(new BookEntry("Brave New World"));
    huxley.addBook(new BookEntry("Island"));
    huxley.addBook(new BookEntry("Point Counter Point"));

    authorRepository.save(tolkien);
    authorRepository.save(orwell);
    authorRepository.save(huxley);
    entityManager.flush();
    entityManager.clear();
  }

  @Nested
  class N1Problem {

    @Test
    void shouldTriggerAdditionalSelectStatementsWhenAccessingCollections() {
      List<Author> authors =
          entityManager
              .getEntityManager()
              .createQuery("SELECT a FROM Author a", Author.class)
              .getResultList();

      List<String> bookTitles =
          authors.stream()
              .flatMap(author -> author.getBooks().stream())
              .map(BookEntry::getTitle)
              .toList();

      assertThat(authors).hasSize(3);
      assertThat(bookTitles).hasSize(9);
    }
  }

  @Nested
  class EntityGraphFix {

    @Test
    void shouldLoadAllBooksInASingleQueryWhenUsingEntityGraph() {
      List<Author> authors = authorRepository.findAll();

      List<String> bookTitles =
          authors.stream()
              .flatMap(author -> author.getBooks().stream())
              .map(BookEntry::getTitle)
              .toList();

      assertThat(authors).hasSize(3);
      assertThat(bookTitles).hasSize(9);
    }
  }
}
