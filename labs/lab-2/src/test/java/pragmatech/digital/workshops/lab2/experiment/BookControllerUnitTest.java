package pragmatech.digital.workshops.lab2.experiment;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import pragmatech.digital.workshops.lab2.controller.BookController;
import pragmatech.digital.workshops.lab2.dto.BookCreationRequest;
import pragmatech.digital.workshops.lab2.dto.BookUpdateRequest;
import pragmatech.digital.workshops.lab2.entity.Book;
import pragmatech.digital.workshops.lab2.entity.BookStatus;
import pragmatech.digital.workshops.lab2.service.BookService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * This is a unit test for the BookController class.
 * It demonstrates what can be tested with plain JUnit and Mockito without using Spring's test infrastructure.
 * This approach is fast and focused on the controller's logic in isolation.
 */
@ExtendWith(MockitoExtension.class)
class BookControllerUnitTest {

  @Mock
  private BookService bookService;

  @InjectMocks
  private BookController bookController;

  @Test
  void shouldReturnAllBooksWhenGettingAllBooks() {
    // Arrange
    Book book1 = new Book("123-1234567890", "Test Book", "Test Author", LocalDate.of(2020, 1, 1));
    book1.setStatus(BookStatus.AVAILABLE);
    Book book2 = new Book("456-1234567890", "Another Book", "Another Author", LocalDate.of(2021, 2, 2));
    List<Book> books = Arrays.asList(book1, book2);
    when(bookService.getAllBooks()).thenReturn(books);

    // Act
    List<Book> result = bookController.getAllBooks();

    // Assert
    assertThat(result).hasSize(2);
    assertThat(result).contains(book1);
    verify(bookService).getAllBooks();
  }

  @Test
  void shouldReturnBookWhenBookExistsById() {
    // Arrange
    Book book = new Book("123-1234567890", "Test Book", "Test Author", LocalDate.of(2020, 1, 1));
    book.setStatus(BookStatus.AVAILABLE);
    when(bookService.getBookById(1L)).thenReturn(Optional.of(book));

    // Act
    ResponseEntity<Book> response = bookController.getBookById(1L);

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(book);
    verify(bookService).getBookById(1L);
  }

  @Test
  void shouldReturnNotFoundWhenBookDoesNotExistById() {
    // Arrange
    when(bookService.getBookById(anyLong())).thenReturn(Optional.empty());

    // Act
    ResponseEntity<Book> response = bookController.getBookById(999L);

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNull();
  }

  @Test
  void shouldReturnCreatedResponseWithLocationWhenCreatingBook() {
    // Arrange
    BookCreationRequest creationRequest = new BookCreationRequest(
      "123-1234567890",
      "Test Book",
      "Test Author",
      LocalDate.of(2020, 1, 1)
    );

    UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
      .scheme("http")
      .host("localhost");

    when(bookService.createBook(any(BookCreationRequest.class))).thenReturn(1L);

    // Act
    ResponseEntity<Void> response = bookController.createBook(creationRequest, uriBuilder);

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getHeaders().getLocation().toString()).contains("/api/books/1");
    verify(bookService).createBook(creationRequest);
  }

  @Test
  void shouldReturnUpdatedBookWhenBookExistsForUpdate() {
    // Arrange
    BookUpdateRequest updateRequest = new BookUpdateRequest(
      "Updated Book",
      "Updated Author",
      LocalDate.of(2021, 1, 1),
      BookStatus.RESERVED
    );

    Book updatedBook = new Book("123-1234567890", "Updated Book", "Updated Author", LocalDate.of(2021, 1, 1));
    updatedBook.setStatus(BookStatus.RESERVED);

    when(bookService.updateBook(eq(1L), any(BookUpdateRequest.class))).thenReturn(Optional.of(updatedBook));

    // Act
    ResponseEntity<Book> response = bookController.updateBook(1L, updateRequest);

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getTitle()).isEqualTo("Updated Book");
    assertThat(response.getBody().getStatus()).isEqualTo(BookStatus.RESERVED);
  }

  @Test
  void shouldReturnNoContentWhenBookExistsForDeletion() {
    // Arrange
    when(bookService.deleteBook(1L)).thenReturn(true);

    // Act
    ResponseEntity<Void> response = bookController.deleteBook(1L);

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }

  @Test
  void shouldReturnNotFoundWhenBookDoesNotExistForDeletion() {
    // Arrange
    when(bookService.deleteBook(anyLong())).thenReturn(false);

    // Act
    ResponseEntity<Void> response = bookController.deleteBook(999L);

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
