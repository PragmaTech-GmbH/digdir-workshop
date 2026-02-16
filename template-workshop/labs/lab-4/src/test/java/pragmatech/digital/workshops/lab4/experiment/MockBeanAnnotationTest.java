package pragmatech.digital.workshops.lab4.experiment;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pragmatech.digital.workshops.lab4.LocalDevTestcontainerConfig;
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
 * This test demonstrates the usage of @MockBean annotation from Spring Boot Test.
 * <p>
 * Key characteristics of @MockBean:
 * - Spring Boot Test specific annotation
 * - Creates a Mockito mock and adds it to the Spring application context
 * - Replaces any existing bean of the same type in the context with the mock
 * - Requires a full Spring application context to be started
 * - Slower than @Mock as it needs to start Spring context
 * - Useful for integration tests where you want to mock specific beans in the context
 * - When you modify mocks between tests, it triggers context reload (slower)
 */
@SpringBootTest
@Import(LocalDevTestcontainerConfig.class)
@DisplayName("@MockBean Annotation Test")
class MockBeanAnnotationTest {

  @MockitoBean
  private OpenLibraryApiClient openLibraryApiClient;

  @MockBean
  private BookRepository bookRepository;

  @Autowired
  private BookService bookService;

  @Test
  void shouldEnrichBookWithExternalInfoFromMockedClient() {
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

    verify(openLibraryApiClient, times(4)).getBookByIsbn(anyString());
    verify(bookRepository, times(1)).save(any());
  }
}
