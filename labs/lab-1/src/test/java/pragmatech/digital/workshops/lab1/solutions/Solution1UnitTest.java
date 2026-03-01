package pragmatech.digital.workshops.lab1.solutions;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.Disabled;
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
import pragmatech.digital.workshops.lab1.util.TimeProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tools.jackson.databind.ext.javatime.deser.JSR310StringParsableDeserializer.ZONE_ID;

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
      .thenReturn(Optional.of(new Book(existingIsbn, "Effective Java", "Joshua Bloch", LocalDate.now(), Instant.now())));

    // Act & Assert
    BookAlreadyExistsException exception = assertThrows(
      BookAlreadyExistsException.class,
      () -> cut.registerBook(existingIsbn, "Effective Java", "Joshua Bloch")
    );

    assertThat(exception.getMessage()).contains(existingIsbn);
  }

  @Test
  @DisplayName("Should create a book when ISBN does not exist")
  @Disabled("Disabled to prevent test failure due to Sunday restriction in BookService")
  void shouldCreateBookWhenIsbnDoesNotExist() {
    // Arrange
    BookService cut = new BookService(bookRepository);
    String isbn = "9780134685991";
    String title = "Effective Java";
    String author = "Joshua Bloch";

    Book savedBook = new Book(isbn, title, author, LocalDate.now(), Instant.now());
    savedBook.setId(42L);

    when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.empty());
    when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

    // Act
    Long bookId = cut.registerBook(isbn, title, author);

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

  @Test
  void shouldThrowExceptionWhenTryingToRegisterBookOnSunday() {
    // Arrange
    TimeProvider timeProvider = mock(TimeProvider.class);
    when(timeProvider.getCurrentDate()).thenReturn(LocalDate.of(2026, 3, 1)); // A Sunday

    RefactoredBookService cut = new RefactoredBookService(bookRepository, timeProvider);
    String isbn = "9780134685991";

    when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.empty());

    // Act & Assert
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> cut.registerBook(isbn, "Effective Java", "Joshua Bloch")
    );

    assertThat(exception.getMessage()).isEqualTo("Books cannot be registered on Sunday");
  }

  @Test
  void shouldThrowExceptionWhenTryingToRegisterBookOnSundayWithClock() {
    // Arrange
    LocalDate fixedDate = LocalDate.of(2026, 3, 1);
    Clock fixedClock = Clock.fixed(
      fixedDate.atStartOfDay(ZoneId.of("UTC")).toInstant(),
      ZoneId.of("UTC")
    );

    BookServiceWithClock cut = new BookServiceWithClock(bookRepository, fixedClock);
    String isbn = "9780134685991";

    when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.empty());

    // Act & Assert
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> cut.registerBook(isbn, "Effective Java", "Joshua Bloch")
    );

    assertThat(exception.getMessage()).isEqualTo("Books cannot be registered on Sunday");
  }
}
