package pragmatech.digital.workshops.lab1.solutions;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pragmatech.digital.workshops.lab1.domain.Book;
import pragmatech.digital.workshops.lab1.repository.BookRepository;
import pragmatech.digital.workshops.lab1.service.BookAlreadyExistsException;
import pragmatech.digital.workshops.lab1.service.BookService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Solution1UnitTest {

  @Mock
  private BookRepository bookRepository;

  @Test
  void shouldThrowExceptionWhenBookWithIsbnAlreadyExists() {
    // Arrange
    BookService cut = new BookService(bookRepository);
    String existingIsbn = "9780134685991";

    when(bookRepository.findByIsbn(existingIsbn))
      .thenReturn(Optional.of(new Book(existingIsbn, "Effective Java", "Joshua Bloch", LocalDate.now())));

    // Act & Assert
    BookAlreadyExistsException exception = assertThrows(
      BookAlreadyExistsException.class,
      () -> cut.create(existingIsbn, "Effective Java", "Joshua Bloch")
    );

    assertThat(exception.getMessage()).contains(existingIsbn);
  }

  @Test
  @DisplayName("Should create a book when ISBN does not exist")
  void shouldCreateBookWhenIsbnDoesNotExist() {
    // Arrange
    BookService cut = new BookService(bookRepository);
    String isbn = "9780134685991";
    String title = "Effective Java";
    String author = "Joshua Bloch";

    Book savedBook = new Book(isbn, title, author, LocalDate.now());
    savedBook.setId(42L);

    when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.empty());
    when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

    // Act
    Long bookId = cut.create(isbn, title, author);

    // Assert
    assertThat(bookId).isEqualTo(42L);

    ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);
    verify(bookRepository).save(bookCaptor.capture());

    Book capturedBook = bookCaptor.getValue();
    assertThat(capturedBook.getIsbn()).isEqualTo(isbn);
    assertThat(capturedBook.getTitle()).isEqualTo(title);
    assertThat(capturedBook.getAuthor()).isEqualTo(author);
    assertThat(capturedBook.getPublishedDate()).isNotNull();
  }
}
