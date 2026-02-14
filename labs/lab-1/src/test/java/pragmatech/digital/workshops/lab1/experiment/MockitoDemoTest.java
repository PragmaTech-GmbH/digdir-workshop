package pragmatech.digital.workshops.lab1.experiment;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pragmatech.digital.workshops.lab1.domain.Book;
import pragmatech.digital.workshops.lab1.repository.BookRepository;
import pragmatech.digital.workshops.lab1.service.BookService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MockitoDemoTest {

  @Mock
  private BookRepository bookRepository;

  @InjectMocks
  private BookService bookService;

  @Test
  void shouldReturnBookWhenFound() {
    // Arrange
    when(bookRepository.findByIsbn("1234")).thenReturn(Optional.empty());
    when(bookRepository.save(any())).thenReturn(new Book(1L));

    // Act
    Long id = bookService.create("1234", "Test Book", "Author");

    // Assert
    assertEquals(1L, id);
    verify(bookRepository).findByIsbn("1234");
  }
}
