package pragmatech.digital.workshops.lab4.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pragmatech.digital.workshops.lab4.dto.BookMetadataResponse;

/**
 * Client for interacting with the OpenLibrary API.
 */
@Component
public class OpenLibraryApiClient {

  private final WebClient webClient;

  public OpenLibraryApiClient(WebClient openLibraryWebClient) {
    this.webClient = openLibraryWebClient;
  }

  public BookMetadataResponse getBookByIsbn(String isbn) {
    return webClient.get()
      .uri("/isbn/{isbn}", isbn)
      .retrieve()
      .bodyToMono(BookMetadataResponse.class)
      .block();
  }
}
