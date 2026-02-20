package pragmatech.digital.workshops.lab5.exercises;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pragmatech.digital.workshops.lab5.LocalDevTestcontainerConfig;
import pragmatech.digital.workshops.lab5.config.WireMockContextInitializer;

/**
 * Exercise 1: Integration Testing with MockMvc
 * <p>
 * In this exercise, you will write a full integration test using
 * {@code @SpringBootTest} with a MOCK web environment and MockMvc.
 * <p>
 * Key concepts to explore:
 * <ul>
 *   <li>MockMvc runs in the SAME thread as the test method</li>
 *   <li>When combined with {@code @Transactional}, changes are automatically rolled back</li>
 *   <li>No real HTTP server is started - requests are dispatched through the DispatcherServlet directly</li>
 *   <li>Use {@code @WithMockUser} for security context (not real HTTP Basic Auth)</li>
 * </ul>
 * <p>
 * Hints:
 * <ul>
 *   <li>The BookCreationRequest requires ISBN in format "123-1234567890"</li>
 *   <li>WireMock stubs need to match the ISBN passed to the OpenLibrary API</li>
 *   <li>POST /api/books returns a Location header with the created book's URL</li>
 *   <li>GET /api/books is public, GET /api/books/{id} requires USER role, DELETE requires ADMIN role</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(LocalDevTestcontainerConfig.class)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
class Exercise1MockMvcIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void shouldCreateAndRetrieveBookWhenUsingMockMvc() {
    // TODO:
    // 1. Add a WireMock stub for the ISBN you will use (inject WireMockServer as a bean)
    //    The OpenLibrary API client calls /isbn/{isbn} and the stub needs to return valid JSON.
    //    Use OpenLibraryApiStub to set up the stub.
    //
    // 2. Perform a POST to /api/books with a valid JSON body (use a text block):
    //    - Use MockMvcRequestBuilders.post("/api/books")
    //    - Set content type to APPLICATION_JSON
    //    - Use @WithMockUser(roles = "USER") or add it at method level
    //    - The JSON body needs: isbn (format "123-1234567890"), title, author, publishedDate
    //
    // 3. Assert the response status is 201 Created
    //
    // 4. Extract the Location header from the response
    //
    // 5. Perform a GET to the Location URL to retrieve the created book
    //    - Note: GET /api/books/{id} requires USER role
    //
    // 6. Assert the response contains the expected book data using jsonPath()
    //
    // Observe: Because MockMvc shares the same thread, @Transactional will
    // automatically roll back the data after this test completes.
  }

  @Test
  void shouldReturnAllBooksWhenUsingMockMvc() {
    // TODO:
    // 1. Perform a GET to /api/books (this endpoint is publicly accessible)
    //
    // 2. Assert the response status is 200 OK
    //
    // 3. Assert the response body is a JSON array using jsonPath("$").isArray()
  }

  @Test
  void shouldRejectInvalidBookCreationRequest() {
    // TODO:
    // 1. Perform a POST to /api/books with an INVALID JSON body
    //    (e.g., missing required fields or invalid ISBN format)
    //
    // 2. Assert the response status is 400 Bad Request
    //
    // Hint: Use @WithMockUser(roles = "USER") since POST requires authentication
  }
}
