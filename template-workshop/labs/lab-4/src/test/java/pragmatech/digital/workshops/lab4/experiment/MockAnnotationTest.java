package pragmatech.digital.workshops.lab4.experiment;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pragmatech.digital.workshops.lab4.client.OpenLibraryApiClient;
import pragmatech.digital.workshops.lab4.dto.BookCreationRequest;
import pragmatech.digital.workshops.lab4.dto.BookMetadataResponse;
import pragmatech.digital.workshops.lab4.entity.Book;
import pragmatech.digital.workshops.lab4.repository.BookRepository;
import pragmatech.digital.workshops.lab4.service.BookService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * This test demonstrates the usage of @Mock annotation from Mockito with JUnit 5.
 * <p>
 * Key characteristics of @Mock:
 * - A plain Mockito annotation, not Spring-specific
 * - Creates mock objects but doesn't register them in Spring context
 * - Requires MockitoExtension to be activated (@ExtendWith(MockitoExtension.class))
 * - Typically faster as it doesn't start Spring context
 * - Suitable for plain unit tests that don't need Spring features
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("@Mock Annotation Test")
class MockAnnotationTest {

  @Mock
  private OpenLibraryApiClient openLibraryApiClient;

  @Mock
  private BookRepository bookRepository;

  @InjectMocks
  private BookService bookService;

  @Test
  void shouldReturnBookWhenEnrichingWithMetadata() {
    // Arrange
    BookCreationRequest bookCreationRequest = new BookCreationRequest("1234567890", "Original Title", "Mike", LocalDate.now());

    BookMetadataResponse metadata = new BookMetadataResponse(
      "/books/123",
      "Enriched Title",
      null, null, null, null, null, null, null,
      "Book description", null, null);

    Book savedBook = new Book();
    savedBook.setId(42L);

    when(openLibraryApiClient.getBookByIsbn("1234567890")).thenReturn(metadata);
    when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());
    when(bookRepository.save(any())).thenReturn(savedBook);

    // Act
    Long createdBookId = bookService.createBook(bookCreationRequest);

    // Assert
    assertThat(createdBookId).isNotNull();

    verify(openLibraryApiClient, times(1)).getBookByIsbn(anyString());
    verify(bookRepository, times(1)).save(any());
  }
}
