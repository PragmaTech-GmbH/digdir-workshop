package pragmatech.digital.workshops.lab1.experiment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JUnit5DemoTest {

  @Test
  void shouldCreateNewBook() {
    TestBook testBook = new TestBook("1234", "Spring Boot Testing", "Test Author");

    assertEquals("1234", testBook.isbn());
    assertEquals("Spring Boot Testing", testBook.title());
    assertEquals("Test Author", testBook.author());
  }

  @ParameterizedTest
  @CsvSource({
    "1234, Spring Boot Testing, Test Author",
    "5678, Advanced Spring, Another Author"
  })
  void shouldCreateBooksFromParameters(String isbn, String title, String author) {
    TestBook testBook = new TestBook(isbn, title, author);

    assertNotNull(testBook);
    assertEquals(isbn, testBook.isbn());
    assertEquals(title, testBook.title());
    assertEquals(author, testBook.author());
  }
}
