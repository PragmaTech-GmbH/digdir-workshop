package pragmatech.digital.workshops.lab5.experiment;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import pragmatech.digital.workshops.lab5.LocalDevTestcontainerConfig;
import pragmatech.digital.workshops.lab5.client.OpenLibraryApiClient;
import pragmatech.digital.workshops.lab5.dto.BookMetadataResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Showcases context customisation techniques:
 *
 * 1. @MockitoBean replaces the real OpenLibraryApiClient — no WireMock or
 *    ContextInitializer needed. The context starts without any outbound HTTP calls.
 *
 * 2. @SpringBootTest(properties = ...) inlines property overrides scoped to this
 *    test class, demonstrating the highest-priority override mechanism.
 *
 * 3. @Transactional on the class causes each test to roll back automatically —
 *    no @AfterEach cleanup required even though MockMvc creates real book records.
 *
 * Note: @MockitoBean forces a new application context. Avoid using it in large
 * suites where context reuse matters for performance.
 */
@SpringBootTest(
  properties = "logging.level.org.springframework.transaction=DEBUG"
)
@AutoConfigureMockMvc
@Transactional
@Import(LocalDevTestcontainerConfig.class)
class ContextCustomisationTest {

  @MockitoBean
  private OpenLibraryApiClient openLibraryApiClient;

  @Autowired
  private MockMvc mockMvc;

  private static final String VALID_ISBN = "978-0132350884";

  private static final BookMetadataResponse CLEAN_CODE_METADATA = new BookMetadataResponse(
    null, "Clean Code",
    List.of("9780132350884"), null,
    "2008", List.of("Prentice Hall"),
    null, 431, null, null, null, null
  );

  @BeforeEach
  void setUp() {
    when(openLibraryApiClient.getBookByIsbn(any())).thenReturn(CLEAN_CODE_METADATA);
  }

  @Nested
  class MockitoBeanReplacement {

    @Test
    @WithMockUser(roles = "USER")
    void shouldCreateBookWithMockedApiClientWithoutWireMock() throws Exception {
      String requestBody = """
        {
          "isbn": "%s",
          "title": "Clean Code",
          "author": "Robert C. Martin",
          "publishedDate": "2008-08-01"
        }
        """.formatted(VALID_ISBN);

      MvcResult result = mockMvc.perform(post("/api/books")
          .contentType(MediaType.APPLICATION_JSON)
          .content(requestBody))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andReturn();

      mockMvc.perform(get(result.getResponse().getHeader("Location")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isbn").value(VALID_ISBN))
        .andExpect(jsonPath("$.title").value("Clean Code"))
        .andExpect(jsonPath("$.author").value("Robert C. Martin"))
        .andExpect(jsonPath("$.status").value("AVAILABLE"));

      verify(openLibraryApiClient).getBookByIsbn(VALID_ISBN);
    }

    @Test
    void shouldReturnEmptyBookListWhenNoBooksExist() throws Exception {
      mockMvc.perform(get("/api/books"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());
    }
  }

  @Nested
  class PropertyOverride {

    @Test
    @WithMockUser(roles = "USER")
    void shouldRejectRequestWithInvalidIsbnFormat() throws Exception {
      String requestBody = """
        {
          "isbn": "not-a-valid-isbn",
          "title": "Some Book",
          "author": "Some Author",
          "publishedDate": "2020-01-01"
        }
        """;

      mockMvc.perform(post("/api/books")
          .contentType(MediaType.APPLICATION_JSON)
          .content(requestBody))
        .andExpect(status().isBadRequest());
    }
  }
}
