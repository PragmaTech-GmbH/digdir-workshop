package pragmatech.digital.workshops.lab5.solutions;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import pragmatech.digital.workshops.lab5.LocalDevTestcontainerConfig;
import pragmatech.digital.workshops.lab5.config.OpenLibraryApiStub;
import pragmatech.digital.workshops.lab5.config.WireMockContextInitializer;
import pragmatech.digital.workshops.lab5.entity.Book;
import pragmatech.digital.workshops.lab5.repository.BookRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Solution for Exercise 1: Integration Testing with MockMvc
 * <p>
 * This test uses {@code @SpringBootTest} with the default MOCK web environment
 * and {@code @AutoConfigureMockMvc} to test the full Spring context without
 * starting a real HTTP server.
 * <p>
 * Key observations:
 * <ul>
 *   <li>MockMvc dispatches requests through the DispatcherServlet in the SAME thread</li>
 *   <li>{@code @Transactional} causes automatic rollback after each test</li>
 *   <li>{@code @WithMockUser} provides a mock security context (no real auth needed)</li>
 *   <li>No real HTTP connection is made - faster than WebTestClient/TestRestTemplate</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(LocalDevTestcontainerConfig.class)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
class Solution1MockMvcIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private BookRepository bookRepository;

  @Autowired
  private OpenLibraryApiStub openLibraryApiStub;

  private static final String VALID_ISBN = "978-0134757599";


  @Test
  @WithMockUser(roles = "USER")
  void shouldCreateAndRetrieveBookWhenUsingMockMvc() throws Exception {
    // Arrange
    String requestBody = """
      {
        "isbn": "%s",
        "title": "Effective Java",
        "author": "Joshua Bloch",
        "publishedDate": "2018-01-06"
      }
      """.formatted(VALID_ISBN);

    openLibraryApiStub.stubForSuccessfulBookResponse(VALID_ISBN);

    // Act - Create a book
    MvcResult createResult = mockMvc.perform(post("/api/books")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
      .andExpect(status().isCreated())
      .andExpect(header().exists("Location"))
      .andReturn();

    // Extract the Location header to get the book URL
    String locationHeader = createResult.getResponse().getHeader("Location");

    // Act - Retrieve the created book
    mockMvc.perform(get(locationHeader)
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.isbn").value(VALID_ISBN))
      .andExpect(jsonPath("$.title").value("Effective Java"))
      .andExpect(jsonPath("$.author").value("Joshua Bloch"))
      .andExpect(jsonPath("$.status").value("AVAILABLE"));

    // Note: Because @Transactional is present and MockMvc runs in the same thread,
    // the created book will be automatically rolled back after this test completes.
  }

  @Test
  void shouldReturnAllBooksWhenUsingMockMvc() throws Exception {
    // GET /api/books is publicly accessible (permitAll)
    this.bookRepository.save(new Book("123-1234567890", "Book One", "Author A", LocalDate.now()));
    this.bookRepository.save(new Book("456-1234567890", "Book Two", "Author B", LocalDate.now()));

    mockMvc.perform(get("/api/books")
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$.size()").value(2));
  }

  @Test
  @WithMockUser(roles = "USER")
  void shouldRejectInvalidBookCreationRequest() throws Exception {
    // Arrange - Missing required fields and invalid ISBN format
    String invalidRequestBody = """
      {
        "isbn": "invalid-isbn",
        "title": "",
        "author": "",
        "publishedDate": null
      }
      """;

    // Act & Assert
    mockMvc.perform(post("/api/books")
        .contentType(MediaType.APPLICATION_JSON)
        .content(invalidRequestBody))
      .andExpect(status().isBadRequest());
  }
}
