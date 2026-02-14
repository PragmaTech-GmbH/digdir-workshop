package pragmatech.digital.workshops.lab2.exercises;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pragmatech.digital.workshops.lab2.controller.BookController;
import pragmatech.digital.workshops.lab2.service.BookService;

/**
 * Exercise 1: WebMvc Test
 * <p>
 * This exercise demonstrates testing a Spring MVC controller with security using @WebMvcTest
 * <p>
 * Tasks:
 * 1. Test that only admins can delete books
 * 2. Test that regular users or unauthenticated users can't delete books
 * 3. Test that books can be created successfully with proper JSON data
 */
@WebMvcTest(BookController.class)
class Exercise1WebMvcTest {

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
      // Write a test that verifies DELETE requests without authentication return 401 Unauthorized
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return 403 Forbidden when authenticated with insufficient privileges")
    void shouldReturnForbiddenWhenAuthenticatedWithInsufficientPrivileges() throws Exception {
      // Write a test that verifies DELETE requests with USER role return 403 Forbidden
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 204 No Content when authenticated as admin and book exists")
    void shouldReturnNoContentWhenAuthenticatedAsAdminAndBookExists() throws Exception {
      // Write a test that verifies DELETE requests with ADMIN role are successful
      // Mock the service to return true indicating the book was deleted
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 Not Found when authenticated as admin but book doesn't exist")
    void shouldReturnNotFoundWhenAuthenticatedAsAdminButBookDoesntExist() throws Exception {
      // Write a test that verifies DELETE requests with ADMIN role return 404 when the book doesn't exist
      // Mock the service to return false indicating the book was not found
    }
  }

  @Nested
  @DisplayName("POST /api/books endpoint tests")
  class CreateBookTests {

    @Test
    @WithMockUser
    @DisplayName("Should return 201 Created when valid book data is provided")
    void shouldReturnCreatedWhenValidBookDataIsProvided() throws Exception {
      // Write a test that verifies POST requests with valid book data return 201 Created
      // Mock the service to return a book ID
      // Check that the Location header is present in the response
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 400 Bad Request when invalid book data is provided")
    void shouldReturnBadRequestWhenInvalidBookDataIsProvided() throws Exception {
      // Write a test that verifies POST requests with invalid book data return 400 Bad Request
      // Don't mock any service method since the validation should fail before the service is called
    }
  }
}
