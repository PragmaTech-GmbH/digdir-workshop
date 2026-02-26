package pragmatech.digital.workshops.lab4.experiment;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pragmatech.digital.workshops.lab4.client.OpenLibraryApiClient;
import pragmatech.digital.workshops.lab4.dto.BookMetadataResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Disabled
class WireMockAdvancedTest {

  @RegisterExtension
  static WireMockExtension wireMockServer = WireMockExtension.newInstance()
    .options(wireMockConfig().dynamicPort().notifier(new ConsoleNotifier(true)))  // true = verbose
    .build();

  private OpenLibraryApiClient cut;

  @BeforeEach
  void setUp() {
    cut = new OpenLibraryApiClient(
      WebClient.builder().baseUrl(wireMockServer.baseUrl()).build());

    wireMockServer.resetAll();
  }

  /**
   * Feature: Response Templating
   * <p>
   * WireMock can use Handlebars templates in response bodies to dynamically inject
   * values from the incoming request (headers, query params, body, URL segments).
   * Enable per-stub with {@code withTransformers("response-template")}.
   * <p>
   * Common helpers: {{request.query.name.[0]}}, {{request.headers.X-Foo}},
   * {{jsonPath request.body '$.id'}}, {{now format='yyyy-MM-dd'}}
   */
  @Nested
  class ResponseTemplating {

    @Test
    void shouldInjectBibkeyQueryParamIntoResponseBody() {
      String isbn = "9780132350884";

      wireMockServer.stubFor(get(urlPathEqualTo("/api/books"))
        .willReturn(ok()
          .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
          .withBody("""
              {
                "{{request.query.bibkeys.[0]}}": {
                  "title": "Resolved for {{request.query.bibkeys.[0]}}",
                  "number_of_pages": 250
                }
              }
              """)
          .withTransformers("response-template")));

      BookMetadataResponse result = cut.getBookByIsbn(isbn);

      assertThat(result).isNotNull();
      assertThat(result.title()).isEqualTo("Resolved for ISBN:9780132350884");
      assertThat(result.numberOfPages()).isEqualTo(250);
    }
  }

  /**
   * Feature: Proxying & Recording
   * <p>
   * WireMock can act as a recording proxy: forward requests to a real upstream,
   * capture the responses as stub mappings, then replay them offline in future runs.
   * <p>
   * Typical workflow (run once against production/staging):
   * <pre>
   *   wireMockServer.startStubRecording(RecordSpec.forTarget("https://openlibrary.org").build());
   *   // ... exercise the code under test (real HTTP traffic flows through)
   *   wireMockServer.stopStubRecording();  // stubs saved to mappings/ + __files/
   * </pre>
   * Subsequent CI runs skip the real network entirely and replay from disk.
   * <p>
   * This test hits the real OpenLibrary API on the first call so WireMock can capture
   * an authentic response, then replays it offline on every subsequent call.
   */
  @Nested
  class ProxyingAndRecording {

    private WireMockServer recordingProxy;

    @BeforeEach
    void setUpRecordingProxy() {
      recordingProxy = new WireMockServer(wireMockConfig().dynamicPort());
      recordingProxy.start();
    }

    @AfterEach
    void tearDownRecordingProxy() {
      recordingProxy.stop();
    }

    @Test
    void shouldRecordLiveResponseAndReplayOfflineWithoutUpstream() {
      String isbn = "9780132350884";

      recordingProxy.startRecording(
        recordSpec().forTarget("https://openlibrary.org").makeStubsPersistent(true).build());

      OpenLibraryApiClient proxyClient = new OpenLibraryApiClient(
        WebClient.builder().baseUrl(recordingProxy.baseUrl()).build());

      BookMetadataResponse liveResponse = proxyClient.getBookByIsbn(isbn);
      assertThat(liveResponse.title()).isEqualTo("Clean Code");

      recordingProxy.stopRecording();

      BookMetadataResponse replayedResponse = proxyClient.getBookByIsbn(isbn);

      assertThat(replayedResponse.title()).isEqualTo("Clean Code");
    }
  }

  /**
   * Feature: Stateful Scenarios
   * <p>
   * WireMock scenarios let a single stub URL return different responses based on
   * a named state machine. Each stub declares which state it matches and what state
   * to transition to after responding.
   * <p>
   * Useful for simulating: retry / eventual consistency, pagination, CQRS read-after-write
   * lag, resource lifecycle (PENDING → ACTIVE → DELETED), etc.
   */
  @Nested
  class StatefulScenarios {

    @Test
    void shouldSucceedOnRetryWhenFirstCallFails() {
      String isbn = "9780132350884";

      wireMockServer.stubFor(get(urlPathEqualTo("/api/books"))
        .inScenario("eventual-consistency")
        .whenScenarioStateIs(Scenario.STARTED)
        .willReturn(serverError())
        .willSetStateTo("recovered"));

      wireMockServer.stubFor(get(urlPathEqualTo("/api/books"))
        .inScenario("eventual-consistency")
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

      wireMockServer.stubFor(get(urlPathEqualTo("/api/books"))
        .willReturn(ok()
          .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
          .withBodyFile(isbn + "-success.json")));

      cut.getBookByIsbn(isbn);
      cut.getBookByIsbn(isbn);

      wireMockServer.verify(2, getRequestedFor(urlPathEqualTo("/api/books")));
    }
  }

  @Nested
  class SlowResponses {

    @Test
    void shouldStillRespondWhenResponseIsDelayed() {
      String isbn = "9780132350884";
      int delayMs = 500;

      wireMockServer.stubFor(get(urlPathEqualTo("/api/books"))
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
