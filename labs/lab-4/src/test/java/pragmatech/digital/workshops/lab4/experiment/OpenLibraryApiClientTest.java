package pragmatech.digital.workshops.lab4.experiment;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pragmatech.digital.workshops.lab4.client.OpenLibraryApiClient;
import pragmatech.digital.workshops.lab4.dto.BookMetadataResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OpenLibraryApiClientTest {

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
  void shouldReturnBookMetadataWhenApiReturnsValidResponse() {
    String isbn = "9780132350884";

    wireMockServer.stubFor(
      get(urlPathEqualTo("/api/books"))
        .willReturn(aResponse()
          .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
          .withBodyFile(isbn + "-success.json")));

    BookMetadataResponse result = cut.getBookByIsbn(isbn);

    assertThat(result).isNotNull();
    assertThat(result.title()).isEqualTo("Clean Code");
    assertThat(result.getMainIsbn()).isEqualTo("9780132350884");
    assertThat(result.getPublisher()).isEqualTo("Prentice Hall");
    assertThat(result.numberOfPages()).isEqualTo(431);
  }

  @Test
  void shouldThrowExceptionWhenBookNotFound() {
    String isbn = "9999999999";

    wireMockServer.stubFor(
      get(urlPathEqualTo("/api/books"))
        .willReturn(aResponse().withStatus(404)));

    assertThatThrownBy(() -> cut.getBookByIsbn(isbn))
      .isInstanceOf(WebClientResponseException.NotFound.class);
  }

  @Test
  void shouldThrowExceptionWhenServerReturnsInternalError() {
    String isbn = "9999999999";

    wireMockServer.stubFor(
      get(urlPathEqualTo("/api/books"))
        .willReturn(aResponse()
          .withStatus(500)
          .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
          .withBody("{\"error\": \"Internal Server Error\"}")));

    assertThatThrownBy(() -> cut.getBookByIsbn(isbn))
      .isInstanceOf(WebClientResponseException.InternalServerError.class);
  }
}
