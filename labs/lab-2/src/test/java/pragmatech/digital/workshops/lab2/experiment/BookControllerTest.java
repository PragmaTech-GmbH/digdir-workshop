package pragmatech.digital.workshops.lab2.experiment;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pragmatech.digital.workshops.lab2.config.SecurityConfig;
import pragmatech.digital.workshops.lab2.controller.BookController;
import pragmatech.digital.workshops.lab2.dto.BookCreationRequest;
import pragmatech.digital.workshops.lab2.dto.BookUpdateRequest;
import pragmatech.digital.workshops.lab2.entity.Book;
import pragmatech.digital.workshops.lab2.entity.BookStatus;
import pragmatech.digital.workshops.lab2.exception.BookAlreadyExistsException;
import pragmatech.digital.workshops.lab2.service.BookService;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * This test demonstrates using @WebMvcTest with MockMvc to test the BookController.
 * With this approach, we can test:
 * - HTTP requests and responses
 * - Content serialization/deserialization
 * - URL mappings
 * - Request parameter validation
 * - Exception handling
 * - Security constraints
 * <p>
 * The main advantage over a plain unit test is that we can test the full HTTP request/response cycle
 * without starting a full application context.
 */
@Import(SecurityConfig.class)
@WebMvcTest(BookController.class)
class BookControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private BookService bookService;

  @Test
  void shouldReturnAllBooksWhenGettingAllBooks() throws Exception {
    // Arrange
    Book book1 = new Book("123-1234567890", "Test Book", "Test Author", LocalDate.of(2020, 1, 1));
    Book book2 = new Book("456-1234567890", "Another Book", "Another Author", LocalDate.of(2021, 2, 2));

    when(bookService.getAllBooks()).thenReturn(Arrays.asList(book1, book2));

    // Act & Assert
    mockMvc.perform(get("/api/books"))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$", hasSize(2)))
      .andExpect(jsonPath("$[0].isbn", is("123-1234567890")))
      .andExpect(jsonPath("$[1].isbn", is("456-1234567890")));
  }

  @Test
  void shouldReturnBookWhenBookExistsById() throws Exception {
    // Arrange
    Book book = new Book("123-1234567890", "Test Book", "Test Author", LocalDate.of(2020, 1, 1));
    book.setStatus(BookStatus.AVAILABLE);

    when(bookService.getBookById(1L)).thenReturn(Optional.of(book));

    // Act & Assert
    mockMvc.perform(get("/api/books/1")
        .with(SecurityMockMvcRequestPostProcessors.user("user").roles("USER")))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.isbn", is("123-1234567890")))
      .andExpect(jsonPath("$.title", is("Test Book")))
      .andExpect(jsonPath("$.status", is("AVAILABLE")));
  }

  @Test
  void shouldReturnNotFoundWhenBookDoesNotExistById() throws Exception {
    // Arrange
    when(bookService.getBookById(anyLong())).thenReturn(Optional.empty());

    // Act & Assert
    mockMvc.perform(get("/api/books/999")
        .with(SecurityMockMvcRequestPostProcessors.user("user").roles("USER")))
      .andExpect(status().isNotFound());
  }

  @Test
  void shouldReturnCreatedWithLocationWhenCreatingBookWithValidData() throws Exception {
    // Arrange
    BookCreationRequest request = new BookCreationRequest(
      "123-1234567890",
      "Test Book",
      "Test Author",
      LocalDate.of(2020, 1, 1)
    );

    when(bookService.createBook(any(BookCreationRequest.class))).thenReturn(1L);

    // Act & Assert
    mockMvc.perform(post("/api/books")
        .with(SecurityMockMvcRequestPostProcessors.user("user").roles("USER"))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isCreated())
      .andExpect(header().string("Location", containsString("/api/books/1")));
  }

  @Test
  void shouldReturnBadRequestWhenCreatingBookWithInvalidISBN() throws Exception {
    // Arrange
    BookCreationRequest request = new BookCreationRequest(
      "invalid-isbn", // Invalid ISBN format
      "Test Book",
      "Test Author",
      LocalDate.of(2020, 1, 1)
    );

    // Act & Assert
    mockMvc.perform(post("/api/books")
        .with(SecurityMockMvcRequestPostProcessors.user("user").roles("USER"))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturnBadRequestWhenCreatingBookWithExistingISBN() throws Exception {
    // Arrange
    BookCreationRequest request = new BookCreationRequest(
      "123-1234567890",
      "Test Book",
      "Test Author",
      LocalDate.of(2020, 1, 1)
    );

    when(bookService.createBook(any(BookCreationRequest.class)))
      .thenThrow(new BookAlreadyExistsException("123-1234567890"));

    // Act & Assert
    mockMvc.perform(post("/api/books")
        .with(SecurityMockMvcRequestPostProcessors.user("user").roles("USER"))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.title", is("Book Already Exists")))
      .andExpect(jsonPath("$.detail", containsString("Book with ISBN 123-1234567890 already exists")));
  }

  @Test
  void shouldReturnUpdatedBookWhenUpdatingWithValidData() throws Exception {
    // Arrange
    BookUpdateRequest request = new BookUpdateRequest(
      "Updated Book",
      "Updated Author",
      LocalDate.of(2021, 1, 1),
      BookStatus.RESERVED
    );

    Book updatedBook = new Book("123-1234567890", "Updated Book", "Updated Author", LocalDate.of(2021, 1, 1));
    updatedBook.setStatus(BookStatus.RESERVED);

    when(bookService.updateBook(eq(1L), any(BookUpdateRequest.class))).thenReturn(Optional.of(updatedBook));

    // Act & Assert
    mockMvc.perform(put("/api/books/1")
        .with(SecurityMockMvcRequestPostProcessors.user("user").roles("USER"))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.title", is("Updated Book")))
      .andExpect(jsonPath("$.status", is("RESERVED")));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void shouldReturnNoContentWhenDeletingExistingBook() throws Exception {
    // Arrange
    when(bookService.deleteBook(1L)).thenReturn(true);

    // Act & Assert
    mockMvc.perform(delete("/api/books/1"))
      .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(roles = "USER")
    // Not an admin
  void shouldReturnForbiddenWhenDeletingBookWithInsufficientPermissions() throws Exception {
    // Act & Assert
    mockMvc.perform(delete("/api/books/1"))
      .andExpect(status().isForbidden());
  }

  @Test
  void shouldReturnUnauthorizedWhenGettingBookByIdWithoutAuthentication() throws Exception {
    // Act & Assert - No security context provided
    mockMvc.perform(get("/api/books/1"))
      .andExpect(status().isUnauthorized());
  }
}
