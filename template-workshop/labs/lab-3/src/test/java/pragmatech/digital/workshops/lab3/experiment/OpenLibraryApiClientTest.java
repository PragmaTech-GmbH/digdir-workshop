package pragmatech.digital.workshops.lab3.experiment;

import java.io.IOException;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pragmatech.digital.workshops.lab3.client.OpenLibraryApiClient;
import pragmatech.digital.workshops.lab3.dto.BookMetadataResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OpenLibraryApiClientTest {

  @RegisterExtension
  static WireMockExtension wireMockServer = WireMockExtension.newInstance()
    .options(wireMockConfig().dynamicPort())
    .build();

  private OpenLibraryApiClient cut;

  @BeforeEach
  void setUp() {
    WebClient webClient = WebClient.builder()
      .baseUrl(wireMockServer.baseUrl())
      .build();

    cut = new OpenLibraryApiClient(webClient);
  }

  @Test
  void shouldReturnBookMetadataWhenApiReturnsValidResponse() throws IOException {
    // Arrange
    String isbn = "9780132350884";

    wireMockServer.stubFor(
      get("/isbn/" + isbn)
        .willReturn(aResponse()
          .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
          .withBodyFile(isbn + "-success.json"))
    );

    // Act
    BookMetadataResponse result = cut.getBookByIsbn(isbn);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.title()).isEqualTo("Clean Code");
    assertThat(result.getMainIsbn()).isEqualTo("9780132350884");
    assertThat(result.getPublisher()).isEqualTo("Prentice Hall");
    assertThat(result.numberOfPages()).isEqualTo(431);
  }

  @Test
  void shouldThrowExceptionWhenBookNotFound() {
    // Arrange
    String isbn = "9999999999";

    wireMockServer.stubFor(
      get("/isbn/" + isbn)
        .willReturn(aResponse()
          .withStatus(404)));

    // Act & Assert
    assertThrows(WebClientResponseException.class, () ->
      cut.getBookByIsbn(isbn)
    );
  }
}
