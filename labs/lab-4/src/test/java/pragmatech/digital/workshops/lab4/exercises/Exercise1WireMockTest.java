package pragmatech.digital.workshops.lab4.exercises;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.web.reactive.function.client.WebClient;
import pragmatech.digital.workshops.lab4.client.OpenLibraryApiClient;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * Exercise 1: Testing HTTP Clients with WireMock
 *
 * Your tasks:
 * 1. Implement shouldReturnBookMetadataWhenApiReturnsSuccessResponse:
 *    - Stub WireMock to return a 200 response with body from "__files/9780132350884-success.json"
 *    - Call cut.getBookByIsbn("9780132350884")
 *    - Assert the title is "Clean Code", publisher is "Prentice Hall", pages is 431
 *
 * 2. Implement shouldThrowExceptionWhenServerReturnsInternalError:
 *    - Stub WireMock to return a 500 response
 *    - Assert that calling cut.getBookByIsbn(...) throws WebClientResponseException.InternalServerError
 *
 * Optional:
 * 3. Handle 404 responses gracefully by returning null instead of throwing.
 *    Add a test shouldReturnNullWhenBookNotFound and modify OpenLibraryApiClient accordingly.
 *
 * Hints:
 * - Use wireMockServer.stubFor(get(urlPathEqualTo("/api/books")).willReturn(...))
 * - Use aResponse().withHeader(...).withBodyFile(...) for successful responses
 * - Use assertThatThrownBy(...).isInstanceOf(...) for exception assertions
 * - Check Solution1WireMockTest.java if you get stuck
 */
class Exercise1WireMockTest {

  @RegisterExtension
  static WireMockExtension wireMockServer = WireMockExtension.newInstance()
    .options(wireMockConfig().dynamicPort())
    .build();

  private OpenLibraryApiClient cut;

  @BeforeEach
  void setUp() {
    cut = new OpenLibraryApiClient(
      WebClient.builder().baseUrl(wireMockServer.baseUrl()).build());
  }

  @Test
  void shouldReturnBookMetadataWhenApiReturnsSuccessResponse() {
    // TODO: implement this test
  }

  @Test
  void shouldThrowExceptionWhenServerReturnsInternalError() {
    // TODO: implement this test
  }
}
