package pragmatech.digital.workshops.lab2.solutions;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pragmatech.digital.workshops.lab2.config.SecurityConfig;
import pragmatech.digital.workshops.lab2.controller.BookController;
import pragmatech.digital.workshops.lab2.service.BookService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Solution 1: WebMvc Test
 * <p>
 * This class demonstrates testing a Spring MVC controller with security using @WebMvcTest
 * It focuses on testing the authorization rules and validations for the BookController
 */
@WebMvcTest(BookController.class)
@Import(SecurityConfig.class)
class Solution1WebMvcTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private BookService bookService;

  @Nested
  @DisplayName("DELETE /api/books/{id} endpoint tests")
  class DeleteBookTests {

    @Test
    @DisplayName("Should return 401 Unauthorized when no authentication is provided")
    void shouldReturnUnauthorizedWhenNoAuthenticationIsProvided() throws Exception {
      // Arrange - No authentication credentials provided

      // Act & Assert
      mockMvc.perform(delete("/api/books/1"))
        .andExpect(status().isUnauthorized());

      // Verify - Service should not be called
      verify(bookService, times(0)).deleteBook(any());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return 403 Forbidden when authenticated with insufficient privileges")
    void shouldReturnForbiddenWhenAuthenticatedWithInsufficientPrivileges() throws Exception {
      // Arrange - User with insufficient privileges (USER role)

      // Act & Assert
      mockMvc.perform(delete("/api/books/1"))
        .andExpect(status().isForbidden());

      // Verify - Service should not be called
      verify(bookService, times(0)).deleteBook(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 204 No Content when authenticated as admin and book exists")
    void shouldReturnNoContentWhenAuthenticatedAsAdminAndBookExists() throws Exception {
      // Arrange - User with admin privileges and book exists
      when(bookService.deleteBook(1L)).thenReturn(true);

      // Act & Assert
      mockMvc.perform(delete("/api/books/1"))
        .andExpect(status().isNoContent());

      // Verify - Service should be called with the book ID
      verify(bookService, times(1)).deleteBook(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 Not Found when authenticated as admin but book doesn't exist")
    void shouldReturnNotFoundWhenAuthenticatedAsAdminButBookDoesntExist() throws Exception {
      // Arrange - User with admin privileges but book doesn't exist
      when(bookService.deleteBook(999L)).thenReturn(false);

      // Act & Assert
      mockMvc.perform(delete("/api/books/999"))
        .andExpect(status().isNotFound());

      // Verify - Service should be called with the book ID
      verify(bookService, times(1)).deleteBook(999L);
    }
  }

  @Nested
  @DisplayName("POST /api/books endpoint tests")
  class CreateBookTests {

    @Test
    @WithMockUser
    @DisplayName("Should return 201 Created when valid book data is provided")
    void shouldReturnCreatedWhenValidBookDataIsProvided() throws Exception {
      // Arrange - Valid book data and service returns ID
      String validBookJson = """
        {
            "isbn": "123-1234567890",
            "title": "Test Book",
            "author": "Test Author",
            "publishedDate": "2023-01-01"
        }
        """;

      when(bookService.createBook(any())).thenReturn(1L);

      // Act & Assert
      mockMvc.perform(post("/api/books")
          .contentType(MediaType.APPLICATION_JSON)
          .content(validBookJson))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(header().string("Location", Matchers.containsString("/api/books/1")));

      // Verify - Service should be called
      verify(bookService, times(1)).createBook(any());
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 400 Bad Request when invalid book data is provided")
    void shouldReturnBadRequestWhenInvalidBookDataIsProvided() throws Exception {
      // Arrange - Invalid book data (missing required fields)
      String invalidBookJson = """
        {
            "isbn": "",
            "title": "",
            "author": "Test Author",
            "publishedDate": "2025-01-01"
        }
        """;

      // Act & Assert
      mockMvc.perform(post("/api/books")
          .contentType(MediaType.APPLICATION_JSON)
          .content(invalidBookJson))
        .andExpect(status().isBadRequest());

      // Verify - Service should not be called due to validation failure
      verify(bookService, times(0)).createBook(any());
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 400 Bad Request when ISBN format is invalid")
    void shouldReturnBadRequestWhenIsbnFormatIsInvalid() throws Exception {
      // Arrange - Invalid ISBN format
      String invalidIsbnJson = """
        {
            "isbn": "123456789X",
            "title": "Test Book",
            "author": "Test Author",
            "publishedDate": "2023-01-01"
        }
        """;

      // Act & Assert
      mockMvc.perform(post("/api/books")
          .contentType(MediaType.APPLICATION_JSON)
          .content(invalidIsbnJson))
        .andExpect(status().isBadRequest());

      // Verify - Service should not be called due to validation failure
      verify(bookService, times(0)).createBook(any());
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 400 Bad Request when publishedDate is in the future")
    void shouldReturnBadRequestWhenPublishedDateIsInFuture() throws Exception {
      // Arrange - Future published date
      String futureDateJson = """
        {
            "isbn": "123-1234567890",
            "title": "Test Book",
            "author": "Test Author",
            "publishedDate": "2099-01-01"
        }
        """;

      // Act & Assert
      mockMvc.perform(post("/api/books")
          .contentType(MediaType.APPLICATION_JSON)
          .content(futureDateJson))
        .andExpect(status().isBadRequest());

      // Verify - Service should not be called due to validation failure
      verify(bookService, times(0)).createBook(any());
    }
  }
}
