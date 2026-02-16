package pragmatech.digital.workshops.lab2.experiment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pragmatech.digital.workshops.lab2.controller.BookController;
import pragmatech.digital.workshops.lab2.service.BookService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * This test demonstrates using @WebMvcTest to test a servlet filter.
 * Using MockMvc provides several advantages over mocking servlet components directly:
 * - Tests the filter in a more realistic environment
 * - Can test the filter's integration with the Spring MVC infrastructure
 * - Can test request/response handling with actual HTTP semantics
 * - Easier to maintain as it's less dependent on servlet implementation details
 */
@WebMvcTest(
  controllers = BookController.class,
  excludeAutoConfiguration = SecurityAutoConfiguration.class
)
class BlockBotRequestFilterWebMvcTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private BookService bookService;

  @ParameterizedTest
  @ValueSource(strings = {
    "Mozilla/5.0 (compatible; OpenAI/1.0)",
    "Mozilla/5.0 Claude-Web/1.0",
    "Anthropic-AI-Bot/1.0",
    "ChatGPT-User/3.0",
    "GPT-4 Browser Helper/1.0"
  })
  void shouldReturnTeapotStatusWhenRequestComesFromLlmBot(String userAgent) throws Exception {
    // Act & Assert
    MvcResult result = mockMvc.perform(get("/api/books/1")
        .header("User-Agent", userAgent))
      .andExpect(status().is(HttpStatus.I_AM_A_TEAPOT.value()))
      .andReturn();

    // Verify response body is empty JSON object
    assertThat(result.getResponse().getContentAsString()).isEqualTo("{}");
  }

  @Test
  void shouldAllowRequestWhenRegularUserAgent() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/api/books")
        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"))
      .andExpect(status().isOk());
  }

  @Test
  void shouldAllowRequestWhenUserAgentIsEmpty() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/api/books")
        .header("User-Agent", ""))
      .andExpect(status().isOk());
  }

  @Test
  void shouldAllowRequestWhenUserAgentHeaderIsMissing() throws Exception {
    // Act & Assert - No User-Agent header provided
    mockMvc.perform(get("/api/books"))
      .andExpect(status().isOk());
  }
}
