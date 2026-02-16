package pragmatech.digital.workshops.lab2.experiment;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import pragmatech.digital.workshops.lab2.config.BlockBotRequestFilter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Disabled("Don't do this, favor a @WebMvcTest instead")
@ExtendWith(MockitoExtension.class)
class LlmBotRequestFilterTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain filterChain;

  @InjectMocks
  private BlockBotRequestFilter llmBotRequestFilter;

  @ParameterizedTest
  @ValueSource(strings = {
    "Mozilla/5.0 (compatible; OpenAI/1.0)",
    "Mozilla/5.0 Claude-Web/1.0",
    "Anthropic-AI-Bot/1.0",
    "ChatGPT-User/3.0",
    "GPT-4 Browser Helper/1.0",
    "Mozilla/5.0 (compatible; BingBot/2.0;)",
    "GoogleBot/2.1 (+http://www.google.com/bot.html)"
  })
  void shouldBlockRequestsFromLlmBots(String userAgent) throws ServletException, IOException {
    // Arrange
    when(request.getHeader("User-Agent")).thenReturn(userAgent);
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    // Act
    llmBotRequestFilter.doFilterInternal(request, response, filterChain);

    // Assert
    verify(response).setStatus(HttpStatus.FORBIDDEN.value());
    verify(response).setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    verify(filterChain, never()).doFilter(request, response);
    assertTrue(stringWriter.toString().contains("LLM Bot Access Denied"));
  }

  @Test
  void shouldAllowRegularUserRequests() throws ServletException, IOException {
    // Arrange
    when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

    // Act
    llmBotRequestFilter.doFilterInternal(request, response, filterChain);

    // Assert
    verify(filterChain).doFilter(request, response);
    verify(response, never()).setStatus(anyInt());
  }

  @Test
  void shouldHandleNullUserAgent() throws ServletException, IOException {
    // Arrange
    when(request.getHeader("User-Agent")).thenReturn(null);

    // Act
    llmBotRequestFilter.doFilterInternal(request, response, filterChain);

    // Assert
    verify(filterChain).doFilter(request, response);
    verify(response, never()).setStatus(anyInt());
  }
}
