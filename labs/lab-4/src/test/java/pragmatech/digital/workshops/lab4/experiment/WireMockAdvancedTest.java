package pragmatech.digital.workshops.lab4.experiment;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WireMockAdvancedTest {

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

  @Nested
  class StatefulScenarios {

    @Test
    void shouldSucceedOnRetryWhenFirstCallFails() {
      String isbn = "9780132350884";

      wireMockServer.stubFor(get("/isbn/" + isbn)
        .inScenario("retry")
        .whenScenarioStateIs(Scenario.STARTED)
        .willReturn(serverError())
        .willSetStateTo("recovered"));

      wireMockServer.stubFor(get("/isbn/" + isbn)
        .inScenario("retry")
        .whenScenarioStateIs("recovered")
        .willReturn(ok()
          .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
          .withBodyFile(isbn + "-success.json")));

      assertThatThrownBy(() -> cut.getBookByIsbn(isbn))
        .isInstanceOf(WebClientResponseException.InternalServerError.class);

      BookMetadataResponse result = cut.getBookByIsbn(isbn);

      assertThat(result.title()).isEqualTo("Clean Code");
    }
  }

  @Nested
  class RequestVerification {

    @Test
    void shouldVerifyExactNumberOfRequestsMade() {
      String isbn = "9780132350884";

      wireMockServer.stubFor(get("/isbn/" + isbn)
        .willReturn(ok()
          .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
          .withBodyFile(isbn + "-success.json")));

      cut.getBookByIsbn(isbn);
      cut.getBookByIsbn(isbn);

      wireMockServer.verify(2, getRequestedFor(urlEqualTo("/isbn/" + isbn)));
    }
  }

  @Nested
  class SlowResponses {

    @Test
    void shouldStillRespondWhenResponseIsDelayed() {
      String isbn = "9780132350884";
      int delayMs = 500;

      wireMockServer.stubFor(get("/isbn/" + isbn)
        .willReturn(ok()
          .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
          .withBodyFile(isbn + "-success.json")
          .withFixedDelay(delayMs)));

      long start = System.currentTimeMillis();
      BookMetadataResponse result = cut.getBookByIsbn(isbn);
      long elapsed = System.currentTimeMillis() - start;

      assertThat(result.title()).isEqualTo("Clean Code");
      assertThat(elapsed).isGreaterThanOrEqualTo(delayMs);
    }
  }
}
