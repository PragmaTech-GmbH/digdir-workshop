package pragmatech.digital.workshops.lab3.experiment;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import pragmatech.digital.workshops.lab3.dto.BookCreationRequest;
import pragmatech.digital.workshops.lab3.entity.Book;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookJsonTest {

  @Autowired
  private JacksonTester<Book> bookJson;

  @Autowired
  private JacksonTester<BookCreationRequest> bookCreationRequestJson;

  @Test
  void shouldSerializeBookStatusAsStringWhenWritingBook() throws Exception {
    Book book = new Book("978-0-13-235088-4", "Clean Code", "Robert C. Martin", LocalDate.of(2008, 8, 1));

    assertThat(bookJson.write(book))
      .extractingJsonPathStringValue("$.status")
      .isEqualTo("AVAILABLE");
  }

  @Test
  void shouldSerializePublishedDateAsIsoFormatWhenWritingBook() throws Exception {
    Book book = new Book("978-0-13-235088-4", "Clean Code", "Robert C. Martin", LocalDate.of(2008, 8, 1));

    assertThat(bookJson.write(book))
      .extractingJsonPathStringValue("$.publishedDate")
      .isEqualTo("2008-08-01");
  }

  @Test
  void shouldContainAllExpectedFieldsWhenSerializingBook() throws Exception {
    Book book = new Book("978-0-13-235088-4", "Clean Code", "Robert C. Martin", LocalDate.of(2008, 8, 1));

    assertThat(bookJson.write(book))
      .hasJsonPathStringValue("$.isbn")
      .hasJsonPathStringValue("$.title")
      .hasJsonPathStringValue("$.author")
      .hasJsonPathStringValue("$.publishedDate");
  }

  @Test
  void shouldDeserializeBookCreationRequestWhenParsingValidJson() throws Exception {
    String json = """
      {
        "isbn": "978-0-13-235088-4",
        "title": "Clean Code",
        "author": "Robert C. Martin",
        "publishedDate": "2008-08-01"
      }
      """;

    BookCreationRequest result = bookCreationRequestJson.parseObject(json);

    assertThat(result.isbn()).isEqualTo("978-0-13-235088-4");
    assertThat(result.title()).isEqualTo("Clean Code");
    assertThat(result.author()).isEqualTo("Robert C. Martin");
    assertThat(result.publishedDate()).isEqualTo(LocalDate.of(2008, 8, 1));
  }
}
