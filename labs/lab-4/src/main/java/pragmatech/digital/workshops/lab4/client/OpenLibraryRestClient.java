package pragmatech.digital.workshops.lab4.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
    return restClient.get()
      .uri("/isbn/{isbn}", isbn)
      .retrieve()
      .body(BookMetadataResponse.class);
  }
}
