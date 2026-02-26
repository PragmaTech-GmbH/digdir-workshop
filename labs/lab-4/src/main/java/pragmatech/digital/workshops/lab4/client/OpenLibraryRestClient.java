package pragmatech.digital.workshops.lab4.client;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import pragmatech.digital.workshops.lab4.dto.BookMetadataResponse;

/**
 * RestClient-based client for interacting with the OpenLibrary API.
 * Demonstrates @RestClientTest compatibility (unlike the WebClient-based OpenLibraryApiClient).
 */
@Component
public class OpenLibraryRestClient {

  private static final Logger logger = LoggerFactory.getLogger(OpenLibraryRestClient.class);

  private final RestClient restClient;

  public OpenLibraryRestClient(
    RestClient.Builder restClientBuilder,
    @Value("${book.metadata.api.url:https://openlibrary.org}") String baseUrl) {
    this.restClient = restClientBuilder
      .baseUrl(baseUrl)
      .build();
  }

  public BookMetadataResponse getBookByIsbn(String isbn) {
    logger.debug("Fetching book metadata for ISBN: {}", isbn);
    String bibKey = "ISBN:" + isbn;

    Map<String, BookMetadataResponse> result = restClient.get()
      .uri(uriBuilder -> uriBuilder
        .path("/api/books")
        .queryParam("bibkeys", bibKey)
        .queryParam("format", "json")
        .queryParam("jscmd", "data")
        .build())
      .retrieve()
      .body(new ParameterizedTypeReference<>() {});

    return result != null ? result.get(bibKey) : null;
  }
}
