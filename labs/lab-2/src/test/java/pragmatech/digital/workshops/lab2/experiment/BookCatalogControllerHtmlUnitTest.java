package pragmatech.digital.workshops.lab2.experiment;

import java.time.LocalDate;
import java.util.List;

import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlTable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pragmatech.digital.workshops.lab2.config.SecurityConfig;
import pragmatech.digital.workshops.lab2.controller.BookCatalogController;
import pragmatech.digital.workshops.lab2.entity.Book;
import pragmatech.digital.workshops.lab2.service.BookService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@WebMvcTest(BookCatalogController.class)
@Import(SecurityConfig.class)
class BookCatalogControllerHtmlUnitTest {

  @Autowired
  private WebClient webClient;

  @MockitoBean
  private BookService bookService;

  @Test
  @WithMockUser
  void shouldRenderBookCatalogPageWithAllBooks() throws Exception {
    List<Book> books = List.of(
      new Book("978-0-13-468599-1", "Effective Java", "Joshua Bloch", LocalDate.of(2018, 1, 6)),
      new Book("978-1-61729-254-0", "Spring Boot in Action", "Craig Walls", LocalDate.of(2016, 1, 4))
    );
    given(bookService.getAllBooks()).willReturn(books);

    HtmlPage page = webClient.getPage("/books");

    assertThat(page.getTitleText()).isEqualTo("Book Catalog");
    assertThat(page.getBody().getTextContent()).contains("Effective Java");
    assertThat(page.getBody().getTextContent()).contains("Spring Boot in Action");
    assertThat(page.getBody().getTextContent()).contains("Joshua Bloch");
    assertThat(page.getBody().getTextContent()).contains("Craig Walls");
  }

  @Test
  @WithMockUser
  void shouldRenderEmptyStateWhenNoBooksExist() throws Exception {
    given(bookService.getAllBooks()).willReturn(List.of());

    HtmlPage page = webClient.getPage("/books");

    assertThat(page.getTitleText()).isEqualTo("Book Catalog");
    assertThat(page.getBody().getTextContent()).contains("No books available");
  }

  @Test
  @WithMockUser
  void shouldRenderBookDetailsInHtmlTable() throws Exception {
    List<Book> books = List.of(
      new Book("978-0-13-468599-1", "Effective Java", "Joshua Bloch", LocalDate.of(2018, 1, 6))
    );
    given(bookService.getAllBooks()).willReturn(books);

    HtmlPage page = webClient.getPage("/books");

    HtmlTable table = page.getFirstByXPath("//table");
    assertThat(table).isNotNull();
    assertThat(table.asNormalizedText()).contains("978-0-13-468599-1");
    assertThat(table.asNormalizedText()).contains("Effective Java");
    assertThat(table.asNormalizedText()).contains("AVAILABLE");
  }
}
